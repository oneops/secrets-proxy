package com.oneops.proxy.clients.auth;

import com.oneops.proxy.auth.user.OneOpsUser;
import java.io.IOException;
import javax.annotation.Nonnull;
import org.springframework.stereotype.Component;

/**
 * Interface provides methods to clients for authorize and normalizePath
 *
 * @author Varsha
 */
@Component
public interface Client {

  /* {@link boolean} method authorizeUser with arugument user and appName */
  boolean authorizeUser(@Nonnull String appName, @Nonnull OneOpsUser user)
      throws IOException;
}
