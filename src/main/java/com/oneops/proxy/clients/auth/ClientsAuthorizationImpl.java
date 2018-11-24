package com.oneops.proxy.clients.auth;

import com.oneops.proxy.authz.UserRepository;
import com.oneops.proxy.clients.connection.TektonClientService;
import com.oneops.proxy.config.OneOpsConfig;
import com.oneops.proxy.model.AppGroup;
import org.springframework.stereotype.Component;

/**
 * Factory Class which invokes respective client Authorization based on appName {@link AuthorizationProcess}.
 *
 * @author Varsha
 */

@Component
public class ClientsAuthorizationImpl extends AbstractClientAuthFactory {

  @Override
  public AuthorizationProcess getAuthorization(AppGroup appGroup,  OneOpsConfig config, UserRepository userRepo) throws Exception {

    if(appGroup.getOrg().startsWith("ms")) {
      return new MSAuthorizationProcess();
    }else if (appGroup.getSysName().equalsIgnoreCase("tekton")) {
        return new TektonAuthorizationProcess(new TektonClientService(config));
    }else{
        return new OneopsAuthorizationProcess(userRepo);
    }
  }
}
