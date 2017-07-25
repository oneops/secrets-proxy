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
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import sun.security.x509.X500Name;

import javax.annotation.Nullable;

import java.io.IOException;

import static com.oneops.proxy.auth.user.OneOpsUser.Role.USER;
import static java.util.Collections.singletonList;

/**
 * A service to authenticate users using the {@link LdapClient}
 *
 * @author Suresh
 */
@Service
public class LdapUserService {

    private static final Logger log = LoggerFactory.getLogger(LdapUserService.class);

    private LdapClient ldapClient;

    public LdapUserService(LdapClient ldapClient) {
        this.ldapClient = ldapClient;
    }

    /**
     * Authenticates the username and password using the LDAP/AD service. By default all the
     * users authenticated will have <b>USER</b> role. We might change this in future depending
     * on the attribute info available in the LDAP entries.
     *
     * @param userName ldap username
     * @param password ldap password
     * @param domain   mgmt domain.
     * @return {@link OneOpsUser} details object if successfully authenticated, else returns <code>null</code>.
     * @throws LdapException throws if any error authenticating/connecting to ldap server.
     */
    public @Nullable
    OneOpsUser authenticate(String userName, char[] password, String domain) throws LdapException {
        LdapEntry ldapUser = ldapClient.authenticate(userName, password);
        if (ldapUser != null) {
            String cn = getCommonName(ldapUser, userName);
            return new OneOpsUser(userName, String.valueOf(password), singletonList(new SimpleGrantedAuthority(USER.authority())), cn, domain);
        }
        return null;
    }

    /**
     * Returns the common name from LDAP entry.
     *
     * @param ldapUser    ldap entry
     * @param defaultName default name if there is no cn.
     * @return common name.
     */
    private String getCommonName(LdapEntry ldapUser, String defaultName) {
        String cn;
        try {
            cn = new X500Name(ldapUser.getDn()).getCommonName();
        } catch (IOException e) {
            cn = defaultName;
        }
        return cn;
    }
}
