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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Proxy authentication controller.
 *
 * @author Suresh G
 */
@RestController
public class AuthController {

    @PostMapping(path = "/login")
    public LoginResponse login(@RequestBody LoginRequest creds) {
        return LoginResponse.of("xxxx", "Bearer", 3600);
    }


    @PostMapping(path = "/logout")
    public String logout() {
        return "Logout Successful.";
    }
}
