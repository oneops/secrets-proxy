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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

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

    @Bean
    @Lazy
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http.sessionManagement()
                  .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                    .authorizeRequests()
                    .antMatchers(GET, "/").permitAll()
                    .antMatchers(GET,  mgmtContext + "/info").permitAll()
                    .antMatchers(GET, mgmtContext + "/health").permitAll()
                    .antMatchers(GET,mgmtContext + "/**").hasRole("ADMIN")
                    .antMatchers(POST, "/login").permitAll()
                    .anyRequest().fullyAuthenticated()
                .and()
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
        // For static resources.
        web.ignoring().antMatchers(GET, "/assets/**");
    }
}
