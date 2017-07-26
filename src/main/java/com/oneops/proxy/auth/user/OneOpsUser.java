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

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

import static com.oneops.proxy.config.Constants.DEFAULT_DOMAIN;

/**
 * OneOps user details.
 *
 * @author Suresh
 */
public class OneOpsUser extends User {

    /**
     * Common name.
     */
    private final String cn;

    /**
     * OneOps management domain.
     */
    private final String domain;

    /**
     * Creates a new OneOps user from the {@link UserDetails} object.
     *
     * @param user user details.
     */
    public OneOpsUser(User user) {
        this(user.getUsername(), user.getPassword(), user.getAuthorities(), user.getUsername(), DEFAULT_DOMAIN);
    }

    public OneOpsUser(String username, String password, Collection<? extends GrantedAuthority> authorities, String cn, String domain) {
        super(username, password, authorities);
        this.cn = cn;
        this.domain = domain;
    }

    public String getDomain() {
        return domain;
    }

    public String getCn() {
        return cn;
    }

    /**
     * OneOps user roles.
     */
    public enum Role {

        USER, ADMIN, MGMT;

        /**
         * Authorities for this role name.
         *
         * @return role name prefixed with "ROLE_"
         */
        public String authority() {
            return "ROLE_" + name();
        }
    }
}
