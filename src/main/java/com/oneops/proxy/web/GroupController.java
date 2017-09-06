/*******************************************************************************
 *
 *   Copyright 2017 Walmart, Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *******************************************************************************/
package com.oneops.proxy.web;

import com.google.common.collect.ImmutableMap;
import com.oneops.proxy.audit.*;
import com.oneops.proxy.auth.user.OneOpsUser;
import com.oneops.proxy.keywhiz.*;
import com.oneops.proxy.keywhiz.model.v2.*;
import com.oneops.proxy.model.*;
import com.oneops.proxy.security.annotations.*;
import com.oneops.proxy.service.SecretService;
import io.swagger.annotations.*;
import org.slf4j.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

import static com.oneops.proxy.audit.EventTag.*;
import static com.oneops.proxy.auth.user.OneOpsUser.Role.ADMIN;
import static com.oneops.proxy.config.Constants.GROUP_CTLR_BASE_PATH;
import static com.oneops.proxy.model.AppGroup.*;
import static com.oneops.proxy.model.AppSecret.APP_SECRET_PARAM;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.springframework.http.HttpStatus.*;

/**
 * An authenticated REST controller to manage Keywhiz application group and associated secrets.
 * The <code>appName</code> is the OneOps environment name with <b>{org}_{assembly}_{env}</b>
 * format, for which you are managing the secrets.
 *
 * @author Suresh
 * @see AuthzRestController
 */
@AuthzRestController
@RequestMapping(GROUP_CTLR_BASE_PATH)
@Api(value = "Secrets EndPoint", description = "Secret Operations")
public class GroupController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * A logger to audit all important events.
     */
    private final AuditLog auditLog;

    /**
     * Keywhiz automation client.
     */
    private final KeywhizAutomationClient kwClient;

    /**
     * For validating secrets content.
     */
    private final SecretService secretService;

    /**
     * {@link GroupController} constructor.
     *
     * @param kwClient      Keywhiz automation client.
     * @param secretService Service containing utility functions to validate the secrets.
     * @param auditLog      Audit logger.
     */
    public GroupController(KeywhizAutomationClient kwClient, SecretService secretService, AuditLog auditLog) {
        this.kwClient = kwClient;
        this.secretService = secretService;
        this.auditLog = auditLog;
    }


    /**
     * Retrieve information on a group.
     *
     * @param appName OneOps application name.
     * @param user    Authorized {@link OneOpsUser}
     * @return Group information ({@link GroupDetailResponseV2}) retrieved.
     * @throws IOException Throws if the request could not be executed due to cancellation, a connectivity
     *                     problem or timeout.
     */
    @GetMapping
    @ApiOperation(value = "Retrieve application details")
    public GroupDetailResponseV2 info(@PathVariable(APP_NAME_PARAM) String appName, @CurrentUser OneOpsUser user) throws IOException {
        AppGroup appGroup = new AppGroup(user.getDomain(), appName);
        GroupDetailResponseV2 groupDetails = kwClient.getGroupDetails(appGroup.getKeywhizGroup());
        return secretService.normalize(groupDetails);
    }

    /**
     * Retrieve metadata for clients in a particular group.
     *
     * @param appName OneOps application name.
     * @param user    Authorized {@link OneOpsUser}
     * @return List of client information ({@link ClientDetailResponseV2}) retrieved.
     * @throws IOException Throws if the request could not be executed due to cancellation, a connectivity
     *                     problem or timeout.
     */
    @GetMapping("/clients")
    @ApiOperation(value = "Retrieve metadata for clients in an application")
    public List<ClientDetailResponseV2> getClients(@PathVariable(APP_NAME_PARAM) String appName, @CurrentUser OneOpsUser user) throws IOException {
        AppGroup appGroup = new AppGroup(user.getDomain(), appName);
        return kwClient.getClients(appGroup.getKeywhizGroup());
    }


    /**
     * Retrieve information on a client.
     *
     * @param appName OneOps application name.
     * @param name    Client name.
     * @param user    Authorized {@link OneOpsUser}
     * @return Client information ({@link ClientDetailResponseV2}) retrieved.
     * @throws IOException Throws if the request could not be executed due to cancellation, a connectivity
     *                     problem or timeout.
     */
    @GetMapping("/clients/{clientName}")
    @ApiOperation(value = "Retrieve information on a client in an application")
    public ClientDetailResponseV2 getClientDetails(@PathVariable(APP_NAME_PARAM) String appName, @PathVariable("clientName") String name, @CurrentUser OneOpsUser user) throws IOException {
        checkClientInGroup(name, AppGroup.from(user.getDomain(), appName));
        return kwClient.getClientDetails(name);
    }

    /**
     * Deletes a client. It's exposed only for admin users..
     *
     * @param appName OneOps application name.
     * @param name    Client name to be deleted.
     * @param user    Authorized {@link OneOpsUser}
     * @throws IOException Throws if the request could not be executed due to cancellation, a connectivity
     *                     problem or timeout.
     */
    @DeleteMapping("/clients/{clientName}")
    @ResponseStatus(NO_CONTENT)
    @ApiOperation(value = "Delete a given client in an application", notes = "For Admin users only!")
    public void deleteClient(@PathVariable(APP_NAME_PARAM) String appName, @PathVariable("clientName") String name, @CurrentUser OneOpsUser user) throws IOException {
        AppGroup appGroup = new AppGroup(user.getDomain(), appName);
        checkClientInGroup(name, appGroup);

        if (user.hasRole(ADMIN)) {
            kwClient.deleteClient(name);
            auditLog.log(new Event(CLIENT_DELETE, user.getUsername(), appGroup.getGroupName(), name));
        } else {
            log.error(String.format("User: %s don't have %s permission to delete client: %s for %s", user.getUsername(), ADMIN, name, appGroup.getGroupName()));
            throw new KeywhizException(FORBIDDEN.value(), format("User %s don't have permission to delete the client.", user.getUsername()));
        }
    }


    /**
     * Retrieve metadata for secrets in a particular group.
     *
     * @param appName OneOps application name.
     * @param user    Authorized {@link OneOpsUser}
     * @return List of secrets information ({@link SecretDetailResponseV2}) retrieved.
     * @throws IOException Throws if the request could not be executed due to cancellation, a connectivity
     *                     problem or timeout.
     */
    @GetMapping("/secrets")
    @ApiOperation(value = "Retrieve metadata for secrets in an application")
    public List<SecretDetailResponseV2> getSecrets(@PathVariable(APP_NAME_PARAM) String appName, @CurrentUser OneOpsUser user) throws IOException {
        AppGroup appGroup = new AppGroup(user.getDomain(), appName);
        List<SecretDetailResponseV2> secrets = kwClient.getSecrets(appGroup.getKeywhizGroup());
        return secrets.stream().map(secretService::normalize).collect(Collectors.toList());
    }

    /**
     * Delete all secrets in a particular group.
     *
     * @param appName OneOps application name.
     * @param user    Authorized {@link OneOpsUser}
     * @return list of secrets names deleted.
     * @throws IOException Throws if the request could not be executed due to cancellation, a connectivity
     *                     problem or timeout.
     */
    @DeleteMapping("/secrets")
    @ApiOperation(value = "Delete all secrets in an application")
    public List<String> deleteAllSecrets(@PathVariable(APP_NAME_PARAM) String appName, @CurrentUser OneOpsUser user) throws IOException {
        AppGroup appGroup = new AppGroup(user.getDomain(), appName);
        List<AppSecret> appSecrets = kwClient.getSecrets(appGroup.getKeywhizGroup()).stream().map(s -> new AppSecret(s.name())).collect(Collectors.toList());
        for (AppSecret secret : appSecrets) {
            purgeSecret(secret, user);
        }
        return appSecrets.stream().map(AppSecret::getSecretName).collect(Collectors.toList());
    }

    /**
     * Retrieve listing of secrets expiring soon in a group.
     *
     * @param appName OneOps application name.
     * @param time    Timestamp for farthest expiry to include.
     * @param user    Authorized {@link OneOpsUser}
     * @return List of secrets name expiring soon in group.
     * @throws IOException Throws if the request could not be executed due to cancellation, a connectivity
     *                     problem or timeout.
     */
    @GetMapping("/secrets/expiring/{time}")
    @ApiOperation(value = "Retrieve listing of secrets expiring soon in an application")
    public List<String> getSecretsExpiring(@PathVariable(APP_NAME_PARAM) String appName, @PathVariable("time") long time, @CurrentUser OneOpsUser user) throws IOException {
        AppGroup appGroup = new AppGroup(user.getDomain(), appName);
        List<String> secrets = kwClient.getSecretsExpiring(appGroup.getKeywhizGroup(), time);
        return secretService.normalize(secrets);
    }

    /**
     * Creates new secret.
     *
     * @param createGroup   <code>true</code> to create non existing application group. Default is <code>false</code>.
     * @param appName       Application name.
     * @param secretName    secret name.
     * @param secretRequest Secret request {@link SecretRequest}
     * @param user          Authorized {@link OneOpsUser}
     * @throws IOException      Throws if the request could not be executed due to cancellation, a connectivity
     *                          problem or timeout.
     * @throws KeywhizException Throws if application group doesn't exist or the secret with the same name already exists.
     */
    @PostMapping(value = "/secrets/{secretName}")
    @ResponseStatus(CREATED)
    @ApiOperation(value = "Creates a secret for an application")
    public void createSecret(@RequestParam(value = "createGroup", required = false, defaultValue = "false") boolean createGroup,
                             @PathVariable(APP_NAME_PARAM) String appName,
                             @PathVariable(APP_SECRET_PARAM) String secretName,
                             @RequestBody SecretRequest secretRequest,
                             @CurrentUser OneOpsUser user) throws IOException {

        AppSecret appSecret = new AppSecret(secretName, user.getDomain(), appName);
        String uniqSecretName = appSecret.getUniqSecretName();
        String groupName = appSecret.getGroupName();

        log.info(format("Creating new secret: %s", uniqSecretName));
        SecretRequest secret = secretService.validateAndEnrichReq(secretRequest, appSecret, user);
        checkAndCreateGroup(appSecret.getGroup(), createGroup, user); // Check and create group if not exists.

        CreateSecretRequestV2 secretRequestV2 = CreateSecretRequestV2.fromParts(uniqSecretName, secret.getContent(), secret.getDescription(), secret.getMetadata(), secret.getExpiry(), secret.getType(), singletonList(groupName));
        kwClient.createSecret(secretRequestV2);

        auditLog.log(new Event(SECRET_CREATE, user.getUsername(), appSecret.getGroupName(), appSecret.getSecretName()));
        log.info(format("Created new secret: %s ", uniqSecretName));
    }


    /**
     * Updates the secret.
     *
     * @param appName       Application name.
     * @param secretName    secret name.
     * @param secretRequest Secret request {@link SecretRequest}
     * @param user          Authorized {@link OneOpsUser}
     * @throws IOException      Throws if the request could not be executed due to cancellation, a connectivity
     *                          problem or timeout.
     * @throws KeywhizException Throws if the secret is not part of given application group.
     */
    @PutMapping("/secrets/{secretName}")
    @ResponseStatus(CREATED)
    @ApiOperation(value = "Updates a secret for an application")
    public void updateSecret(@PathVariable(APP_NAME_PARAM) String appName,
                             @PathVariable(APP_SECRET_PARAM) String secretName,
                             @RequestBody SecretRequest secretRequest,
                             @CurrentUser OneOpsUser user) throws IOException {

        AppSecret appSecret = new AppSecret(secretName, user.getDomain(), appName);
        String uniqSecretName = appSecret.getUniqSecretName();

        log.info(format("Updating the secret: %s", uniqSecretName));
        checkSecretInGroup(appSecret); // Have to make sure secret exists in the appGroup.

        SecretRequest secret = secretService.validateAndEnrichReq(secretRequest, appSecret, user);
        kwClient.createOrUpdateSecret(uniqSecretName, secretService.makeCreateOrUpdateReq(secret));

        auditLog.log(new Event(SECRET_UPDATE, user.getUsername(), appSecret.getGroupName(), appSecret.getSecretName()));
        log.info(format("Updated the secret: %s", uniqSecretName));
    }


    /**
     * Retrieve information on a secret series.
     *
     * @param appName    Application name.
     * @param secretName secret name.
     * @param user       Authorized {@link OneOpsUser}
     * @throws IOException      Throws if the request could not be executed due to cancellation, a connectivity
     *                          problem or timeout.
     * @throws KeywhizException Throws if the secret is not part of given application group.
     */
    @GetMapping("/secrets/{secretName}")
    @ApiOperation(value = "Retrieve information on a secret series in an application")
    public SecretDetailResponseV2 getSecret(@PathVariable(APP_NAME_PARAM) String appName,
                                            @PathVariable(APP_SECRET_PARAM) String secretName,
                                            @CurrentUser OneOpsUser user) throws IOException {

        AppSecret appSecret = new AppSecret(secretName, user.getDomain(), appName);
        String uniqSecretName = appSecret.getUniqSecretName();

        checkSecretInGroup(appSecret);
        SecretDetailResponseV2 secretDetails = kwClient.getSecretDetails(uniqSecretName);
        return secretService.normalize(secretDetails);
    }

    /**
     * Retrieve all versions of this secret, sorted from newest to oldest update time.
     *
     * @param appName    Application name.
     * @param secretName secret name.
     * @throws IOException      Throws if the request could not be executed due to cancellation, a connectivity
     *                          problem or timeout.
     * @throws KeywhizException Throws if the secret not exists or is not part of given application group.
     */
    @GetMapping("/secrets/{secretName}/versions")
    @ApiOperation(value = "Retrieve all versions of this secret in an application")
    public List<SecretDetailResponseV2> getSecretVersions(@PathVariable(APP_NAME_PARAM) String appName,
                                                          @PathVariable(APP_SECRET_PARAM) String secretName,
                                                          @CurrentUser OneOpsUser user) throws IOException {

        AppSecret appSecret = new AppSecret(secretName, user.getDomain(), appName);
        String uniqSecretName = appSecret.getUniqSecretName();

        checkSecretInGroup(appSecret);
        List<SecretDetailResponseV2> secrets = kwClient.getSecretVersions(uniqSecretName, 0, Integer.MAX_VALUE);
        return secrets.stream().map(secretService::normalize).collect(Collectors.toList());
    }

    /**
     * Retrieve all versions of this secret, sorted from newest to oldest update time.
     *
     * @param appName    Application name.
     * @param secretName secret name.
     * @param user       Authorized {@link OneOpsUser}
     * @throws IOException      Throws if the request could not be executed due to cancellation, a connectivity
     *                          problem or timeout.
     * @throws KeywhizException Throws if the secret not exists or is not part of given application group.
     */
    @PostMapping("/secrets/{secretName}/setversion")
    @ResponseStatus(CREATED)
    @ApiOperation(value = "Set the version of given secret in an application")
    public void setSecretVersion(@PathVariable(APP_NAME_PARAM) String appName,
                                 @PathVariable(APP_SECRET_PARAM) String secretName,
                                 @RequestBody SecretVersionRequest secretVersion,
                                 @CurrentUser OneOpsUser user) throws IOException {

        AppSecret appSecret = new AppSecret(secretName, user.getDomain(), appName);
        String uniqSecretName = appSecret.getUniqSecretName();

        checkSecretInGroup(appSecret);
        long version = secretVersion.getVersion();
        kwClient.setSecretVersion(uniqSecretName, version);

        Map<String, String> extInfo = new HashMap<>(1);
        extInfo.put("version", String.valueOf(version));
        auditLog.log(new Event(SECRET_CHANGEVERSION, user.getUsername(), appSecret.getGroupName(), appSecret.getSecretName(), extInfo));
    }

    /**
     * Delete a secret series.
     *
     * @param appName    Application name.
     * @param secretName secret name.
     * @param user       Authorized {@link OneOpsUser}
     * @throws IOException      Throws if the request could not be executed due to cancellation, a connectivity
     *                          problem or timeout.
     * @throws KeywhizException Throws if the secret not exists or not part of given application group.
     */
    @DeleteMapping("/secrets/{secretName}")
    @ResponseStatus(NO_CONTENT)
    @ApiOperation(value = "Deletes the given secret in an application")
    public void deleteSecret(@PathVariable(APP_NAME_PARAM) String appName,
                             @PathVariable(APP_SECRET_PARAM) String secretName,
                             @CurrentUser OneOpsUser user) throws IOException {

        AppSecret appSecret = new AppSecret(secretName, user.getDomain(), appName);
        checkSecretInGroup(appSecret);
        purgeSecret(appSecret, user);
    }


    /**
     * Retrieve contents for a set of secret series. This is a POST request
     * because it updates the secret access metadata.
     *
     * @param appName    Application name.
     * @param secretName secret name.
     * @param user       Authorized {@link OneOpsUser}
     * @throws IOException      Throws if the request could not be executed due to cancellation, a connectivity
     *                          problem or timeout.
     * @throws KeywhizException Throws if the secret is not part of given application group.
     */
    @PostMapping("/secrets/{secretName}/contents")
    @ApiOperation(value = "Retrieve content for a given secret in an application")
    public SecretContent getSecretContent(@PathVariable(APP_NAME_PARAM) String appName,
                                          @PathVariable(APP_SECRET_PARAM) String secretName,
                                          @CurrentUser OneOpsUser user) throws IOException {

        AppSecret appSecret = new AppSecret(secretName, user.getDomain(), appName);
        String uniqSecretName = appSecret.getUniqSecretName();
        checkSecretInGroup(appSecret);

        SecretContentsResponseV2 secretsContent = kwClient.getSecretsContent(uniqSecretName);
        auditLog.log(new Event(SECRET_READCONTENT, user.getUsername(), appSecret.getGroupName(), appSecret.getSecretName()));
        return SecretContent.from(uniqSecretName, secretsContent.successSecrets().get(uniqSecretName));
    }


    /**
     * Helper method to delete a single secret.
     */
    private void purgeSecret(AppSecret appSecret, @CurrentUser OneOpsUser user) throws IOException {
        String uniqSecretName = appSecret.getUniqSecretName();
        if (log.isDebugEnabled()) {
            log.debug("Deleting secret: " + uniqSecretName);
        }
        kwClient.deleteSecret(URLEncoder.encode(uniqSecretName, "UTF-8"));
        auditLog.log(new Event(SECRET_DELETE, user.getUsername(), appSecret.getGroupName(), appSecret.getSecretName()));
    }

    /**
     * Checks if the client is assigned to given application group, else throw {@link IOException}.
     *
     * @param clientName Client name.
     * @param appGroup   Application group.
     * @throws IOException      Throws if the request could not be executed.
     * @throws KeywhizException Throws if the client is not part of given application group.
     */
    private void checkClientInGroup(String clientName, AppGroup appGroup) throws IOException {
        List<ClientDetailResponseV2> clients = kwClient.getClients(appGroup.getKeywhizGroup());
        boolean clientExists = clients.stream().anyMatch(client -> client.name().equalsIgnoreCase(clientName));
        if (!clientExists) {
            log.error(format("Client %s not found for app %s", clientName, appGroup.getGroupName()));
            throw new KeywhizException(NOT_FOUND.value(), "Client " + clientName + " not found.");
        }
    }

    /**
     * Checks if the secret is assigned to given application group, else throw {@link IOException}.
     *
     * @param appSecret App secret with group details.
     * @throws IOException      Throws if the request could not be executed.
     * @throws KeywhizException Throws if the secret is not part of given application group.
     */
    private void checkSecretInGroup(AppSecret appSecret) throws IOException {
        String uniqSecretName = appSecret.getUniqSecretName();
        String groupName = appSecret.getGroupName();

        List<String> groups = kwClient.getGroupsForSecret(uniqSecretName);
        boolean groupExists = groups.stream().anyMatch(group -> group.equals(groupName));
        if (!groupExists) {
            log.error(format("Secret %s not found for %s", uniqSecretName, groupName));
            throw new KeywhizException(NOT_FOUND.value(), "Secret " + appSecret.getSecretName() + " not found.");
        }
    }

    /**
     * Creates new group if it's not exists.
     *
     * @param appGroup    Application group info.
     * @param createGroup <code>true</code> to create non existing application group.
     * @param user        OneOps user.
     * @throws IOException      Throws if the request could not be executed.
     * @throws KeywhizException Throws if the app group doesn't exist.
     */
    private void checkAndCreateGroup(AppGroup appGroup, boolean createGroup, OneOpsUser user) throws IOException {
        String group = appGroup.getGroupName();
        log.info("Checking the application group: " + group + " with createGroup: " + createGroup);
        try {
            GroupDetailResponseV2 groupDetails = kwClient.getGroupDetails(appGroup.getKeywhizGroup());
            log.info(groupDetails.name() + " exists. Returning.");
        } catch (IOException ex) {
            if (createGroup) {
                log.error(group + " not exists. Error: " + ex.getMessage());
                log.info("Creating the application group: " + group + ".");
                kwClient.createGroup(group, "Created by OneOps Secrets Proxy.", ImmutableMap.of(USERID_METADATA, user.getUsername(), DOMAIN_METADATA, appGroup.getDomain()));
                auditLog.log(new Event(GROUP_CREATE, user.getUsername(), group));
            } else {
                throw new KeywhizException(NOT_FOUND.value(), format("Application group, %s not exists.", group), ex);
            }
        }
    }
}

