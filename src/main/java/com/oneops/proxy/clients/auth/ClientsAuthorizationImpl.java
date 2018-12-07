package com.oneops.proxy.clients.auth;

import com.oneops.proxy.authz.UserRepository;
import com.oneops.proxy.clients.connection.MSClientService;
import com.oneops.proxy.clients.connection.TektonClientService;
import com.oneops.proxy.config.ClientsAuthConfig;
import com.oneops.proxy.model.AppGroup;
import com.oneops.proxy.utils.SecretsConstants;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * Factory invokes respective client Authorization based on appName {@link AuthorizationProcess}.
 *
 * @author Varsha
 */
@Component
public class ClientsAuthorizationImpl extends AbstractClientAuthFactory {

  private ClientsAuthConfig clientsAuthConfig;

  public ClientsAuthorizationImpl(ClientsAuthConfig clientsAuthConfig) {
    this.clientsAuthConfig = clientsAuthConfig;
  }

  @Override
  public AuthorizationProcess getAuthorization(AppGroup appGroup, UserRepository userRepo)
      throws IOException, Exception {

    if (appGroup.getName().startsWith(SecretsConstants.MS_APP)) {
      return new MSAuthorizationProcess(new MSClientService(), clientsAuthConfig, appGroup);
    } else if (appGroup.getDomain().getType().startsWith(SecretsConstants.TEKTON_APP)) {
      return new TektonAuthorizationProcess(new TektonClientService(), clientsAuthConfig, appGroup);
    } else {
      return new OneopsAuthorizationProcess(userRepo);
    }
  }
}
