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
package com.oneops.proxy.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneops.proxy.model.LoginRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.util.StringUtils.isEmpty;

/**
 * Login processing filter. De-serialization and basic validation of
 * the incoming JSON {@link LoginRequest} payload is done here. Upon
 * successful validation, the authentication logic is delegated to
 * {@link UserAuthProvider}.
 *
 * @author Suresh
 */
public class LoginProcessingFilter extends AbstractAuthenticationProcessingFilter {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final AuthenticationSuccessHandler successHandler;
    private final AuthenticationFailureHandler failureHandler;
    private final ObjectMapper mapper;

    public LoginProcessingFilter(String loginUrl, AuthenticationSuccessHandler successHandler, AuthenticationFailureHandler failureHandler, ObjectMapper mapper) {
        super(loginUrl);
        log.info("Initializing Login processing filter for " + loginUrl);
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;
        this.mapper = mapper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res) throws AuthenticationException, IOException, ServletException {
        String httpMethod = req.getMethod();
        if (!POST.name().equalsIgnoreCase(httpMethod)) {
            String resMsg = String.format("Authentication method not supported. Request method: %s", httpMethod);
            res.sendError(METHOD_NOT_ALLOWED.value(), resMsg);
            throw new HttpRequestMethodNotSupportedException(httpMethod, new String[]{POST.name()}, resMsg);
        }

        LoginRequest loginReq;
        try {
            loginReq = mapper.readValue(req.getReader(), LoginRequest.class);
        } catch (IOException ioe) {
            String errMsg = "Bad login request json!";
            res.sendError(BAD_REQUEST.value(), errMsg);
            if (log.isDebugEnabled()) {
                log.debug(errMsg, ioe);
            }
            throw ioe;
        }

        if (isEmpty(loginReq.getUsername()) || isEmpty(loginReq.getPassword())) {
            String errMsg = "Username or Password not provided.";
            res.sendError(BAD_REQUEST.value(), errMsg);
            throw new AuthenticationServiceException(errMsg);
        }

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(loginReq.getUsername(), loginReq.getPassword(), Collections.emptyList());
        auth.setDetails(loginReq.getDomain());
        return getAuthenticationManager().authenticate(auth);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        successHandler.onAuthenticationSuccess(req, res, authResult);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest req, HttpServletResponse res, AuthenticationException failed) throws IOException, ServletException {
        log.debug("Login authentication failed. Clearing the security holder context.");
        SecurityContextHolder.clearContext();
        failureHandler.onAuthenticationFailure(req, res, failed);
    }
}
