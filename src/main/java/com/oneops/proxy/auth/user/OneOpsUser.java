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
package com.oneops.proxy.auth.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.oneops.proxy.authz.AuthDomain;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * OneOps user details. The user credential (<b>password</b>) is ignored from JSON serialization in
 * case it's used as Rest controller response.
 *
 * @author Suresh
 */
@JsonIgnoreProperties("password")
public class OneOpsUser extends User {

  private final String cn;

  private final AuthDomain domain;

  /**
   * Creates a new OneOps user from the {@link UserDetails} object.
   *
   * @param user user details.
   */
  public OneOpsUser(User user) {
    this(
        user.getUsername(),
        user.getPassword(),
        user.getAuthorities(),
        user.getUsername(),
        AuthDomain.PROD);
  }

  public OneOpsUser(
      String username,
      String password,
      Collection<? extends GrantedAuthority> authorities,
      String cn,
      AuthDomain domain) {
    super(username, password, authorities);
    this.cn = cn;
    this.domain = domain;
  }

  /** Returns OneOps mgmt domain. */
  public AuthDomain getDomain() {
    return domain;
  }

  /** Returns the OneOps user common name. Usually it's your full name. */
  public String getCn() {
    return cn;
  }

  /**
   * Checks whether the user has given role.
   *
   * @param role {@link Role}
   * @return <code>true</code> if the user has given authority.
   */
  public boolean hasRole(Role role) {
    return getAuthorities()
        .stream()
        .anyMatch(a -> a.getAuthority().equalsIgnoreCase(role.authority()));
  }

  /** OneOps user roles. */
  public enum Role {
    USER,
    ADMIN,
    MGMT;

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
