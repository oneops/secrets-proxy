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
package com.oneops.proxy.web.support;

import static com.oneops.proxy.model.AppGroup.APP_NAME_PARAM;
import static com.oneops.proxy.model.AppSecret.APP_SECRET_PARAM;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;
import static org.springframework.web.servlet.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE;

import com.oneops.proxy.auth.user.OneOpsUser;
import com.oneops.proxy.config.WebConfig;
import com.oneops.proxy.model.*;
import java.util.*;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.*;

/**
 * {@link AppSecret} method argument resolver for rest controllers. It's configured in {@link
 * WebConfig#addArgumentResolvers(List)}.
 *
 * @author Suresh G
 */
public class AppSecretArgResolver implements HandlerMethodArgumentResolver {

  @Override
  public boolean supportsParameter(MethodParameter param) {
    return param.getParameterType().equals(AppSecret.class);
  }

  /**
   * Construct new {@link AppSecret} object from the given Web request. The authenticated user is
   * available in the web request principal.
   */
  @Override
  public Object resolveArgument(
      MethodParameter parameter,
      ModelAndViewContainer mavContainer,
      NativeWebRequest webReq,
      WebDataBinderFactory binderFactory)
      throws Exception {
    Authentication auth = (Authentication) webReq.getUserPrincipal();
    OneOpsUser user = (OneOpsUser) auth.getPrincipal();

    // Get the path variable.
    @SuppressWarnings("unchecked")
    Map<String, String> pathVars =
        (Map<String, String>) webReq.getAttribute(URI_TEMPLATE_VARIABLES_ATTRIBUTE, SCOPE_REQUEST);
    String group = pathVars != null ? pathVars.get(APP_NAME_PARAM) : "";
    String secret = pathVars != null ? pathVars.get(APP_SECRET_PARAM) : "";

    AppGroup appGroup = new AppGroup(user.getDomain(), group);
    return new AppSecret(secret, appGroup);
  }
}
