package com.oneops.proxy.clients.auth;

import com.oneops.proxy.auth.user.OneOpsUser;
import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.annotation.Nonnull;
import org.springframework.stereotype.Component;

/**
 * Interface provides methods to clients for authorize
 *
 * @author Varsha
 */
@Component
public interface Client {

  /**
   * @param appName applicationName
   * @param user OneOps User {@link boolean} method authorizeUser with argument user and appName
   */
  boolean authorizeUser(@Nonnull String appName, @Nonnull OneOpsUser user)
      throws IOException, GeneralSecurityException;
}
