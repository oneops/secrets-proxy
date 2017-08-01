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
package com.oneops.proxy.keywhiz;

import com.fasterxml.jackson.core.type.TypeReference;
import com.oneops.proxy.keywhiz.http.HttpClient;
import com.oneops.proxy.keywhiz.model.v2.*;
import com.oneops.proxy.security.KeywhizKeyStore;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * Client for interacting with the Keywhiz Server using
 * mutually authenticated automation APIs. Automation client
 * is included mainly for testing purpose. The CLI is depends
 * on the {@link KeywhizClient}.
 *
 * @author Suresh
 */
public class KeywhizAutomationClient extends HttpClient {

    /**
     * Create a keywhiz automation client for the given baseurl.
     *
     * @param baseUrl         Keywhiz server base url
     * @param keywhizKeyStore Keywhiz keystore.
     * @throws GeneralSecurityException Throws if any error creating the https client.
     */
    public KeywhizAutomationClient(String baseUrl, KeywhizKeyStore keywhizKeyStore) throws GeneralSecurityException {
        super(baseUrl, keywhizKeyStore);
    }

    /**
     * Retrieve listing of all keywhiz client names.
     *
     * @return List of all client names.
     * @throws IOException Throws if the request could not be executed due to cancellation, a connectivity
     *                     problem or timeout.
     */
    public List<String> allClients() throws IOException {
        String httpResponse = httpGet(baseUrl.resolve("/automation/v2/clients"));
        return mapper.readValue(httpResponse, new TypeReference<List<String>>() {
        });
    }

    /**
     * Retrieve listing of application group names.
     *
     * @return List of all group names.
     * @throws IOException Throws if the request could not be executed due to cancellation, a connectivity
     *                     problem or timeout.
     */
    public List<String> getAllGroups() throws IOException {
        String httpResponse = httpGet(baseUrl.resolve("/automation/v2/groups"));
        return mapper.readValue(httpResponse, new TypeReference<List<String>>() {
        });
    }

    /**
     * Retrieve information on a group.
     *
     * @param group Group name
     * @return Group information ({@link GroupDetailResponseV2}) retrieved
     * @throws IOException Throws if the request could not be executed due to cancellation, a connectivity
     *                     problem or timeout.
     */
    public GroupDetailResponseV2 getGroupDetails(String group) throws IOException {
        String httpResponse = httpGet(baseUrl.resolve("/automation/v2/groups/" + group));
        return mapper.readValue(httpResponse, new TypeReference<GroupDetailResponseV2>() {
        });
    }

    /**
     * Retrieve metadata for clients in a particular group.
     *
     * @param group Group name.
     * @return List of client information ({@link ClientDetailResponseV2}) retrieved.
     * @throws IOException Throws if the request could not be executed due to cancellation, a connectivity
     *                     problem or timeout.
     */
    public List<ClientDetailResponseV2> getClients(String group) throws IOException {
        String httpResponse = httpGet(baseUrl.resolve("/automation/v2/groups/" + group + "/clients"));
        return mapper.readValue(httpResponse, new TypeReference<List<ClientDetailResponseV2>>() {
        });
    }

    /**
     * Retrieve information on a client
     *
     * @param name Client name.
     * @return Client information ({@link ClientDetailResponseV2}) retrieved.
     * @throws IOException Throws if the request could not be executed due to cancellation, a connectivity
     *                     problem or timeout.
     */
    public ClientDetailResponseV2 getClientDetails(String name) throws IOException {
        String httpResponse = httpGet(baseUrl.resolve("/automation/v2/clients/" + name));
        return mapper.readValue(httpResponse, new TypeReference<ClientDetailResponseV2>() {
        });
    }

    /**
     * Retrieve metadata for secrets in a particular group
     *
     * @param group Group name.
     * @return List of secrets information ({@link SecretDetailResponseV2}) retrieved.
     * @throws IOException Throws if the request could not be executed due to cancellation, a connectivity
     *                     problem or timeout.
     */
    public List<SecretDetailResponseV2> getSecrets(String group) throws IOException {
        String httpResponse = httpGet(baseUrl.resolve("/automation/v2/groups/" + group + "/secrets"));
        return mapper.readValue(httpResponse, new TypeReference<List<SecretDetailResponseV2>>() {
        });
    }

    /**
     * Creates or updates (if it exists) a secret.
     *
     * @param name   Secret name.
     * @param secret Secret details,  ${@link CreateOrUpdateSecretRequestV2}
     * @return Created or updated secret details response (${@link SecretDetailResponseV2})
     * @throws IOException Throws if the request could not be executed due to cancellation, a connectivity
     *                     problem or timeout.
     */
    public SecretDetailResponseV2 createOrUpdateSecret(String name, CreateOrUpdateSecretRequestV2 secret) throws IOException {
        String httpResponse = httpPost(baseUrl.resolve("/automation/v2/secrets/" + name), secret);
        return mapper.readValue(httpResponse, new TypeReference<List<SecretDetailResponseV2>>() {
        });
    }

    /**
     * Creates a secret and assigns to given groups
     *
     * @param secret Secret details,  ${@link CreateSecretRequestV2}
     * @throws IOException Throws if the request could not be executed due to cancellation, a connectivity
     *                     problem or timeout.
     */
    public void createSecret(CreateSecretRequestV2 secret) throws IOException {
        httpPost(baseUrl.resolve("/automation/v2/secrets"), secret);
    }

    /**
     * Creates a client and assigns to given groups.
     *
     * @return Client create response.
     */
    public String createClient(String name, String description, String... groups) throws IOException {
        CreateClientRequestV2 clientReq = CreateClientRequestV2.builder()
                .name(name)
                .description(description)
                .groups(groups).build();
        return httpPost(baseUrl.resolve("/automation/v2/clients"), clientReq);
    }

    /**
     * Automation client is using mTLS (client auth)
     */
    @Override
    public boolean isClientAuthEnabled() {
        return true;
    }
}
