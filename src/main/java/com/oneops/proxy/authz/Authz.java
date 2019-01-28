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
package com.oneops.proxy.authz;

import static com.oneops.proxy.authz.OneOpsTeam.SECRETS_ADMIN_TEAM;

import com.oneops.proxy.auth.user.OneOpsUser;
import com.oneops.proxy.clients.auth.Client;
import com.oneops.proxy.clients.auth.ClientSelector;
import com.oneops.proxy.model.AppGroup;
import javax.annotation.Nonnull;
import org.slf4j.*;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.stereotype.Component;

/**
 * Implements Keywhiz application group authorization logic. The application group name is the env
 * nspath, which is of the format <b>{org}_{assembly}_{env}</b>
 *
 * @author Suresh
 */
@Component
public class Authz {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private final UserRepository userRepo;
  private ClientSelector clientSelector;

  public Authz(UserRepository userRepo, ClientSelector clientSelector) {
    this.userRepo = userRepo;
    this.clientSelector = clientSelector;
  }

  /**
   * Checks if {@link OneOpsUser} is authorized to manage secrets for the given application group.
   * Env nspath is used as the application group with the <b>{org}_{assembly}_{env}</b> format.
   *
   * @param appName Application name.
   * @param user Authenticated user.
   * @return <code>true</code> if the user is authorized.
   */
  public boolean isAuthorized(@Nonnull String appName, @Nonnull OneOpsUser user) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug(
          "Checking the authz for user: "
              + user.getUsername()
              + ", domain: "
              + user.getDomain()
              + " and application: "
              + appName);
    }
    AppGroup appGroup = new AppGroup(user.getDomain(), appName);
    Client client = clientSelector.selectClient(appGroup, userRepo);
    if (client != null && client.authorizeUser(appName, user)) {
      log.info("Authorization process is done for user " + user.getUsername());
    } else {
      throw new AuthorizationServiceException(
          "User '"
              + user.getCn()
              + "' is not a '"
              + SECRETS_ADMIN_TEAM
              + "' or not authorized to manage the secrets for environment: "
              + appGroup.getName());
    }
    return true;
  }

  /**
   * Checks if the given team is a 'secret-admin' and has access to manage design and transition for
   * the given application group.
   *
   * @param team OneOps team
   * @param appGroup Application group
   * @return <code>true</code> if the team has admin access.
   */
  public boolean hasAdminAccess(OneOpsTeam team, AppGroup appGroup) {
    return team.isSecretsAdmin(appGroup.getAssembly()) && team.isDesign() && team.isTransition();
  }
}
