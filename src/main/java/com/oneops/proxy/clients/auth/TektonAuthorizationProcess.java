package com.oneops.proxy.clients.auth;

import com.oneops.proxy.auth.user.OneOpsUser;
import com.oneops.proxy.clients.connection.TektonClientService;
import com.oneops.proxy.config.ClientsAuthConfig;
import com.oneops.proxy.model.AppGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Authorization provides for Tekton services.
 *
 * @author Varsha
 */
public class TektonAuthorizationProcess implements AuthorizationProcess {

  private final Logger log = LoggerFactory.getLogger(getClass());
  private final TektonClientService tektonClient;
  private final ClientsAuthConfig serviceAuth;
  private final AppGroup appGroup;

  public TektonAuthorizationProcess(
      TektonClientService tektonClient, ClientsAuthConfig serviceAuth, AppGroup appGroup) {
    this.tektonClient = tektonClient;
    this.serviceAuth = serviceAuth;
    this.appGroup = appGroup;
  }

  /* Invoke Tekton service Clients and call auth api.
   *
   * @param appName appName
   * @param user OneopsUser
   * @return <code>true</code> if user has admin access.
   */

  @Override
  public boolean authorizeUser(@Nonnull String appName, @Nonnull OneOpsUser user)
      throws IOException {
    return tektonClient.auth(user, appName, serviceAuth, appGroup);
  }
}
