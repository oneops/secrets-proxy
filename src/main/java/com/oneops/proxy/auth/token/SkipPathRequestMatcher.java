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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Request paths matcher configured to skip endpoints for {@link TokenAuthProcessingFilter}
 * By default it's configured to skip management endpoints.
 *
 * @author Suresh
 */
public class SkipPathRequestMatcher implements RequestMatcher {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final OrRequestMatcher matcher;

    public SkipPathRequestMatcher(@Nonnull List<String> pathsToSkip) {
        log.info("Initializing Skip path request matchers for " + pathsToSkip);
        List<RequestMatcher> pathMatchers = pathsToSkip.stream().map(AntPathRequestMatcher::new).collect(Collectors.toList());
        this.matcher = new OrRequestMatcher(pathMatchers);
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        // Skip any http requests matching pathsToSkip.
        return !matcher.matches(request);
    }
}
