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
package com.oneops.proxy.endpoint;

import com.oneops.proxy.keywhiz.KeywhizAutomationClient;
import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom actuator endpoint (<b>/keywhiz</b>) to display keywhiz status.
 *
 * @author Suresh G
 */
@Component
public class KeywhizEndPoint extends AbstractEndpoint<Map<String, Object>> {

    private final KeywhizAutomationClient kwClient;

    public KeywhizEndPoint(KeywhizAutomationClient kwClient) {
        super("keywhiz", false, true);
        this.kwClient = kwClient;
    }

    @Override
    public Map<String, Object> invoke() {
        try {
            return kwClient.getStatus();
        } catch (IOException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", "down");
            err.put("message", e.getMessage());
            return err;
        }
    }
}
