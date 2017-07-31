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
import com.oneops.proxy.keywhiz.model.SanitizedSecret;
import com.oneops.proxy.security.annotations.AuthzRestController;
import com.oneops.proxy.security.annotations.CurrentUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.List;

/**
 * Keywhiz secrets controller.
 *
 * @author Suresh
 */
@AuthzRestController
@RequestMapping("/groups/{appGroup}")
public class SecretsController {

    /**
     * Keywhiz automation client.
     */
    private KeywhizAutomationClient kwClient;

    public SecretsController(KeywhizAutomationClient kwClient) {
        this.kwClient = kwClient;
    }

    /**
     * Get all client for application group.
     *
     * @param appGroup application group name.
     * @param user     authorized user.
     * @return list of all clients.
     * @throws IOException
     */
    @GetMapping("/clients")
    public List<String> getAllClients(@PathVariable("appGroup") String appGroup, @CurrentUser OneOpsUser user) throws IOException {
        return kwClient.allClients();
    }

    /**
     * Get all secrets
     *
     * @param appGroup application group name.
     * @param user     authorized user.
     * @return all secrets.
     * @throws IOException
     */
    @GetMapping("/secrets")
    public List<SanitizedSecret> getSecrets(@PathVariable("appGroup") String appGroup, @CurrentUser OneOpsUser user) throws IOException {
        return null;
    }
}
