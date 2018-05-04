/**
 * *****************************************************************************
 *
 * <p>Copyright 2017 Walmart, Inc.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * <p>*****************************************************************************
 */
package com.oneops.proxy.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import static com.oneops.proxy.config.Constants.DEFAULT_TOKEN_TYPE;

/**
 * Login response.
 *
 * @author Suresh
 */
public class LoginResponse {

  @JsonProperty private String accessToken;

  @JsonProperty private String tokenType = DEFAULT_TOKEN_TYPE;

  @JsonProperty private int expiresInSec;

  public LoginResponse(String accessToken, String tokenType, int expiresInSec) {
    this.accessToken = accessToken;
    this.tokenType = tokenType;
    this.expiresInSec = expiresInSec;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public String getTokenType() {
    return tokenType;
  }

  public void setTokenType(String tokenType) {
    this.tokenType = tokenType;
  }

  public int getExpiresInSec() {
    return expiresInSec;
  }

  public void setExpiresInSec(int expiresInSec) {
    this.expiresInSec = expiresInSec;
  }

  @Override
  public String toString() {
    return "LoginResponse{"
        + "accessToken=******"
        + ", tokenType='"
        + tokenType
        + '\''
        + ", expiresInSec="
        + expiresInSec
        + '}';
  }
}
