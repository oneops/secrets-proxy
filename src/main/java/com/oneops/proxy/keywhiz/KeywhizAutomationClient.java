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
import com.oneops.proxy.keywhiz.model.v2.CreateClientRequestV2;
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
     * @param baseUrl         keywhiz server base url
     * @param keywhizKeyStore keywhiz keystore.
     * @throws GeneralSecurityException throws if any error creating the https client.
     */
    public KeywhizAutomationClient(String baseUrl, KeywhizKeyStore keywhizKeyStore) throws GeneralSecurityException {
        super(baseUrl, keywhizKeyStore);
    }

    public List<String> allClients() throws IOException {
        String httpResponse = httpGet(baseUrl.resolve("/automation/v2/clients"));
        return mapper.readValue(httpResponse, new TypeReference<List<String>>() {
        });
    }

    /**
     * Creates a client and assigns to given groups.
     *
     * @return client create response.
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
