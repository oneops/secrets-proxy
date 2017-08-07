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
package com.oneops.proxy.config;

/**
 * Constants used in the application.
 *
 * @author Suresh
 */
public interface Constants {

    /**
     * Current Rest API version.
     */
    String API_VERSION = "v1";

    /**
     * {@link com.oneops.proxy.web.AuthController} base path URI.
     */
    String AUTH_CTLR_BASE_PATH = "/" + API_VERSION + "/auth";

    /**
     * Authentication token path URI.
     */
    String AUTH_TOKEN_URI = AUTH_CTLR_BASE_PATH + "/token";

    /**
     * {@link com.oneops.proxy.web.GroupController} base path URI.
     */
    String GROUP_CTLR_BASE_PATH = "/" + API_VERSION + "/apps";

    /**
     * Default mgmt domain for keywhiz requests.
     */
    String DEFAULT_DOMAIN = "prod";

    /**
     * Default access token type (Eg: Bearer, JWT etc)
     */
    String DEFAULT_TOKEN_TYPE = "Bearer";
}
