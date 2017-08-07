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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Secret version request for set version.
 *
 * @author Suresh G
 */
public class SecretVersionRequest {

    @JsonProperty("version")
    private long version;

    public SecretVersionRequest() {
    }

    public SecretVersionRequest(long version) {
        this.version = version;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "SecretVersionRequest{" +
                "version=" + version +
                '}';
    }
}
