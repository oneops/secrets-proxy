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

import com.oneops.proxy.security.JwtTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT token authentication processing filter. The filter will check for 'X-Authorization' header.
 * and delegate authentication to {@link TokenAuthProvider}.
 *
 * @author Suresh
 */
public class TokenAuthProcessingFilter extends AbstractAuthenticationProcessingFilter {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final AuthenticationFailureHandler failureHandler;

    private final JwtTokenService jwtTokenService;

    public TokenAuthProcessingFilter(RequestMatcher requestMatcher, AuthenticationFailureHandler failureHandler, JwtTokenService jwtTokenService) {
        super(requestMatcher);
        log.info("Initializing Token auth processing filter");
        this.failureHandler = failureHandler;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res) throws AuthenticationException, IOException, ServletException {
        log.debug("Attempting token authentication.");
        JwtAuthToken jwtAuthToken = jwtTokenService.getAccessToken(req);
        if (jwtAuthToken == null) {
            throw new AuthenticationCredentialsNotFoundException("Authorization header is missing.");
        }
        return getAuthenticationManager().authenticate(jwtAuthToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        log.debug("Token authentication successful. Setting the security context.");
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authResult);
        SecurityContextHolder.setContext(context);
        // Advance the filter to call controller methods.
        chain.doFilter(req, res);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest req, HttpServletResponse res, AuthenticationException failed) throws IOException, ServletException {
        log.debug("Token authentication failed. Clearing the security holder context.", failed);
        SecurityContextHolder.clearContext();
        failureHandler.onAuthenticationFailure(req, res, failed);
    }
}
