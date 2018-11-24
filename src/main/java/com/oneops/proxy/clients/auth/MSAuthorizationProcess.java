package com.oneops.proxy.clients.auth;

import com.oneops.proxy.auth.user.OneOpsUser;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Authorization provides for Managed Servlet
 *
 * @author Varsha
 */

@Component
public class MSAuthorizationProcess implements AuthorizationProcess {

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Override
  public boolean authorizeUser(@Nonnull String appName, @Nonnull OneOpsUser user) {

    System.out.println(appName);
    return true;
  }

}
