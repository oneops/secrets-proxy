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

import com.oneops.proxy.model.LoginRequest;
import com.oneops.proxy.model.LoginResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

/**
 * Proxy authentication controller.
 *
 * @author Suresh G
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @GetMapping(path = "/")
    public String root() {
        return "OneOps Keywhiz Proxy!";
    }

    // @PostAuthorize("returnObject?.accessToken() == principal.username")
    @PostMapping(path = "/token")
    public LoginResponse login(LoginRequest creds) {
        return LoginResponse.of("xxxx", "Bearer", 3600);
    }

    @PostMapping(path = "/invalidate")
    public String logout() {
        return "Logout Successful.";
    }

    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }
}
