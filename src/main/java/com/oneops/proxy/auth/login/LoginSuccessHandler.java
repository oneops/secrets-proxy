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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneops.proxy.audit.AuditLog;
import com.oneops.proxy.audit.Event;
import com.oneops.proxy.auth.user.OneOpsUser;
import com.oneops.proxy.model.LoginResponse;
import com.oneops.proxy.security.JwtTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static com.oneops.proxy.audit.EventTag.GENERATE_TOKEN;
import static com.oneops.proxy.config.Constants.DEFAULT_DOMAIN;

/**
 * A login success handle, invoked by {@link LoginAuthProvider} when
 * the user is successfully authenticated. This class is responsible
 * for returning {@link LoginResponse} with <b>access_token</b> on
 * successful login.
 *
 * @author Suresh G
 */
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final ObjectMapper mapper;
    private final JwtTokenService jwtTokenService;
    private final AuditLog auditLog;

    public LoginSuccessHandler(ObjectMapper mapper, JwtTokenService jwtTokenService, AuditLog auditLog) {
        this.mapper = mapper;
        this.jwtTokenService = jwtTokenService;
        this.auditLog = auditLog;
    }

    /**
     * Since we are using multiple {@link AuthenticationProvider}s, make sure to
     * convert the authentication principal to proper {@link OneOpsUser} type.
     *
     * @param req            http request.
     * @param res            http response.
     * @param authentication authentication object
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res, Authentication authentication) throws IOException, ServletException {
        User principal = (User) authentication.getPrincipal();
        OneOpsUser user;
        if (principal instanceof OneOpsUser) {
            user = (OneOpsUser) principal;
        } else {
            user = getOneOpsUser(principal);
        }

        String token = jwtTokenService.generateToken(user);
        auditLog.log(new Event(GENERATE_TOKEN, user.getUsername(), ""));

        LoginResponse loginResponse = new LoginResponse(token, jwtTokenService.getTokenType(), jwtTokenService.getExpiresInSec());
        res.setStatus(HttpStatus.OK.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(res.getWriter(), loginResponse);

        clearAuthenticationAttributes(req);
    }

    /**
     * Helper method to create {@link OneOpsUser} for authentication principal.
     *
     * @param principal authentication principal
     * @return oneops user.
     */
    private OneOpsUser getOneOpsUser(User principal) {
        log.debug("Found user details in authentication. Creating OneOps User.");
        String userName = principal.getUsername();
        String password = principal.getPassword();

        if (password == null) {
            log.debug(userName + " credentials are already erased.");
            password = "";
        }
        return new OneOpsUser(userName, password, principal.getAuthorities(), userName, DEFAULT_DOMAIN);
    }

    /**
     * Removes any temporary authentication-related data which may have been
     * stored in the session during the authentication process.
     *
     * @param request http request.
     */
    private void clearAuthenticationAttributes(HttpServletRequest request) {
        // Don't create new session.
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    }
}
