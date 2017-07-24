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
package com.oneops.proxy.auth;

import com.oneops.proxy.ldap.LDAPClient;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static java.util.Collections.singletonList;

/**
 * User service to authenticate OneOps keywhiz proxy. Internally it
 * uses the AD/LDAP directory service for user authentication.
 * <p>
 * Note: Right now it doesn't uses Spring security LDAP, as the current
 * implementation ({@link LDAPClient}) is good enough and does support
 * caching and connection pooling.
 *
 * @author Suresh
 */
//@Service
public class OneOpsUserDetailsService implements UserDetailsService {

    private LDAPClient adAuthClient;

    public OneOpsUserDetailsService(LDAPClient adAuthClient) {
        this.adAuthClient = adAuthClient;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new User(username, "", singletonList(new SimpleGrantedAuthority("user")));
    }
}
