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

import com.google.common.collect.ImmutableMap;
import com.oneops.proxy.auth.user.OneOpsUser;
import com.oneops.proxy.config.OneOpsConfig;
import com.oneops.proxy.keywhiz.KeywhizException;
import com.oneops.proxy.keywhiz.model.v2.CreateOrUpdateSecretRequestV2;
import com.oneops.proxy.keywhiz.model.v2.PartialUpdateSecretRequestV2;
import com.oneops.proxy.model.SecretRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpStatus.PAYLOAD_TOO_LARGE;

/**
 * Service to validate the secret content and other properties..
 *
 * @author Suresh
 */
@Service
public class SecretContentService {

    /**
     * Default secret type.
     */
    public static final String DEFAULT_TYPE = "secret";

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
     * Keywhiz expects the secret content to be Base64 encoded string.
     *
     * @param req  Secret request.
     * @param name Secret name.
     * @param user OneOps user.
     * @return {@link SecretRequest}
     */
    public SecretRequest validateAndEnrich(SecretRequest req, String name, OneOpsUser user) throws IOException {
        String content = req.getContent();
        if (isBlank(content)) {
            throw new IllegalArgumentException("Secret content is not provided!");
        }

        if (content.length() > maxSecretSize) {
            String errMsg = String.format("Secret size (%s) is too large. Max allowed secret size is %s.", binaryPrefix(content.length()), binaryPrefix(maxSecretSize));
            throw new KeywhizException(PAYLOAD_TOO_LARGE.value(), errMsg);
        }

        String desc = req.getDescription();
        if (desc == null) {
            req.setDescription("");
        }

        String type = req.getType();
        if (type == null) {
            req.setType(DEFAULT_TYPE);
        }

        if (req.getMetadata() == null) {
            req.setMetadata(new HashMap<>(2));
        }
        Map<String, String> metadata = req.getMetadata();
        metadata.put("userId", user.getUsername());
        metadata.put("versionDesc", req.getDescription());

        return req;
    }

    /**
     * Helper method to create Partial update request.
     *
     * @param secretRequest Secret request user input.
     * @return {@link PartialUpdateSecretRequestV2}
     */
    public PartialUpdateSecretRequestV2 getPartialUpdateReq(SecretRequest secretRequest) {
        PartialUpdateSecretRequestV2.Builder builder = PartialUpdateSecretRequestV2.builder();
        builder.contentPresent(true).content(secretRequest.getContent());
        builder.descriptionPresent(true).description(secretRequest.getDescription());
        builder.expiryPresent(true).expiry(secretRequest.getExpiry());
        builder.typePresent(true).type(secretRequest.getType());
        builder.metadataPresent(true).metadata(ImmutableMap.copyOf(secretRequest.getMetadata()));
        return builder.build();
    }

    /**
     * Helper method for create/update request.
     *
     * @param secret Secret request user input.
     * @return {@link CreateOrUpdateSecretRequestV2}
     */
    public CreateOrUpdateSecretRequestV2 getCreateUpdateReq(SecretRequest secret) {
        return CreateOrUpdateSecretRequestV2.fromParts(secret.getContent(),
                secret.getDescription(),
                secret.getMetadata(),
                secret.getExpiry(),
                secret.getType());
    }
}
