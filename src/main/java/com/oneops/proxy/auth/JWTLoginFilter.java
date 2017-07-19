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
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * Login authentication filter.
 *
 * @author Suresh
 */
public class JWTLoginFilter extends AbstractAuthenticationProcessingFilter {

    private static final Logger log = LoggerFactory.getLogger(JWTLoginFilter.class);

    private final ObjectMapper mapper;

    private final JWTAuthService authService;

    /**
     * Creates new JWT filter for the specific url using the given ${@link AuthenticationManager}
     *
     * @param url         filter processing url.
     * @param authMgr     authentication manager.
     * @param authService ${@link JWTAuthService}
     * @param mapper      Json object mapper.
     */
    public JWTLoginFilter(String url, AuthenticationManager authMgr, JWTAuthService authService, ObjectMapper mapper) {
        super(url);
        log.info("Initializing JWT Login Filter for " + url);
        this.mapper = mapper;
        this.authService = authService;
        setAuthenticationManager(authMgr);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res) throws AuthenticationException, IOException, ServletException {
        LoginRequest userCreds = getLoginRequest(req, res);
        return getAuthenticationManager().authenticate(new UsernamePasswordAuthenticationToken(userCreds.getUsername(), userCreds.getPassword(), Collections.emptyList()));
    }

    /**
     * Helper method to validate and read the ${@link LoginRequest}. The login
     * reques to get token should be a <b>POST</b> call.
     *
     * @param req http request
     * @param res http response
     * @return {@link LoginRequest}
     * @throws IOException
     * @throws ServletException
     */
    private LoginRequest getLoginRequest(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {

        String httpMethod = req.getMethod();
        if (!HttpMethod.POST.name().equalsIgnoreCase(httpMethod)) {
            res.sendError(BAD_REQUEST.value(), "Invalid HTTP method. Use POST.");
            throw new HttpRequestMethodNotSupportedException(httpMethod, new String[]{HttpMethod.POST.name()});
        }

        LoginRequest userCreds;
        try {
            userCreds = mapper.readValue(req.getReader(), LoginRequest.class);
        } catch (IOException ioe) {
            res.sendError(BAD_REQUEST.value(), "Bad login request");
            throw ioe;
        }
        return userCreds;
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        // Add token to response header
        authService.addAuthentication(res, authResult);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest req, HttpServletResponse res, AuthenticationException failed) throws IOException, ServletException {
        super.unsuccessfulAuthentication(req, res, failed);
    }

}
