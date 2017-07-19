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
import com.oneops.proxy.auth.JWTAuthFilter;
import com.oneops.proxy.auth.JWTAuthService;
import com.oneops.proxy.auth.JWTLoginFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static com.oneops.proxy.web.EndPoints.AUTH_TOKEN;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

/**
 * Web security configurer for the application.
 *
 * @author Suresh
 * @see <a href="https://goo.gl/pSsmpy">Spring-boot-sample-web-security</a>
 */
@EnableWebSecurity
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private static final Logger log = LoggerFactory.getLogger(WebSecurityConfig.class);

    @Value("${management.context-path}")
    private String mgmtContext;

    private final JWTAuthService jwtAuthService;

    private final ObjectMapper objectMapper;

    @Autowired
    public WebSecurityConfig(JWTAuthService jwtAuthService, ObjectMapper objectMapper) {
        this.jwtAuthService = jwtAuthService;
        this.objectMapper = objectMapper;
    }

    @Bean
    @Lazy
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http.sessionManagement()
                  .sessionCreationPolicy(STATELESS)
                .and()
                  .requiresChannel().anyRequest().requiresSecure()
                .and()
                    .authorizeRequests()
                    .antMatchers(GET, "/").permitAll()
                    .antMatchers(POST, AUTH_TOKEN).permitAll()
                    .antMatchers(GET, AUTH_TOKEN).denyAll()
                    .antMatchers(GET,  mgmtContext + "/info").permitAll()
                    .antMatchers(GET, mgmtContext + "/health").permitAll()
                    .antMatchers(GET,mgmtContext + "/**").hasRole("ADMIN")
                    .anyRequest().fullyAuthenticated()
                .and()
                    .addFilterBefore(new JWTLoginFilter(AUTH_TOKEN, authenticationManager(),jwtAuthService,objectMapper),UsernamePasswordAuthenticationFilter.class)
                    .addFilterBefore(new JWTAuthFilter(jwtAuthService),UsernamePasswordAuthenticationFilter.class)
                    .httpBasic()
                .and()
                   .formLogin()//.loginPage("/login")
                .and()
                   .logout().disable()
                   .csrf().disable()
                   .cors()
                .and()
                   .headers()
                   .frameOptions().disable();
        // @formatter:on
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(GET, "/assets/**"); // Static resources.
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        log.info("Configuring application authentication manager.");
        auth.inMemoryAuthentication()
                .withUser("oouser")
                .password("oouser")
                .roles("USER", "ADMIN");
    }
}
