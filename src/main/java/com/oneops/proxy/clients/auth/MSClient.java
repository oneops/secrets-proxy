package com.oneops.proxy.clients.auth;

import com.oneops.proxy.auth.user.OneOpsUser;
import com.oneops.proxy.clients.model.Result;
import com.oneops.proxy.clients.proxy.MSProxyClient;
import com.oneops.proxy.model.ms.Credentials;
import com.oneops.proxy.model.ms.MSClientAuthRequest;
import com.oneops.proxy.model.ms.MSClientAuthResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

  private String namespace;
  private MSProxyClient msProxyClient;

  public MSClient(String namespace, MSProxyClient msProxyClient) {
    this.namespace = namespace;
    this.msProxyClient = msProxyClient;
  }

  /**
   * Invoke Managed service Clients and call auth api.
   *
   * @param appName appName
   * @param user OneopsUser
   * @return <code>true</code> if user has admin access.
   */
  @Override
  public boolean authorizeUser(@Nonnull String appName, @Nonnull OneOpsUser user)
      throws IOException {

    log.info("MS Client services : authorization started");

    /* Create MS client request */
    MSClientAuthRequest authRequest = createRequest(user.getUsername(), namespace);

    /*Invoking proxy url*/
    Result<MSClientAuthResponse> result = msProxyClient.doAuth(authRequest);
    if (result != null && result.isSuccessful()) {
      return true;
    }
    return false;
  }

  /** create request payload object for Managed services auth api */
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
