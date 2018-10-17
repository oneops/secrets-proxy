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

import com.oneops.proxy.auth.user.OneOpsUser;
import com.oneops.proxy.config.OneOpsConfig;
import com.oneops.proxy.model.AppGroup;
import java.io.IOException;
import javax.annotation.Nonnull;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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
  private String authenticationURL;
  private String authorizationCode;
  //private final UserRepository userRepo;

//  public Authz(UserRepository userRepo) {
//    this.userRepo = userRepo;
//  }


  public Authz(OneOpsConfig config) {
    this.authenticationURL = config.getAuthenticationURL();
    this.authorizationCode = config.getAuthorizationCode();
  }

  /**
   * Checks if {@link OneOpsUser} is authorized to manage secrets for the given application group.
   * Env nspath is used as the application group with the <b>{org}_{assembly}_{env}</b> format.
   *
   * @param appName Application name.
   * @param user Authenticated user.
   * @return <code>true</code> if the user is authorized.
   */
  public boolean isAuthorized(@Nonnull String appName, @Nonnull OneOpsUser user) {
    if (log.isDebugEnabled()) {
      log.debug(
          "Checking the authz for user: " + user.getUsername() + " and application: " + appName);
    }

    AppGroup appGroup = new AppGroup(user.getDomain(), appName);
   // List<OneOpsTeam> teams = userRepo.getTeams(user.getUsername(), appGroup);
    OkHttpClient client = new OkHttpClient();
    String url = authenticationURL + "/" + user.getUsername() + "/" + appName.replace("_", "/");

    Request request = new Request.Builder()
        .url(url)
        .addHeader("Authorization", authorizationCode)
        .build();
    try {
      Response response = client.newCall(request).execute();
      log.debug(url+ "->" +response.code()+":"+response.message());

      if (!response.isSuccessful()){
        throw new AuthorizationServiceException(
            "User '"
                + user.getUsername()
                + "' is not authorized to manage the secrets for environment: "
                + appGroup.getNsPath());
      }
    } catch (IOException e) {
      log.warn(e.getMessage());
      throw new AuthorizationServiceException(
          "Error accessing authorization service: "
              + appGroup.getNsPath());
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
