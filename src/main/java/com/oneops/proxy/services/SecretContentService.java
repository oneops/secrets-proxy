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
package com.oneops.proxy.services;

import com.oneops.proxy.auth.user.OneOpsUser;
import com.oneops.proxy.config.OneOpsConfig;
import com.oneops.proxy.model.SecretRequest;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.util.StringUtils.isEmpty;

/**
 * Service to validate the secret content and other properties..
 *
 * @author Suresh
 */
@Service
public class SecretContentService {

    /**
     * Max secret size in bytes.
     */
    private long maxSecretSize;

    public SecretContentService(OneOpsConfig config) {
        maxSecretSize = config.getKeywhiz().getSecretMaxSize();
    }

    /**
     * Converts a given number to a string preceded by the corresponding
     * binary International System of Units (SI) prefix.
     */
    public String binaryPrefix(long size) {
        long unit = 1000;
        String suffix = "B";

        if (size < unit) {
            return String.format("%d %s", size, suffix);
        } else {
            String prefix = "KMGTPEZY";
            int exp = (int) (Math.log(size) / Math.log(unit));
            // Binary Prefix mnemonic that is prepended to the units.
            String binPrefix = prefix.charAt(exp - 1) + suffix;
            // Count => (unit^0.x * unit^exp)/unit^exp
            return String.format("%.2f %s", size / Math.pow(unit, exp), binPrefix);
        }
    }

    /**
     * Validate and enrich {@link SecretRequest} for it's content and other metadata.
     *
     * @param req  Secret request.
     * @param name Secret name.
     * @param user OneOps user.
     * @return {@link SecretRequest}
     */
    public SecretRequest validateAndEnrich(SecretRequest req, String name, OneOpsUser user) {
        String content = req.getContent();
        if (content == null) {
            throw new IllegalArgumentException("Secret content is not provided!");
        }

        if (content.length() > maxSecretSize) {
            throw new IllegalArgumentException(String.format("Secret size (%s) is too large. Max allowed secret size is %s.",
                    binaryPrefix(content.length()), binaryPrefix(maxSecretSize)));
        }

        String base64Content;
        try {
            base64Content = Base64.getEncoder().encodeToString(content.getBytes("UTF-8"));
            req.setContent(base64Content);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Invalid secret data. UTF-8 encoding failed.");
        }

        String desc = req.getDescription();
        if (isEmpty(desc)) {
            req.setDescription("Created by " + user.getUsername());
        }

        if (req.getMetadata() == null) {
            req.setMetadata(new HashMap<>(2));
        }
        Map<String, String> metadata = req.getMetadata();
        metadata.put("userId", user.getUsername());

        return req;
    }

}
