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
package com.oneops.proxy.health;

import com.oneops.proxy.keywhiz.KeywhizAutomationClient;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Add Keywhiz Server status to health (<b>/health</b>).
 *
 * @author Suresh
 */
@Component
public class KeywhizHealthIndicator extends AbstractHealthIndicator {

    private final KeywhizAutomationClient kwClient;

    public KeywhizHealthIndicator(KeywhizAutomationClient kwClient) {
        this.kwClient = kwClient;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        try {
            Map<String, Object> status = kwClient.getStatus();
            builder.up().status(status.get("message").toString());
        } catch (IOException ioe) {
            builder.down().withException(ioe);
        }
    }
}
