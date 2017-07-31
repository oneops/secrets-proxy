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
package com.oneops.proxy.web;

import com.oneops.proxy.auth.user.OneOpsUser;
import com.oneops.proxy.security.annotations.CurrentUser;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A rest controller to retrieve authenticated user details.
 *
 * @author Suresh G
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    /**
     * Returns the authenticated current user info.
     *
     * @param user {@link OneOpsUser}
     * @return OneOps user details.
     */
    @GetMapping("/user")
    public OneOpsUser user(@CurrentUser OneOpsUser user) {
        return user;
    }

    /**
     * Returns the current CSRF token details.
     *
     * @param token injected csrf token
     * @return csrf token.
     */
    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }
}
