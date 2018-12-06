package com.oneops.proxy.clients.auth;

import com.oneops.proxy.auth.user.OneOpsUser;
import com.oneops.proxy.authz.OneOpsTeam;
import com.oneops.proxy.authz.UserRepository;
import com.oneops.proxy.model.AppGroup;
import javax.annotation.Nonnull;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.stereotype.Component;
import java.util.List;
import static com.oneops.proxy.authz.OneOpsTeam.SECRETS_ADMIN_TEAM;

/**
 * Authorization provides for default application that is Oneops
 *
 * @author Varsha
 */

@Component
public class OneopsAuthorizationProcess implements AuthorizationProcess {

    private final UserRepository userRepo;

  public OneopsAuthorizationProcess(UserRepository userRepo)  {
      this.userRepo = userRepo;
  }

  /* Check in UserRepository if user has authorized access for oneops applicaton.
   *
   * @param appName appName
   * @param user OneopsUser
   * @return <code>true</code> if user has admin access.
   */
  @Override
  public boolean authorizeUser(@Nonnull String appName, @Nonnull OneOpsUser user) {

    AppGroup appGroup = new AppGroup(user.getDomain(), appName);

    List<OneOpsTeam> teams = userRepo.getTeams(user.getUsername(), appGroup);
    boolean hasAccess = teams.stream().anyMatch(team -> hasAdminAccess(team, appGroup));
    if (!hasAccess) {
      throw new AuthorizationServiceException(
          "OneOps user '"
              + user.getCn()
              + "' is not a '"
              + SECRETS_ADMIN_TEAM
              + "' or not authorized to manage the secrets for environment: "
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
