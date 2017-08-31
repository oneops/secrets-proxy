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
package com.oneops.proxy.auth.login;

import com.oneops.proxy.auth.token.TokenAuthProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Invoked by {@link LoginAuthProvider} or {@link TokenAuthProvider} in case of
 * authentication failures. Need to send proper JSON error responses in future.
 *
 * @author Suresh G
 */
@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest req, HttpServletResponse res, AuthenticationException exception) throws IOException, ServletException {
        res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed: " + exception.getMessage());
    }
}
