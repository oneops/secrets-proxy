package com.oneops.proxy.clients.auth;

import com.oneops.proxy.auth.user.OneOpsUser;
import com.oneops.proxy.authz.UserRepository;
import com.oneops.proxy.model.AppGroup;
import org.springframework.stereotype.Component;

/**
 * Abstract factory to get client Authorization {@link Client}.
 *
 * @author Varsha
 */
@Component
public interface ClientFactoryInterface {
  Client selectClient(AppGroup appGroup, String appName, OneOpsUser user, UserRepository userRepo)
      throws Exception;
}
