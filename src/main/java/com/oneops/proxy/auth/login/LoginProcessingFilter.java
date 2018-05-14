/**
 * *****************************************************************************
 *
 * <p>Copyright 2017 Walmart, Inc.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * <p>*****************************************************************************
 */
package com.oneops.proxy.auth.login;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.util.StringUtils.isEmpty;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneops.proxy.model.LoginRequest;
import java.io.IOException;
import java.util.Collections;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.*;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.*;

/**
 * Login processing filter. De-serialization and basic validation of the incoming JSON {@link
 * LoginRequest} payload is done here. Upon successful validation, the authentication logic is
 * delegated to {@link LoginAuthProvider}.
 *
 * @author Suresh
 */
public class LoginProcessingFilter extends AbstractAuthenticationProcessingFilter {

  private final Logger log = LoggerFactory.getLogger(getClass());
  private final AuthenticationSuccessHandler successHandler;
  private final AuthenticationFailureHandler failureHandler;
  private final ObjectMapper mapper;

  public LoginProcessingFilter(
      String loginUrl,
      AuthenticationSuccessHandler successHandler,
      AuthenticationFailureHandler failureHandler,
      ObjectMapper mapper) {
    super(loginUrl);
    log.info("Initializing Login processing filter for " + loginUrl);
    this.successHandler = successHandler;
    this.failureHandler = failureHandler;
    this.mapper = mapper;
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res)
      throws AuthenticationException, IOException {
    log.debug("Attempting login authentication.");
    LoginRequest loginReq = getLoginRequest(req, res);

    LoginAuthToken auth =
        new LoginAuthToken(loginReq.getUsername(), loginReq.getPassword(), Collections.emptyList());
    auth.setDetails(loginReq.getDomain());
    return getAuthenticationManager().authenticate(auth);
  }

  /**
   * Helper method to validate and create the {@link LoginRequest}
   *
   * @param req http request
   * @param res http response
   * @return {@link LoginRequest}
   * @throws IOException
   */
  private LoginRequest getLoginRequest(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    String httpMethod = req.getMethod();
    if (!POST.name().equalsIgnoreCase(httpMethod)) {
      String resMsg =
          String.format("Authentication method not supported. Request method: %s", httpMethod);
      res.sendError(METHOD_NOT_ALLOWED.value(), resMsg);
      throw new AuthenticationServiceException(resMsg);
    }

    LoginRequest loginReq;
    try {
      loginReq = mapper.readValue(req.getReader(), LoginRequest.class);
    } catch (Exception ioe) {
      String errMsg = "Bad token request.";
      res.sendError(BAD_REQUEST.value(), errMsg);
      throw new AuthenticationServiceException(errMsg, ioe);
    }

    if (isEmpty(loginReq.getUsername()) || isEmpty(loginReq.getPassword())) {
      String errMsg = "Username or Password not provided.";
      res.sendError(BAD_REQUEST.value(), errMsg);
      throw new AuthenticationServiceException(errMsg);
    }
    return loginReq;
  }

  @Override
  protected void successfulAuthentication(
      HttpServletRequest req, HttpServletResponse res, FilterChain chain, Authentication authResult)
      throws IOException, ServletException {
    successHandler.onAuthenticationSuccess(req, res, authResult);
  }

  @Override
  protected void unsuccessfulAuthentication(
      HttpServletRequest req, HttpServletResponse res, AuthenticationException failed)
      throws IOException, ServletException {
    log.debug("Login Authentication failed. Clearing the security holder context", failed);
    SecurityContextHolder.clearContext();
    failureHandler.onAuthenticationFailure(req, res, failed);
  }
}
