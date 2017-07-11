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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

/**
 * Login response.
 *
 * @author Suresh
 */
@AutoValue
public abstract class LoginResponse {

    @JsonProperty
    public abstract String accessToken();

    @JsonProperty
    public abstract String tokenType();

    @JsonProperty
    public abstract int expiresInSec();

    @JsonCreator
    public static LoginResponse of(@JsonProperty("accessToken") String accessToken,
                                   @JsonProperty("tokenType") String tokenType,
                                   @JsonProperty("expiresInSec") int expiresInSec) {
        return new AutoValue_LoginResponse(accessToken, tokenType, expiresInSec);
    }
}
