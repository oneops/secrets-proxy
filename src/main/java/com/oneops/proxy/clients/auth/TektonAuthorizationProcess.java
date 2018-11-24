package com.oneops.proxy.clients.auth;

import com.oneops.proxy.auth.user.OneOpsUser;
import com.oneops.proxy.clients.connection.TektonClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Authorization provides for Tekton
 *
 * @author Varsha
 */

public class TektonAuthorizationProcess implements AuthorizationProcess {

  private final Logger log = LoggerFactory.getLogger(getClass());
  private final TektonClientService tektonClient;

  public TektonAuthorizationProcess(TektonClientService tektonClient){
      this.tektonClient = tektonClient;
  }

  @Override
  public boolean authorizeUser(@Nonnull String appName, @Nonnull OneOpsUser user) throws IOException {
    //  boolean isAuthorized = tektonClient.auth(user,appName).isSuccessful();
      return true;
  }

}
