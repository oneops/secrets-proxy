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

import com.oneops.proxy.auth.user.OneOpsUser;
import com.oneops.proxy.keywhiz.KeywhizAutomationClient;
import com.oneops.proxy.keywhiz.model.v2.ClientDetailResponseV2;
import com.oneops.proxy.keywhiz.model.v2.CreateSecretRequestV2;
import com.oneops.proxy.keywhiz.model.v2.GroupDetailResponseV2;
import com.oneops.proxy.keywhiz.model.v2.SecretDetailResponseV2;
import com.oneops.proxy.model.AppGroup;
import com.oneops.proxy.model.SecretRequest;
import com.oneops.proxy.security.annotations.AuthzRestController;
import com.oneops.proxy.security.annotations.CurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

import static java.util.Collections.singletonList;

/**
 * Keywhiz application group controller. <code>appGroup</code> is the OneOps environment
 * name with <b>{org}_{assembly}_{env}</b> format, for which you are managing the secrets.
 *
 * @author Suresh
 */
@AuthzRestController
@RequestMapping("/groups/{appGroup}")
public class GroupController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Keywhiz automation client.
     */
    private KeywhizAutomationClient kwClient;

    /**
     * {@link GroupController} constructor.
     *
     * @param kwClient Keywhiz automation client.
     */
    public GroupController(KeywhizAutomationClient kwClient) {
        this.kwClient = kwClient;
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
     * @param appGroup OneOps environment name with <b>{org}_{assembly}_{env}</b> format, for which you are managing the secrets.
     * @param user     Secret name.
     * @param secret   Secret request {@link SecretRequest}
     * @param user     Authorized {@link OneOpsUser}
     * @return Secret Response details {@link SecretDetailResponseV2}.
     * @throws IOException              Throws if the request could not be executed due to cancellation, a connectivity
     *                                  problem or timeout.
     * @throws IllegalArgumentException For bad request..
     */
    @PostMapping(value = "/secrets/{name}")
    public void createSecret(@PathVariable("name") String name,
                             @RequestBody SecretRequest secret,
                             AppGroup appGroup,
                             @CurrentUser OneOpsUser user) throws IOException {
        CreateSecretRequestV2 secretReq;
        try {
            if (secret.getContent().length() > (1024 * 1024)) {
                throw new IllegalArgumentException("Entity too large!");
            }
            String content = Base64.getEncoder().encodeToString(secret.getContent().getBytes("UTF-8"));

            secretReq = CreateSecretRequestV2.fromParts(name, content, secret.getDescription(),
                    secret.getMetadata(), secret.getExpiry(), secret.getType(), singletonList(appGroup.getGroupName()));
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IllegalArgumentException("Invalid cert request, " + ex.getMessage());
        }
        kwClient.createSecret(secretReq);
    }
}
