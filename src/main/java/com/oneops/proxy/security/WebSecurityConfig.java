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

import static com.oneops.proxy.auth.user.OneOpsUser.Role.MGMT;
import static com.oneops.proxy.config.Constants.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneops.proxy.auth.login.*;
import com.oneops.proxy.auth.token.*;
import com.oneops.proxy.config.OneOpsConfig;
import java.util.*;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.*;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.*;
import org.springframework.web.cors.*;

/**
 * Web security configurer for the application.
 *
 * @author Suresh
 */
@EnableWebSecurity
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  private final Logger log = LoggerFactory.getLogger(getClass());

  /** Application management context & credentials. */
  private String mgmtContext;

  private String mgmtUser;

  private String mgmtPasswd;

  private OneOpsConfig oneOpsConfig;

  /** Paths to be skipped from token authentication. */
  private List<String> tokenAuthSkipPaths;

  /** Permit all these paths without any authentication. */
  private String[] permitAllPaths;

  private final LoginAuthProvider loginAuthProvider;
  private final TokenAuthProvider tokenAuthProvider;
  private final RestAuthEntryPoint authEntryPoint;
  private final JwtTokenService jwtTokenService;
  private final ObjectMapper objectMapper;
  private final AuthenticationSuccessHandler successHandler;
  private final AuthenticationFailureHandler failureHandler;

  public WebSecurityConfig(
      LoginAuthProvider loginAuthProvider,
      TokenAuthProvider tokenAuthProvider,
      AuthenticationSuccessHandler successHandler,
      AuthenticationFailureHandler failureHandler,
      RestAuthEntryPoint authEntryPoint,
      JwtTokenService jwtTokenService,
      ObjectMapper objectMapper,
      OneOpsConfig oneOpsConfig,
      @Value("${management.context-path}") String mgmtContext,
      @Value("${management.user}") String mgmtUser,
      @Value("${management.password}") String mgmtPasswd) {
    this.mgmtContext = mgmtContext;
    this.mgmtUser = mgmtUser;
    this.mgmtPasswd = mgmtPasswd;
    this.loginAuthProvider = loginAuthProvider;
    this.tokenAuthProvider = tokenAuthProvider;
    this.authEntryPoint = authEntryPoint;
    this.jwtTokenService = jwtTokenService;
    this.objectMapper = objectMapper;
    this.oneOpsConfig = oneOpsConfig;
    this.successHandler = successHandler;
    this.failureHandler = failureHandler;
    this.permitAllPaths = ArrayUtils.addAll(DEFAULT_SKIP_PATHS, mgmtContext + "/health");
    this.tokenAuthSkipPaths =
        Arrays.asList(ArrayUtils.addAll(DEFAULT_SKIP_PATHS, mgmtContext + "/**"));
  }

  @Bean
  @Lazy
  public BCryptPasswordEncoder bCryptPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * The method configures 3 authentication providers, namely
   *
   * <p>1. {@link LoginAuthProvider} - Only for login requests 2. {@link TokenAuthProvider} - For
   * all requests except management endpoints 3. InMemoryAuthentication - For management endpoints
   * and user/password auth fallback.
   */
  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    log.info("Configuring user authentication providers.");
    auth.inMemoryAuthentication().withUser(mgmtUser).password(mgmtPasswd).roles(MGMT.name());
    auth.authenticationProvider(loginAuthProvider);
    auth.authenticationProvider(tokenAuthProvider);
  }

  /**
   * Builds {@link LoginProcessingFilter} with the current {@link AuthenticationManager}
   *
   * @return {@link LoginProcessingFilter}
   * @throws Exception
   */
  private LoginProcessingFilter buildLoginProcessingFilter() throws Exception {
    LoginProcessingFilter loginFilter =
        new LoginProcessingFilter(AUTH_TOKEN_URI, successHandler, failureHandler, objectMapper);
    loginFilter.setAuthenticationManager(authenticationManager());
    return loginFilter;
  }

  /**
   * Builds {@link TokenAuthProcessingFilter} with the current {@link AuthenticationManager}. The
   * filter won't be apply for {@link #tokenAuthSkipPaths} request paths.
   *
   * @return Token authentication filter
   * @throws Exception
   */
  private TokenAuthProcessingFilter buildAuthProcessingFilter() throws Exception {
    log.info("Configured to skip " + tokenAuthSkipPaths + " path from TokenAuthProcessingFilter.");
    SkipPathRequestMatcher requestMatcher = new SkipPathRequestMatcher(tokenAuthSkipPaths);
    TokenAuthProcessingFilter authFilter =
        new TokenAuthProcessingFilter(requestMatcher, failureHandler, jwtTokenService);
    authFilter.setAuthenticationManager(authenticationManager());
    return authFilter;
  }

  /**
   * Secrets cli version filter.
   *
   * @return {@link CliVersionFilter}
   */
  private CliVersionFilter buildCliVersionFilter() {
    log.info("Configuring secrets cli version filter.");
    return new CliVersionFilter(oneOpsConfig);
  }

  /**
   * Configures two filters namely, login and auth filters in the same order. {@link
   * LoginProcessingFilter} is for all the login (/auth/token) requests and {@link
   * TokenAuthProcessingFilter} is for other requests to check the presence of JWT in header.
   *
   * @param http http security
   * @throws Exception throws if any error configuring Web security.
   */
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    // @formatter:off
    http.exceptionHandling()
        .authenticationEntryPoint(authEntryPoint)
        .and()
        .sessionManagement()
        .sessionCreationPolicy(STATELESS)
        .and()
        .requiresChannel()
        .anyRequest()
        .requiresSecure()
        .and()
        .authorizeRequests()
        .mvcMatchers(GET, permitAllPaths)
        .permitAll()
        .mvcMatchers(GET, mgmtContext + "/**")
        .hasAnyRole(MGMT.name())
        // .mvcMatchers("/auth/{userId}").access("@authz.isAuthorized(#userId,principal)")
        .anyRequest()
        .fullyAuthenticated()
        .and()
        .addFilterBefore(buildCliVersionFilter(), UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(buildLoginProcessingFilter(), UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(buildAuthProcessingFilter(), UsernamePasswordAuthenticationFilter.class)
        .httpBasic()
        .and()
        .formLogin()
        .disable() // .loginPage("/login")
        .logout()
        .disable()
        .csrf()
        .disable()
        .cors()
        .and()
        .headers()
        .httpStrictTransportSecurity()
        .and()
        .frameOptions()
        .deny()
        .cacheControl()
        .and()
        .xssProtection()
        .xssProtectionEnabled(true);
    // @formatter:on
  }

  @Override
  public void configure(WebSecurity web) throws Exception {
    web.ignoring()
        .antMatchers(GET, "/assets/**")
        .antMatchers(GET, "/static/**"); // Static resources.
  }

  /**
   * Cross-Origin Resource Sharing (CORS) configuration for all the cross-domain REST API calls.
   * Its' applied to all request paths (<b>/**</b>).
   *
   * @return cors filter bean
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.setAllowedOrigins(singletonList("*"));
    config.setAllowedHeaders(singletonList("*"));
    config.setAllowedMethods(asList("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
