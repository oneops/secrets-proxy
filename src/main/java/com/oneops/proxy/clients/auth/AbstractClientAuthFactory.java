package com.oneops.proxy.clients.auth;

import com.oneops.proxy.authz.UserRepository;
import com.oneops.proxy.config.OneOpsConfig;
import com.oneops.proxy.model.AppGroup;

/**
 * Abstract factory to get client Authorization {@link AuthorizationProcess}.
 *
 * @author Varsha
 */

public abstract class AbstractClientAuthFactory {
  public abstract AuthorizationProcess getAuthorization(AppGroup appGroup, UserRepository userRepo) throws Exception;
}
