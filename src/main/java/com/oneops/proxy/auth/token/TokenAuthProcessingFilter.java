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
package com.oneops.proxy.auth.token;

import com.oneops.proxy.security.JwtTokenService;
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.*;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.*;
import org.springframework.security.core.context.*;
import org.springframework.security.web.authentication.*;
import org.springframework.security.web.util.matcher.RequestMatcher;

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

  public TokenAuthProcessingFilter(
      RequestMatcher requestMatcher,
      AuthenticationFailureHandler failureHandler,
      JwtTokenService jwtTokenService) {
    super(requestMatcher);
    log.info("Initializing Token auth processing filter");
    this.failureHandler = failureHandler;
    this.jwtTokenService = jwtTokenService;
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res)
      throws AuthenticationException, IOException, ServletException {
    log.debug("Attempting token authentication.");
    JwtAuthToken jwtAuthToken = jwtTokenService.getAccessToken(req);
    if (jwtAuthToken == null) {
      throw new AuthenticationCredentialsNotFoundException("Authorization header is missing.");
    }
    return getAuthenticationManager().authenticate(jwtAuthToken);
  }

  @Override
  protected void successfulAuthentication(
      HttpServletRequest req, HttpServletResponse res, FilterChain chain, Authentication authResult)
      throws IOException, ServletException {
    log.debug("Token authentication successful. Setting the security context.");
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authResult);
    SecurityContextHolder.setContext(context);
    // Advance the filter to call controller methods.
    chain.doFilter(req, res);
  }

  @Override
  protected void unsuccessfulAuthentication(
      HttpServletRequest req, HttpServletResponse res, AuthenticationException failed)
      throws IOException, ServletException {
    log.debug("Token authentication failed. Clearing the security holder context.", failed);
    SecurityContextHolder.clearContext();
    failureHandler.onAuthenticationFailure(req, res, failed);
  }
}
