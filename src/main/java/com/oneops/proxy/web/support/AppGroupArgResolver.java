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
package com.oneops.proxy.web.support;

import com.oneops.proxy.auth.user.OneOpsUser;
import com.oneops.proxy.config.WebConfig;
import com.oneops.proxy.model.AppGroup;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.*;

import java.util.*;

import static com.oneops.proxy.model.AppGroup.APP_NAME_PARAM;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;
import static org.springframework.web.servlet.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE;

/**
 * {@link AppGroup} method argument resolver for rest controllers.
 * It's configured in {@link WebConfig#addArgumentResolvers(List)}.
 *
 * @author Suresh G
 */
public class AppGroupArgResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter param) {
        return param.getParameterType().equals(AppGroup.class);
    }

    /**
     * Construct new {@link AppGroup} object from the given Web request.
     * The authenticated user is available in the web request principal.
     */
    @Override
    public AppGroup resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webReq, WebDataBinderFactory binderFactory) throws Exception {
        Authentication auth = (Authentication) webReq.getUserPrincipal();
        OneOpsUser user = (OneOpsUser) auth.getPrincipal();

        // Get the path variable.
        @SuppressWarnings("unchecked")
        Map<String, String> pathVars = (Map<String, String>) webReq.getAttribute(URI_TEMPLATE_VARIABLES_ATTRIBUTE, SCOPE_REQUEST);
        String group = pathVars != null ? pathVars.get(APP_NAME_PARAM) : "";
        return AppGroup.from(user.getDomain(), group);
    }
}
