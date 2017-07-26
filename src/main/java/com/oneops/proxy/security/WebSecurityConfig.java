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
package com.oneops.proxy.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneops.proxy.auth.login.LoginAuthProvider;
import com.oneops.proxy.auth.login.LoginProcessingFilter;
import com.oneops.proxy.auth.token.SkipPathRequestMatcher;
import com.oneops.proxy.auth.token.TokenAuthProcessingFilter;
import com.oneops.proxy.auth.token.TokenAuthProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static com.oneops.proxy.auth.user.OneOpsUser.Role.MGMT;
import static com.oneops.proxy.config.Constants.AUTH_TOKEN_URI;
import static java.util.Collections.singletonList;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

/**
 * Web security configurer for the application.
 *
 * @author Suresh
 */
@EnableWebSecurity
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Value("${management.context-path}")
    private String mgmtContext;

    private final LoginAuthProvider loginAuthProvider;
    private final TokenAuthProvider tokenAuthProvider;
    private final RestAuthEntryPoint authEntryPoint;
    private final JwtTokenService jwtTokenService;
    private final ObjectMapper objectMapper;
    private final AuthenticationSuccessHandler successHandler;
    private final AuthenticationFailureHandler failureHandler;


    @Autowired
    public WebSecurityConfig(LoginAuthProvider loginAuthProvider,
                             TokenAuthProvider tokenAuthProvider,
                             AuthenticationSuccessHandler successHandler,
                             AuthenticationFailureHandler failureHandler,
                             RestAuthEntryPoint authEntryPoint,
                             JwtTokenService jwtTokenService,
                             ObjectMapper objectMapper) {
        this.loginAuthProvider = loginAuthProvider;
        this.tokenAuthProvider = tokenAuthProvider;
        this.authEntryPoint = authEntryPoint;
        this.jwtTokenService = jwtTokenService;
        this.objectMapper = objectMapper;
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;
    }

    @Bean
    @Lazy
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }


    /**
     * The method configures 3 authentication providers, namely
     * <p>
     * 1. {@link LoginAuthProvider} - Only for login requests
     * 2. {@link TokenAuthProvider} - For all requests except management endpoints
     * 3. InMemoryAuthentication -  For management endpoints and user/password auth fallback.
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        log.info("Configuring user authentication providers.");
        auth.inMemoryAuthentication().withUser("ooadmin").password("0n30ps").roles(MGMT.name());
        auth.authenticationProvider(loginAuthProvider);
        auth.authenticationProvider(tokenAuthProvider);
    }

    /**
     * Builds  {@link LoginProcessingFilter} with the current {@link AuthenticationManager}
     *
     * @return {@link LoginProcessingFilter}
     * @throws Exception
     */
    private LoginProcessingFilter buildLoginProcessingFilter() throws Exception {
        LoginProcessingFilter loginFilter = new LoginProcessingFilter(AUTH_TOKEN_URI, successHandler, failureHandler, objectMapper);
        loginFilter.setAuthenticationManager(authenticationManager());
        return loginFilter;
    }

    /**
     * Builds {@link TokenAuthProcessingFilter} with the current {@link AuthenticationManager}.
     * The filter won't be apply for <b>management</b> request paths.
     *
     * @return Token authentication filter
     * @throws Exception
     */
    private TokenAuthProcessingFilter buildAuthProcessingFilter() throws Exception {
        SkipPathRequestMatcher requestMatcher = new SkipPathRequestMatcher(singletonList(mgmtContext + "/**"));
        TokenAuthProcessingFilter authFilter = new TokenAuthProcessingFilter(requestMatcher, failureHandler, jwtTokenService);
        authFilter.setAuthenticationManager(authenticationManager());
        return authFilter;
    }

    /**
     * Configures two filters namely, login and auth filters in the same order.
     * {@link LoginProcessingFilter} is for all the login (/auth/token) requests and
     * {@link TokenAuthProcessingFilter} is for other requests to check the presence
     * of JWT in header.
     *
     * @param http http security
     * @throws Exception throws if any error configuring Web security.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http.exceptionHandling().authenticationEntryPoint(authEntryPoint)
                .and()
                  .sessionManagement().sessionCreationPolicy(STATELESS)
                .and()
                  .requiresChannel().anyRequest().requiresSecure()
                .and()
                    .authorizeRequests()
                    .antMatchers(GET, "/").permitAll()
                    .antMatchers(GET,  mgmtContext + "/info").permitAll()
                    .antMatchers(GET, mgmtContext + "/health").permitAll()
                    .antMatchers(GET,mgmtContext + "/**").hasAnyRole(MGMT.name())
                    .anyRequest().fullyAuthenticated()
                .and()
                    .addFilterBefore(buildLoginProcessingFilter(),UsernamePasswordAuthenticationFilter.class)
                    .addFilterBefore(buildAuthProcessingFilter(),UsernamePasswordAuthenticationFilter.class)
                    .httpBasic()
                .and()
                   .formLogin().disable()//.loginPage("/login")
                   .logout().disable()
                   .csrf().disable()
                   .cors()
                .and()
                   .headers().httpStrictTransportSecurity()
                .and()
                   .frameOptions().disable();
        // @formatter:on
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .antMatchers(GET, "/assets/**")
                .antMatchers(GET, "/static/**"); // Static resources.
    }

    /**
     * Cross-Origin Resource Sharing (CORS) configuration for all the
     * cross-domain REST API calls.
     *
     * @return cors filter bean
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(singletonList("*"));
        config.setAllowedHeaders(singletonList("*"));
        config.setAllowedMethods(Arrays.asList("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
