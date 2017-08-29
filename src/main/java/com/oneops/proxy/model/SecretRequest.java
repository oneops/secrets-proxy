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
package com.oneops.proxy.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Keywhiz secret request.
 *
 * @author Suresh
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SecretRequest {

    @JsonProperty
    private String content;

    @JsonProperty
    private String description;

    @JsonProperty
    private Map<String, String> metadata;

    /**
     * Secret expiry. For keystores, it is recommended to use the expiry of the earliest key.
     * Format should be 2006-01-02T15:04:05Z or seconds since epoch.
     */
    @JsonProperty
    private long expiry;

    @JsonProperty
    private String type;

    public SecretRequest() {
    }

    public SecretRequest(String content, String description, Map<String, String> metadata, long expiry, String type) {
        this.content = content;
        this.description = description;
        this.metadata = metadata;
        this.expiry = expiry;
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public long getExpiry() {
        return expiry;
    }

    public void setExpiry(long expiry) {
        this.expiry = expiry;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "SecretRequest{" +
                "content=******" +
                ", description='" + description + '\'' +
                ", metadata=" + metadata +
                ", expiry=" + expiry +
                ", type='" + type + '\'' +
                '}';
    }
}
