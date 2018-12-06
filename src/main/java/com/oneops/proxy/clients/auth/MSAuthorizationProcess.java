package com.oneops.proxy.clients.auth;

import com.oneops.proxy.auth.user.OneOpsUser;
import javax.annotation.Nonnull;

import com.oneops.proxy.clients.connection.MSClientService;
import com.oneops.proxy.config.ClientsAuthConfig;
import com.oneops.proxy.model.AppGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Authorization provides for Managed Services
 *
 * @author Varsha
 */

public class MSAuthorizationProcess implements AuthorizationProcess {

  private final Logger log = LoggerFactory.getLogger(getClass());
  private final MSClientService msClient;
  private final ClientsAuthConfig serviceAuth;
  private final AppGroup appGroup;

  public MSAuthorizationProcess(MSClientService msClient, ClientsAuthConfig serviceAuth, AppGroup appGroup){
      this.msClient = msClient;
      this.serviceAuth = serviceAuth;
      this.appGroup = appGroup;
  }

  /* Invoke Managed service Clients and call auth api.
  *
  * @param appName appName
  * @param user OneopsUser
  * @return <code>true</code> if user has admin access.
  */
  @Override
  public boolean authorizeUser(@Nonnull String appName, @Nonnull OneOpsUser user)  throws IOException, Exception {
      return msClient.auth(user, appName, serviceAuth, appGroup);
  }

}
