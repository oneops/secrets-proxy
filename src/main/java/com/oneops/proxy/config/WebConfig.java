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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_XML;

/**
 * ContentNegotiation configuration for the application.
 *
 * @author Suresh
 */
@Configuration
@EnableWebMvc
public class WebConfig extends WebMvcConfigurerAdapter {

    private static final Logger log = LoggerFactory.getLogger(WebConfig.class);

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        log.info("Configuring ContentNegotiation for the application.");
        configurer.favorPathExtension(false).
                favorParameter(true).
                parameterName("mediaType").
                ignoreAcceptHeader(true).
                useJaf(false).
                defaultContentType(APPLICATION_JSON).
                mediaType("xml", APPLICATION_XML).
                mediaType("json", APPLICATION_JSON);
    }
}
