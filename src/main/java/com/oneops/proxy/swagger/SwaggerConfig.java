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
package com.oneops.proxy.swagger;

import com.oneops.proxy.auth.user.OneOpsUser;
import com.oneops.proxy.config.OneOpsConfig;
import com.oneops.proxy.web.RootController;
import org.slf4j.*;
import org.springframework.context.annotation.*;
import springfox.documentation.builders.*;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.*;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.List;

import static com.oneops.proxy.config.Constants.API_VERSION;
import static java.util.Collections.singletonList;
import static springfox.documentation.builders.PathSelectors.regex;
import static springfox.documentation.builders.RequestHandlerSelectors.basePackage;
import static springfox.documentation.spi.DocumentationType.SWAGGER_2;

/**
 * Swagger2 configuration for the application.
 *
 * @author Suresh
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Controllers API doc. Note: Error controllers are excluded for brevity.
     *
     * @return {@link Docket}
     */
    @Bean
    public Docket api(OneOpsConfig config) {
        log.info("Configuring OneOps Secret API documentation.");
        List<Parameter> headers = singletonList(new ParameterBuilder()
                .name(config.getAuth().getHeader())
                .description("Authorization token header")
                .modelRef(new ModelRef("string"))
                .parameterType("header")
                .defaultValue("Bearer &ltAuth Token&gt")
                .required(true)
                .build());

        return new Docket(SWAGGER_2)
                .groupName("secrets")
                .ignoredParameterTypes(OneOpsUser.class)
                .globalOperationParameters(headers)
                .apiInfo(apiInfo())
                .select()
                .apis(basePackage(RootController.class.getPackage().getName()))
                .paths(regex("^(?!/error).*$")) // Ignore all '/error' handlers.
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("OneOps Secrets API")
                .description("OneOps Secrets API Documentation.")
                .contact(new Contact("OneOps", "https://oneops.github.com/secrets-proxy", "oneops@oneops.com"))
                .license("Apache License 2.0")
                .licenseUrl("https://github.com/oneops/secrets-proxy/blob/master/LICENSE")
                .termsOfServiceUrl("https://github.com/oneops/secrets-proxy/blob/master/.github/CONTRIBUTING.md")
                .version(API_VERSION)
                .build();
    }

    /**
     * Swagger UI config.
     *
     * @see <a href="https://goo.gl/ZXzchg">UiConfiguration</a>
     */
    @Bean
    public UiConfiguration uiConfig() {
        return new UiConfiguration(
                null,              // url
                "list",          // docExpansion          => none | list | full
                "alpha",            // apiSorter             => alpha
                "schema", // defaultModelRendering => schema
                UiConfiguration.Constants.DEFAULT_SUBMIT_METHODS,
                false,        // enableJsonEditor      => true | false
                true, // showRequestHeaders    => true | false
                60000L);  // requestTimeout => in milliseconds, defaults to null (uses jquery xh timeout)
    }
}
