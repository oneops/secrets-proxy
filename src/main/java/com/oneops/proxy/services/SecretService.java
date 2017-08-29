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
import com.oneops.proxy.keywhiz.KeywhizException;
import com.oneops.proxy.keywhiz.model.v2.CreateOrUpdateSecretRequestV2;
import com.oneops.proxy.keywhiz.model.v2.GroupDetailResponseV2;
import com.oneops.proxy.keywhiz.model.v2.SecretDetailResponseV2;
import com.oneops.proxy.model.AppSecret;
import com.oneops.proxy.model.SecretRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.oneops.proxy.model.AppSecret.*;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpStatus.PAYLOAD_TOO_LARGE;

/**
 * Service to validate and transform the secret request and responses.
 *
 * @author Suresh
 */
@Service
public class SecretService {

    /**
     * Default secret type.
     */
    public static final String DEFAULT_TYPE = "secret";

    /**
     * Max file name length.
     */
    private static final int MAX_FILENAME_LENGTH = 255;

    /**
     * Max secret size in bytes.
     */
    private long maxSecretSize;

    public SecretService(OneOpsConfig config) {
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
            return format("%d %s", size, suffix);
        } else {
            String prefix = "KMGTPEZY";
            int exp = (int) (Math.log(size) / Math.log(unit));
            // Binary Prefix mnemonic that is prepended to the units.
            String binPrefix = prefix.charAt(exp - 1) + suffix;
            // Count => (unit^0.x * unit^exp)/unit^exp
            return format("%.2f %s", size / Math.pow(unit, exp), binPrefix);
        }
    }

    /**
     * Validate and enrich {@link SecretRequest} for it's content and other metadata.
     * Keywhiz expects the secret content to be Base64 encoded string.
     *
     * @param req       Secret request.
     * @param appSecret App secret details.
     * @param user      OneOps user.
     * @return {@link SecretRequest}
     * @see <a href="https://en.wikipedia.org/wiki/Comparison_of_file_systems#Limits">File name limit</a>
     */
    public SecretRequest validateAndEnrich(SecretRequest req, AppSecret appSecret, OneOpsUser user) throws IOException {
        String content = req.getContent();
        if (isBlank(content)) {
            throw new IllegalArgumentException("Secret content is not provided!");
        }

        if (appSecret.getUniqSecretName().length() > MAX_FILENAME_LENGTH) {
            throw new IllegalArgumentException("Secret file name too long.");
        }

        if (content.length() > maxSecretSize) {
            String errMsg = format("Secret size (%s) is too large. Max allowed secret size is %s.", binaryPrefix(content.length()), binaryPrefix(maxSecretSize));
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
            req.setMetadata(new HashMap<>(3));
        }
        Map<String, String> metadata = req.getMetadata();
        validateMetadata(metadata);

        // IMP: Add secret aliases to the short name.
        metadata.put(FILENAME_METADATA, appSecret.getSecretName());
        metadata.put(USERID_METADATA, user.getUsername());
        metadata.put(DESC_METADATA, req.getDescription());

        return req;
    }

    /**
     * Perform strong validation of the metadata to make sure it is well formed.
     *
     * @param metadata secret metadata map.
     */
    private void validateMetadata(Map<String, String> metadata) {
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (!key.matches("(owner|group|mode|filename)")) {
                if (!key.startsWith("_")) {
                    throw new IllegalArgumentException(format("Illegal metadata key %s: custom metadata keys must start with an underscore", key));
                }
                if (!key.matches("^[a-zA-Z_0-9\\-.:]+$")) {
                    throw new IllegalArgumentException(format("Illegal metadata key %s: metadata keys can only contain: a-z A-Z 0-9 _ - . :", key));
                }
            }

            if (key.equals("mode") && !value.matches("0[0-7]+")) {
                throw new IllegalArgumentException(format("Mode %s is not proper octal", value));
            }
        }
    }

    /**
     * Helper method for create/update request.
     *
     * @param secret Secret request user input.
     * @return {@link CreateOrUpdateSecretRequestV2}
     */
    public CreateOrUpdateSecretRequestV2 makeCreateOrUpdateReq(SecretRequest secret) {
        return CreateOrUpdateSecretRequestV2.fromParts(secret.getContent(),
                secret.getDescription(),
                secret.getMetadata(),
                secret.getExpiry(),
                secret.getType());
    }

    /**
     * Normalize a list of unique secret names.
     *
     * @param uniqSecretNames collection of unique secret names.
     * @return list of normalized secret names.
     */
    public List<String> normalize(Collection<String> uniqSecretNames) {
        return uniqSecretNames.stream().map(uniqName -> {
            AppSecret appSecret = new AppSecret(uniqName);
            return appSecret.getSecretName();
        }).collect(Collectors.toList());
    }

    /**
     * Normalize the unique secret names in group details response.
     * ie, change globally unique keywhiz secret name to normal
     * secret name.
     *
     * @param group {@link GroupDetailResponseV2}
     * @return normalized Group details.
     */
    public GroupDetailResponseV2 normalize(GroupDetailResponseV2 group) {
        List<String> normalizedSecrets = normalize(group.secrets());
        return GroupDetailResponseV2.fromParts(group.name(),
                group.description(),
                group.createdAtSeconds(),
                group.updatedAtSeconds(),
                group.createdBy(),
                group.updatedBy(),
                normalizedSecrets,
                group.clients(),
                group.metadata());
    }

    /**
     * Normalize the unique secret names in secret response.
     *
     * @param secret {@link SecretDetailResponseV2}
     * @return normalized secret response.
     */
    public SecretDetailResponseV2 normalize(SecretDetailResponseV2 secret) {
        String normalizedSecret = new AppSecret(secret.name()).getSecretName();
        return SecretDetailResponseV2.fromParts(normalizedSecret,
                secret.description(),
                secret.checksum(),
                secret.createdAtSeconds(),
                secret.createdBy(),
                secret.updatedAtSeconds(),
                secret.updatedBy(),
                secret.metadata(),
                secret.type(),
                secret.expiry(),
                secret.version());
    }

}
