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
import com.oneops.proxy.model.*;
import com.oneops.proxy.security.annotations.CurrentUser;
import io.swagger.annotations.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.oneops.proxy.config.Constants.AUTH_CTLR_BASE_PATH;

/**
 * A rest controller to retrieve authenticated user details.
 *
 * @author Suresh G
 */
@RestController
@RequestMapping(AUTH_CTLR_BASE_PATH)
@Api(value = "Auth EndPoint", description = "User Authentication.")
public class AuthController {

    /**
     * Returns the authenticated current user info.
     *
     * @param user {@link OneOpsUser}
     * @return OneOps user details.
     */
    @GetMapping("/user")
    @ApiOperation(value = "Authenticated User Info", notes = "Token user details.")
    public UserResponse user(@CurrentUser OneOpsUser user) {
        List<String> roles = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        return new UserResponse(user.getUsername(), user.getCn(), user.getDomain(), roles);
    }


    /**
     * The token mapping is provided only for generating swagger documentation
     * and shouldn't be called in any case. The actual token authentication is
     * done using the {@link com.oneops.proxy.auth.token.TokenAuthProcessingFilter}.
     *
     * @param loginRequest Login request
     * @return Login response.
     */
    @PostMapping("/token")
    @ApiOperation(value = "Generate Access Token", notes = "Use this Bearer token for all other requests.")
    public LoginResponse token(@RequestBody LoginRequest loginRequest) {
        throw new IllegalStateException("Token method shouldn't be called. This is just for api doc.");
    }
}
