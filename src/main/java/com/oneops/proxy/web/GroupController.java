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
import com.oneops.proxy.keywhiz.model.v2.*;
import com.oneops.proxy.model.AppGroup;
import com.oneops.proxy.model.SecretRequest;
import com.oneops.proxy.security.annotations.AuthzRestController;
import com.oneops.proxy.security.annotations.CurrentUser;
import com.oneops.proxy.services.SecretContentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.oneops.proxy.audit.EventTag.*;
import static java.util.Collections.singletonList;

/**
 * Keywhiz application group controller. <code>appGroup</code> is the OneOps environment
 * name with <b>{org}_{assembly}_{env}</b> format, for which you are managing the secrets.
 *
 * @author Suresh
 * @see AuthzRestController
 */
@AuthzRestController
@RequestMapping("/apps/{appGroup}")
public class GroupController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * A logger to audit all important events.
     */
    private final AuditLog auditLog;

    /**
     * Keywhiz automation client.
     */
    private KeywhizAutomationClient kwClient;

    /**
     * For validating secrets content.
     */
    private final SecretContentService secretService;

    /**
     * {@link GroupController} constructor.
     *
     * @param kwClient      Keywhiz automation client.
     * @param secretService Service containing utility functions to validate the secrets.
     * @param auditLog      Audit logger.
     */
    public GroupController(KeywhizAutomationClient kwClient, SecretContentService secretService, AuditLog auditLog) {
        this.kwClient = kwClient;
        this.secretService = secretService;
        this.auditLog = auditLog;
    }


    /**
     * Retrieve information on a group.
     *
     * @param appGroup OneOps environment name with <b>{org}_{assembly}_{env}</b> format, for which you are managing the secrets.
     * @param user     Authorized {@link OneOpsUser}
     * @return Group information ({@link GroupDetailResponseV2}) retrieved.
     * @throws IOException Throws if the request could not be executed due to cancellation, a connectivity
     *                     problem or timeout.
     */
    @GetMapping
    public GroupDetailResponseV2 info(AppGroup appGroup, @CurrentUser OneOpsUser user) throws IOException {
        return kwClient.getGroupDetails(appGroup.getKeywhizGroup());
    }

    /**
     * Retrieve metadata for clients in a particular group.
     *
     * @param appGroup OneOps environment name with <b>{org}_{assembly}_{env}</b> format, for which you are managing the secrets.
     * @param user     Authorized {@link OneOpsUser}
     * @return List of client information ({@link ClientDetailResponseV2}) retrieved.
     * @throws IOException Throws if the request could not be executed due to cancellation, a connectivity
     *                     problem or timeout.
     */
    @GetMapping("/clients")
    public List<ClientDetailResponseV2> getClients(AppGroup appGroup, @CurrentUser OneOpsUser user) throws IOException {
        return kwClient.getClients(appGroup.getKeywhizGroup());
    }

    /**
     * Retrieve metadata for secrets in a particular group.
     *
     * @param appGroup OneOps environment name with <b>{org}_{assembly}_{env}</b> format, for which you are managing the secrets.
     * @param user     Authorized {@link OneOpsUser}
     * @return List of secrets information ({@link SecretDetailResponseV2}) retrieved.
     * @throws IOException Throws if the request could not be executed due to cancellation, a connectivity
     *                     problem or timeout.
     */
    @GetMapping("/secrets")
    public List<SecretDetailResponseV2> getSecrets(AppGroup appGroup, @CurrentUser OneOpsUser user) throws IOException {
        return kwClient.getSecrets(appGroup.getKeywhizGroup());
    }

    /**
     * Creates new secret.
     *
     * @param name          Secret name
     * @param secretRequest Secret request {@link SecretRequest}
     * @param appGroup      OneOps environment name with <b>{org}_{assembly}_{env}</b> format, for which you are managing the secrets.
     * @param user          Authorized {@link OneOpsUser}
     * @throws IOException              Throws if the request could not be executed due to cancellation, a connectivity
     *                                  problem or timeout.
     * @throws IllegalArgumentException For bad request.
     */
    @PostMapping(value = "/secrets/{name}")
    public void createSecret(@PathVariable("name") String name, @RequestBody SecretRequest secretRequest, AppGroup appGroup, @CurrentUser OneOpsUser user) throws IOException {
        String groupName = appGroup.getGroupName();
        log.info(String.format("Creating new secret %s for application group: %s", name, groupName));

        SecretRequest secret = secretService.validateAndEnrich(secretRequest, name, user);
        checkAndCreateGroup(appGroup, user); // Create group if not exists.
        CreateSecretRequestV2 secretRequestV2 = CreateSecretRequestV2.fromParts(name,
                secret.getContent(),
                secret.getDescription(),
                secret.getMetadata(),
                secret.getExpiry(),
                secret.getType(),
                singletonList(groupName));

        kwClient.createSecret(secretRequestV2);

        auditLog.log(new Event(SECRET_CREATE, user.getUsername(), appGroup.getName(), name));
        log.info(String.format("Created new secret %s for application group: %s", name, groupName));
    }


    /**
     * Updates the secret.
     *
     * @param name          Secret name
     * @param secretRequest Secret request {@link SecretRequest}
     * @param appGroup      OneOps environment name with <b>{org}_{assembly}_{env}</b> format, for which you are managing the secrets.
     * @param user          Authorized {@link OneOpsUser}
     * @throws IOException              Throws if the request could not be executed due to cancellation, a connectivity
     *                                  problem or timeout.
     * @throws IllegalArgumentException For bad request.
     */
    @PutMapping("/secrets/{name}")
    public void updateSecret(@PathVariable("name") String name, @RequestBody SecretRequest secretRequest, AppGroup appGroup, @CurrentUser OneOpsUser user) throws IOException {
        log.info(String.format("Updating the secret %s for %s", name, appGroup.getGroupName()));

        checkSecretInGroup(name, appGroup); // Have to make sure secret exists in the appGroup.
        SecretRequest secret = secretService.validateAndEnrich(secretRequest, name, user);
        CreateOrUpdateSecretRequestV2 secretRequestV2 = CreateOrUpdateSecretRequestV2.fromParts(secret.getContent(),
                secret.getDescription(),
                secret.getMetadata(),
                secret.getExpiry(),
                secret.getType());

        kwClient.createOrUpdateSecret(name, secretRequestV2);

        auditLog.log(new Event(SECRET_UPDATE, user.getUsername(), appGroup.getName(), name));
        log.info(String.format("Updated the secret %s for %s", name, appGroup.getGroupName()));
    }

    /**
     * Retrieve information on a secret series.
     *
     * @param name     Secret name
     * @param appGroup OneOps environment name with <b>{org}_{assembly}_{env}</b> format, for which you are managing the secrets.
     * @param user     Authorized {@link OneOpsUser}
     * @throws IOException              Throws if the request could not be executed due to cancellation, a connectivity
     *                                  problem or timeout.
     * @throws IllegalArgumentException For bad request.
     */
    @GetMapping("/secrets/{name}")
    public SecretDetailResponseV2 getSecret(@PathVariable("name") String name, AppGroup appGroup, @CurrentUser OneOpsUser user) throws IOException {
        checkSecretInGroup(name, appGroup);
        return kwClient.getSecretDetails(name);
    }


    /**
     * Retrieve contents for a set of secret series. This is a POST request
     * because it updates the secret access metadata.
     *
     * @param name     Secret name
     * @param appGroup OneOps environment name with <b>{org}_{assembly}_{env}</b> format, for which you are managing the secrets.
     * @param user     Authorized {@link OneOpsUser}
     * @throws IOException              Throws if the request could not be executed due to cancellation, a connectivity
     *                                  problem or timeout.
     * @throws IllegalArgumentException For bad request.
     */
    @PostMapping("/secrets/{name}/contents")
    public Map<String, String> getSecretContent(@PathVariable("name") String name, AppGroup appGroup, @CurrentUser OneOpsUser user) throws IOException {
        checkSecretInGroup(name, appGroup);
        SecretContentsResponseV2 secretsContent = kwClient.getSecretsContent(name);

        auditLog.log(new Event(SECRET_READCONTENT, user.getUsername(), appGroup.getName(), name));
        return secretsContent.successSecrets();
    }

    /**
     * Checks if the secret is assigned to given application group.
     * Else throw {@link IllegalArgumentException}.
     *
     * @param secretName Secret name.
     * @param appGroup   Application group.
     * @throws IOException Throws if  the secret is not part of given application group.
     */
    private void checkSecretInGroup(String secretName, AppGroup appGroup) throws IOException {
        List<String> groups = kwClient.getGroupsForSecret(secretName);
        String groupName = appGroup.getGroupName();
        if (!groups.contains(groupName)) {
            log.error(String.format("Secret %s not found for %s", secretName, groupName));
            throw new IllegalArgumentException("Secret " + secretName + " not found.");
        }
    }

    /**
     * Creates new group if it's not exists.
     *
     * @param appGroup Application group info
     * @param user     OneOps user
     * @throws IOException Throws if the request could not be executed due to cancellation, a connectivity
     *                     problem or timeout.
     */
    private void checkAndCreateGroup(AppGroup appGroup, OneOpsUser user) throws IOException {
        String group = appGroup.getGroupName();
        log.info("Checking the application group: " + group);
        try {
            GroupDetailResponseV2 groupDetails = kwClient.getGroupDetails(appGroup.getKeywhizGroup());
            log.info(groupDetails.name() + " exists. Returning.");
            return;
        } catch (Exception ex) {
            log.error(group + " NOT exists, Error: " + ex.getMessage());
        }

        log.info("Creating the application group: " + group + ".");
        ImmutableMap<String, String> metadata = ImmutableMap.of("userId", user.getUsername());
        kwClient.createGroup(group, "Created by OneOps Proxy.", metadata);

        auditLog.log(new Event(GROUP_CREATE, user.getUsername(), group));
    }
}

