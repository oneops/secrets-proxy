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
package com.oneops.proxy.auth.user;

import com.oneops.proxy.ldap.LdapClient;
import org.ldaptive.LdapException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import sun.security.x509.X500Name;

import java.io.IOException;
import java.util.List;

import static com.oneops.proxy.auth.user.OneOpsUser.Role.USER;
import static com.oneops.proxy.config.Constants.DEFAULT_DOMAIN;
import static java.util.Collections.singletonList;

/**
 * For loading OneOps user-specific data from database or LDAP. This is used
 * by the default {@link AuthenticationProvider} to get the {@link UserDetails}.
 * <p>
 * Note: This service is created during the initial phase of development and is
 * not used anywhere in the authentication process.
 *
 * @author Suresh
 */
//@Service
public class LdapUserDetailsService implements UserDetailsService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private LdapClient ldapClient;

    public LdapUserDetailsService(LdapClient ldapClient) {
        this.ldapClient = ldapClient;
    }

    @Override
    public OneOpsUser loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            List<X500Name> x500Names = ldapClient.searchUser(username);
            if (x500Names.size() == 0) {
                throw new UsernameNotFoundException("Can't load the user details for " + username);
            }

            X500Name x500Name = x500Names.get(0);
            return new OneOpsUser(username,
                    null,
                    singletonList(new SimpleGrantedAuthority(USER.authority())),
                    x500Name.getCommonName(), DEFAULT_DOMAIN);

        } catch (IOException | LdapException e) {
            throw new UsernameNotFoundException("Can't load the user details for " + username, e);
        }
    }
}
