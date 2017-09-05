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
package com.oneops.proxy.config;

import com.oneops.proxy.model.AppGroup;
import com.oneops.proxy.web.support.*;
import org.slf4j.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.*;

/**
 * ContentNegotiation configuration for the application.
 * <p>
 * Note: If you add ({@link EnableWebMvc}) annotation, you switch off
 * everything in spring boot.
 *
 * @author Suresh
 */
@Configuration
//@EnableWebMvc
public class WebConfig extends WebMvcConfigurerAdapter {


    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        log.info("Configuring ContentNegotiation for the application.");
        configurer.favorPathExtension(false).
                favorParameter(true).
                parameterName("mediaType").
                ignoreAcceptHeader(true).
                useJaf(false).
                defaultContentType(APPLICATION_JSON_UTF8).
                mediaType("xml", APPLICATION_XML).
                mediaType("json", APPLICATION_JSON_UTF8);
    }

    /**
     * Turn on path variable suffix pattern matching ONLY for suffixes you explicitly
     * register using {@link #configureContentNegotiation(ContentNegotiationConfigurer)}.
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer matcher) {
        log.info("Configuring Suffix PathMatching for the application.");
        matcher.setUseRegisteredSuffixPatternMatch(true);
    }

    /**
     * {@link AppGroup} argument resolver used in Rest controllers.
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argResolvers) {
        log.info("Configuring AppGroup arg resolver for the application.");
        argResolvers.add(new AppGroupArgResolver());
        argResolvers.add(new AppSecretArgResolver());
    }
}
