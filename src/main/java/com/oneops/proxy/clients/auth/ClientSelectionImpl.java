package com.oneops.proxy.clients.auth;

import com.oneops.proxy.auth.user.OneOpsUser;
import com.oneops.proxy.authz.UserRepository;
import com.oneops.proxy.clients.proxy.MSProxyClient;
import com.oneops.proxy.clients.proxy.TektonProxyClient;
import com.oneops.proxy.config.ClientsAuthConfig;
import com.oneops.proxy.model.AppGroup;
import com.oneops.proxy.utils.SecretsConstants;
import java.security.GeneralSecurityException;
import org.springframework.stereotype.Component;

/**
 * Factory invokes respective client Authorization based on appName {@link Client}.
 *
 * @author Varsha
 */
@Component
public class ClientSelectionImpl implements ClientFactoryInterface {

  private ClientsAuthConfig clientsAuthConfig;

  public ClientSelectionImpl(ClientsAuthConfig clientsAuthConfig) {
    this.clientsAuthConfig = clientsAuthConfig;
  }

  @Override
  public Client selectClient(
      AppGroup appGroup, String appName, OneOpsUser user, UserRepository userRepo)
      throws GeneralSecurityException {
    if (appGroup.getName().startsWith(SecretsConstants.MS_APP)) {
      return new MSClient(
          new MSProxyClient(clientsAuthConfig, appGroup), clientsAuthConfig, appGroup);
    } else if (appGroup.getDomain().getType().startsWith(SecretsConstants.TEKTON_APP)) {
      return new TektonClient(
          new TektonProxyClient(clientsAuthConfig, appGroup), clientsAuthConfig, appGroup);
    } else {
      return new OneopsClient(userRepo);
    }
  }
}
