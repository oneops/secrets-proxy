package com.oneops.proxy.clients.auth;

import com.oneops.proxy.auth.user.OneOpsUser;
import javax.annotation.Nonnull;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * Interface provides methods to clients for authorize and normalizePath
 *
 * @author Varsha
 */
@Component
public interface AuthorizationProcess {

  /* {@link boolean} method authorizeUser with arugument user and appName */
  boolean authorizeUser(@Nonnull String appName, @Nonnull OneOpsUser user)
      throws IOException, Exception;
}
