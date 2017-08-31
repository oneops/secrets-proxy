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
import com.oneops.proxy.audit.AuditLog;
import com.oneops.proxy.audit.Event;
import com.oneops.proxy.auth.user.OneOpsUser;
import com.oneops.proxy.keywhiz.KeywhizAutomationClient;
import com.oneops.proxy.keywhiz.KeywhizException;
import com.oneops.proxy.keywhiz.model.v2.*;
import com.oneops.proxy.model.*;
import com.oneops.proxy.security.annotations.AuthzRestController;
import com.oneops.proxy.security.annotations.CurrentUser;
import com.oneops.proxy.service.SecretService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.oneops.proxy.audit.EventTag.*;
import static com.oneops.proxy.config.Constants.GROUP_CTLR_BASE_PATH;
import static com.oneops.proxy.model.AppGroup.DOMAIN_METADATA;
import static com.oneops.proxy.model.AppGroup.USERID_METADATA;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * An authenticated REST controller to manage Keywhiz application group and associated secrets.
 * <code>appGroup</code> is the OneOps environment name with <b>{org}_{assembly}_{env}</b> format,
 * for which you are managing the secrets. <b>appContext</b> will get injected automatically
 * depending on the current request context as either {@link AppGroup} or {@link AppSecret}.
 *
 * @author Suresh
 * @see AuthzRestController
 */
@AuthzRestController
@RequestMapping(GROUP_CTLR_BASE_PATH + "/{appGroup}")
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
     * @param appContext App Group info {@link AppGroup}.
     * @param user       Authorized {@link OneOpsUser}
     * @return Group information ({@link GroupDetailResponseV2}) retrieved.
     * @throws IOException Throws if the request could not be executed due to cancellation, a connectivity
     *                     problem or timeout.
     */
    @GetMapping
    public GroupDetailResponseV2 info(AppGroup appContext, @CurrentUser OneOpsUser user) throws IOException {
        GroupDetailResponseV2 groupDetails = kwClient.getGroupDetails(appContext.getKeywhizGroup());
        return secretService.normalize(groupDetails);
    }

    /**
     * Retrieve metadata for clients in a particular group.
     *
     * @param appContext App Group info {@link AppGroup}.
     * @param user       Authorized {@link OneOpsUser}
     * @return List of client information ({@link ClientDetailResponseV2}) retrieved.
     * @throws IOException Throws if the request could not be executed due to cancellation, a connectivity
     *                     problem or timeout.
     */
    @GetMapping("/clients")
    public List<ClientDetailResponseV2> getClients(AppGroup appContext, @CurrentUser OneOpsUser user) throws IOException {
        return kwClient.getClients(appContext.getKeywhizGroup());
    }


    /**
     * Retrieve information on a client.
     *
     * @param name       Client name
     * @param appContext App Group info {@link AppGroup}.
     * @param user       Authorized {@link OneOpsUser}
     * @return Client information ({@link ClientDetailResponseV2}) retrieved.
     * @throws IOException Throws if the request could not be executed due to cancellation, a connectivity
     *                     problem or timeout.
     */
    @GetMapping("/clients/{clientName}")
    public ClientDetailResponseV2 getClientDetails(@PathVariable("clientName") String name, AppGroup appContext, @CurrentUser OneOpsUser user) throws IOException {
        checkClientInGroup(name, appContext);
        return kwClient.getClientDetails(name);
    }


    /**
     * Retrieve metadata for secrets in a particular group.
     *
     * @param appContext App Group info {@link AppGroup}.
     * @param user       Authorized {@link OneOpsUser}
     * @return List of secrets information ({@link SecretDetailResponseV2}) retrieved.
     * @throws IOException Throws if the request could not be executed due to cancellation, a connectivity
     *                     problem or timeout.
     */
    @GetMapping("/secrets")
    public List<SecretDetailResponseV2> getSecrets(AppGroup appContext, @CurrentUser OneOpsUser user) throws IOException {
        List<SecretDetailResponseV2> secrets = kwClient.getSecrets(appContext.getKeywhizGroup());
        return secrets.stream().map(secretService::normalize).collect(Collectors.toList());
    }

    /**
     * Delete all secrets in a particular group.
     *
     * @param appContext App Group info {@link AppGroup}.
     * @param user       Authorized {@link OneOpsUser}
     * @return list of secrets names deleted.
     * @throws IOException Throws if the request could not be executed due to cancellation, a connectivity
     *                     problem or timeout.
     */
    @DeleteMapping("/secrets")
    public List<String> deleteAllSecrets(AppGroup appContext, @CurrentUser OneOpsUser user) throws IOException {
        List<AppSecret> appSecrets = kwClient.getSecrets(appContext.getKeywhizGroup()).stream().map(s -> new AppSecret(s.name())).collect(Collectors.toList());
        for (AppSecret secret : appSecrets) {
            purgeSecret(secret, user);
        }
        return appSecrets.stream().map(AppSecret::getSecretName).collect(Collectors.toList());
    }

    /**
     * Retrieve listing of secrets expiring soon in a group.
     *
     * @param time       Timestamp for farthest expiry to include.
     * @param appContext App Group info {@link AppGroup}.
     * @param user       Authorized {@link OneOpsUser}
     * @return List of secrets name expiring soon in group.
     * @throws IOException Throws if the request could not be executed due to cancellation, a connectivity
     *                     problem or timeout.
     */
    @GetMapping("/secrets/expiring/{time}")
    public List<String> getSecretsExpiring(@PathVariable("time") long time, AppGroup appContext, @CurrentUser OneOpsUser user) throws IOException {
        List<String> secrets = kwClient.getSecretsExpiring(appContext.getKeywhizGroup(), time);
        return secretService.normalize(secrets);
    }

    /**
     * Creates new secret.
     *
     * @param createGroup   <code>true</code> to create non existing application group. Default is <code>false</code>.
     * @param appContext    App Secret info {@link AppSecret}
     * @param secretRequest Secret request {@link SecretRequest}
     * @param user          Authorized {@link OneOpsUser}
     * @throws IOException      Throws if the request could not be executed due to cancellation, a connectivity
     *                          problem or timeout.
     * @throws KeywhizException Throws if application group doesn't exist or the secret with the same name already exists.
     */
    @PostMapping(value = "/secrets/{secretName}")
    @ResponseStatus(HttpStatus.CREATED)
    public void createSecret(@RequestParam(value = "createGroup", required = false, defaultValue = "false") boolean createGroup,
                             AppSecret appContext,
                             @RequestBody SecretRequest secretRequest,
                             @CurrentUser OneOpsUser user) throws IOException {

        String uniqSecretName = appContext.getUniqSecretName();
        String groupName = appContext.getGroupName();

        log.info(format("Creating new secret: %s", uniqSecretName));
        SecretRequest secret = secretService.validateAndEnrich(secretRequest, appContext, user);
        checkAndCreateGroup(appContext.getGroup(), createGroup, user); // Check and create group if not exists.

        CreateSecretRequestV2 secretRequestV2 = CreateSecretRequestV2.fromParts(uniqSecretName, secret.getContent(), secret.getDescription(), secret.getMetadata(), secret.getExpiry(), secret.getType(), singletonList(groupName));
        kwClient.createSecret(secretRequestV2);

        auditLog.log(new Event(SECRET_CREATE, user.getUsername(), appContext.getGroupName(), appContext.getSecretName()));
        log.info(format("Created new secret: %s ", uniqSecretName));
    }


    /**
     * Updates the secret.
     *
     * @param appContext    App Secret info {@link AppSecret}
     * @param secretRequest Secret request {@link SecretRequest}
     * @param user          Authorized {@link OneOpsUser}
     * @throws IOException      Throws if the request could not be executed due to cancellation, a connectivity
     *                          problem or timeout.
     * @throws KeywhizException Throws if the secret is not part of given application group.
     */
    @PutMapping("/secrets/{secretName}")
    @ResponseStatus(HttpStatus.CREATED)
    public void updateSecret(AppSecret appContext, @RequestBody SecretRequest secretRequest, @CurrentUser OneOpsUser user) throws IOException {

        String uniqSecretName = appContext.getUniqSecretName();

        log.info(format("Updating the secret: %s", uniqSecretName));
        checkSecretInGroup(appContext); // Have to make sure secret exists in the appGroup.

        SecretRequest secret = secretService.validateAndEnrich(secretRequest, appContext, user);
        kwClient.createOrUpdateSecret(uniqSecretName, secretService.makeCreateOrUpdateReq(secret));

        auditLog.log(new Event(SECRET_UPDATE, user.getUsername(), appContext.getGroupName(), appContext.getSecretName()));
        log.info(format("Updated the secret: %s", uniqSecretName));
    }


    /**
     * Retrieve information on a secret series.
     *
     * @param appContext App Secret info {@link AppSecret}
     * @param user       Authorized {@link OneOpsUser}
     * @throws IOException      Throws if the request could not be executed due to cancellation, a connectivity
     *                          problem or timeout.
     * @throws KeywhizException Throws if the secret is not part of given application group.
     */
    @GetMapping("/secrets/{secretName}")
    public SecretDetailResponseV2 getSecret(AppSecret appContext, @CurrentUser OneOpsUser user) throws IOException {
        String uniqSecretName = appContext.getUniqSecretName();

        checkSecretInGroup(appContext);
        SecretDetailResponseV2 secretDetails = kwClient.getSecretDetails(uniqSecretName);
        return secretService.normalize(secretDetails);
    }

    /**
     * Retrieve all versions of this secret, sorted from newest to oldest update time.
     *
     * @param appContext App Secret info {@link AppSecret}
     * @param user       Authorized {@link OneOpsUser}
     * @throws IOException      Throws if the request could not be executed due to cancellation, a connectivity
     *                          problem or timeout.
     * @throws KeywhizException Throws if the secret not exists or is not part of given application group.
     */
    @GetMapping("/secrets/{secretName}/versions")
    public List<SecretDetailResponseV2> getSecretVersions(AppSecret appContext, @CurrentUser OneOpsUser user) throws IOException {
        String uniqSecretName = appContext.getUniqSecretName();

        checkSecretInGroup(appContext);
        List<SecretDetailResponseV2> secrets = kwClient.getSecretVersions(uniqSecretName, 0, Integer.MAX_VALUE);
        return secrets.stream().map(secretService::normalize).collect(Collectors.toList());
    }

    /**
     * Retrieve all versions of this secret, sorted from newest to oldest update time.
     *
     * @param appContext    App Secret info {@link AppSecret}
     * @param secretVersion Version to be set.
     * @param user          Authorized {@link OneOpsUser}
     * @throws IOException      Throws if the request could not be executed due to cancellation, a connectivity
     *                          problem or timeout.
     * @throws KeywhizException Throws if the secret not exists or is not part of given application group.
     */
    @PostMapping("/secrets/{secretName}/setversion")
    @ResponseStatus(HttpStatus.CREATED)
    public void setSecretVersion(AppSecret appContext, @RequestBody SecretVersionRequest secretVersion, @CurrentUser OneOpsUser user) throws IOException {
        String uniqSecretName = appContext.getUniqSecretName();

        checkSecretInGroup(appContext);
        long version = secretVersion.getVersion();
        kwClient.setSecretVersion(uniqSecretName, version);

        Map<String, String> extInfo = new HashMap<>(1);
        extInfo.put("version", String.valueOf(version));
        auditLog.log(new Event(SECRET_CHANGEVERSION, user.getUsername(), appContext.getGroupName(), appContext.getSecretName(), extInfo));
    }

    /**
     * Delete a secret series.
     *
     * @param appContext App Secret info {@link AppSecret}
     * @param user       Authorized {@link OneOpsUser}
     * @throws IOException      Throws if the request could not be executed due to cancellation, a connectivity
     *                          problem or timeout.
     * @throws KeywhizException Throws if the secret not exists or not part of given application group.
     */
    @DeleteMapping("/secrets/{secretName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSecret(AppSecret appContext, @CurrentUser OneOpsUser user) throws IOException {
        checkSecretInGroup(appContext);
        purgeSecret(appContext, user);
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
     * Retrieve contents for a set of secret series. This is a POST request
     * because it updates the secret access metadata.
     *
     * @param appContext App Secret info {@link AppSecret}
     * @param user       Authorized {@link OneOpsUser}
     * @throws IOException      Throws if the request could not be executed due to cancellation, a connectivity
     *                          problem or timeout.
     * @throws KeywhizException Throws if the secret is not part of given application group.
     */
    @PostMapping("/secrets/{secretName}/contents")
    public SecretContent getSecretContent(AppSecret appContext, @CurrentUser OneOpsUser user) throws IOException {
        String uniqSecretName = appContext.getUniqSecretName();
        checkSecretInGroup(appContext);

        SecretContentsResponseV2 secretsContent = kwClient.getSecretsContent(uniqSecretName);
        auditLog.log(new Event(SECRET_READCONTENT, user.getUsername(), appContext.getGroupName(), appContext.getSecretName()));
        return SecretContent.from(uniqSecretName, secretsContent.successSecrets().get(uniqSecretName));
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
                kwClient.createGroup(group, "Created by OneOps Proxy.", ImmutableMap.of(USERID_METADATA, user.getUsername(), DOMAIN_METADATA, appGroup.getDomain()));
                auditLog.log(new Event(GROUP_CREATE, user.getUsername(), group));
            } else {
                throw new KeywhizException(NOT_FOUND.value(), format("Application group, %s not exists.", group), ex);
            }
        }
    }
}

