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
package com.oneops.proxy.auth.token;

import com.oneops.proxy.auth.user.OneOpsUser;
import com.oneops.proxy.security.JwtTokenService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

/**
 * A custom {@link AuthenticationProvider} to validate JWT auth tokens.
 * The token authentication provider has the following responsibilities,
 * <p>
 * 1. Verify and validate the access token signature.
 * 2. Create {@link OneOpsUser} by extracting identity and auth claims from token.
 * 3. Throws Authentication exception if the token is invalid, malformed or expired.
 * <p>
 * {@link AuthenticationFailureHandler} is invoked upon failed authentication.
 *
 * @author Suresh G
 */
@Component
public class TokenAuthProvider implements AuthenticationProvider {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final JwtTokenService jwtTokenService;

    public TokenAuthProvider(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        log.debug("Validating and authenticating the token.");
        JwtAuthToken authToken = (JwtAuthToken) authentication;
        String jwtToken = (String) authToken.getCredentials();
        authToken.eraseCredentials(); // Now we can erase the credentials.

        OneOpsUser user;
        try {
            user = jwtTokenService.createUser(jwtToken);
        } catch (ExpiredJwtException ex) {
            throw new CredentialsExpiredException("Token has expired.", ex);
        } catch (JwtException ex) {
            throw new BadCredentialsException("Invalid Authorization Token.", ex);
        }

        // Check for authorize claims.
        if (user.getAuthorities().isEmpty()) {
            throw new InsufficientAuthenticationException(user.getUsername() + " user has no roles assigned.");
        }
        return new JwtAuthToken(user, null, user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthToken.class.isAssignableFrom(authentication);
    }
}
