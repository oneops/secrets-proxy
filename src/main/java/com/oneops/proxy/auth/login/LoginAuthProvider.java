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
package com.oneops.proxy.auth.login;

import com.oneops.proxy.auth.user.LdapUserService;
import com.oneops.proxy.auth.user.OneOpsUser;
import com.oneops.proxy.authz.AuthDomain;
import org.ldaptive.LdapException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * A custom {@link AuthenticationProvider} to validate user credentials against OneOps database or
 * AD/LDAP. {@link AuthenticationSuccessHandler} is invoked upon successful authentication.
 *
 * @author Suresh
 */
@Component
public class LoginAuthProvider implements AuthenticationProvider {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private LdapUserService ldapUserService;

  public LoginAuthProvider(LdapUserService ldapUserService) {
    this.ldapUserService = ldapUserService;
  }

  /**
   * Performs authentication.
   *
   * @param auth UsernamePasswordAuthenticationToken.
   * @return authenticated object.
   * @throws AuthenticationException if authentication fails.
   */
  @Override
  public Authentication authenticate(Authentication auth) throws AuthenticationException {
    Assert.notNull(auth, "No authentication data provided.");
    String userName = (String) auth.getPrincipal();
    String password = (String) auth.getCredentials();
    AuthDomain domain = (AuthDomain) auth.getDetails();

    OneOpsUser user = null;
    try {
      user = ldapUserService.authenticate(userName, password.toCharArray(), domain);
    } catch (LdapException ex) {
      log.debug("Ldap Authentication failed for user: " + userName, ex);
    }

    if (user == null) {
      throw new BadCredentialsException("Invalid Username/Password.");
    }

    // Check for user privileges.
    if (user.getAuthorities().isEmpty()) {
      throw new InsufficientAuthenticationException(
          user.getUsername() + " user has no roles assigned.");
    }

    return new LoginAuthToken(user, null, user.getAuthorities());
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return LoginAuthToken.class.isAssignableFrom(authentication);
  }
}
