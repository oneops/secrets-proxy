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
package com.oneops.proxy.security;

import static com.oneops.proxy.config.Constants.APP_NAME;
import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.stereotype.Component;

/**
 * Authentication entry point bean to commences an authentication scheme on {@link
 * ExceptionTranslationFilter}.
 *
 * @author Suresh
 */
@Component
public class RestAuthEntryPoint implements AuthenticationEntryPoint {

  @Override
  public void commence(
      HttpServletRequest req, HttpServletResponse res, AuthenticationException authException)
      throws IOException, ServletException {
    res.addHeader(WWW_AUTHENTICATE, "Basic realm=\"" + APP_NAME + "\"");
    res.sendError(UNAUTHORIZED.value(), authException.getMessage());
  }
}
