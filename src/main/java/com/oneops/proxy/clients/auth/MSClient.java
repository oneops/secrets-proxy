package com.oneops.proxy.clients.auth;

import com.oneops.proxy.auth.user.OneOpsUser;
import com.oneops.proxy.clients.model.Result;
import com.oneops.proxy.clients.proxy.MSProxyClient;
import com.oneops.proxy.config.ClientsAuthConfig;
import com.oneops.proxy.model.AppGroup;
import com.oneops.proxy.model.ms.Credentials;
import com.oneops.proxy.model.ms.MSClientAuthRequest;
import com.oneops.proxy.model.ms.MSClientAuthResponse;
import com.oneops.proxy.utils.SecretsConstants;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authorization process for Managed Service client
 *
 * @author Varsha
 */
public class MSClient implements Client {

  private final Logger log = LoggerFactory.getLogger(getClass());
  private final MSProxyClient client;
  private final ClientsAuthConfig config;
  private final AppGroup appGroup;

  public MSClient(MSProxyClient client, ClientsAuthConfig serviceAuth, AppGroup appGroup) {
    this.client = client;
    this.config = serviceAuth;
    this.appGroup = appGroup;
  }

  /* Invoke Managed service Clients and call auth api.
   *
   * @param appName appName
   * @param user OneopsUser
   * @return <code>true</code> if user has admin access.
   */
  @Override
  public boolean authorizeUser(@Nonnull String appName, @Nonnull OneOpsUser user)
      throws IOException {

    /*Form domain name for managed services to access properties from ClientAuth Configuration*/
    String domainStr = SecretsConstants.MS_APP + appGroup.getDomain().getType();

    /* Get appName from AppGroup
     * Output : {org}_{asmb}_{env}
     * */
    String namespace = appGroup.getName();

    log.info(
        "Authorize MS user for domain : "
            + appGroup.getDomain().getType()
            + " and appname : "
            + namespace);

    /* Create MS client request */
    MSClientAuthRequest authRequest = createRequest(user.getUsername(), namespace);

    /* Get the configuration properties for managed services only for given domain */
    Optional<ClientsAuthConfig.ClientsAuthDomain> clientAuth =
        config.getConfigs().stream().filter(x -> x.getDomainNames().equals(domainStr)).findFirst();

    /*Invoking proxy url*/
    Result<MSClientAuthResponse> result = client.doAuth(authRequest);
    if (result.isSuccessful()) {
      return true;
    }

    return false;
  }

  /* create request payload object for Managed services auth api
   *
   * */
  private MSClientAuthRequest createRequest(String user, String namespace) {
    MSClientAuthRequest authRequest = new MSClientAuthRequest();
    Credentials cred = new Credentials();
    cred.setUser(user);

    List<Credentials> credential = new ArrayList<>();
    credential.add(cred);
    authRequest.setNamespace(namespace);
    authRequest.setCredential(credential);
    return authRequest;
  }
}
