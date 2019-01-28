package com.oneops.proxy.clients.auth;

import com.oneops.proxy.auth.user.OneOpsUser;
import com.oneops.proxy.clients.model.Result;
import com.oneops.proxy.clients.proxy.TektonProxyClient;
import com.oneops.proxy.utils.SecretsConstants;
import java.io.IOException;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AuthorizationServiceException;

/**
 * Authorization provides for Tekton services.
 *
 * @author Varsha
 */
public class TektonClient implements Client {

  private final Logger log = LoggerFactory.getLogger(getClass());
  private TektonProxyClient tektonProxyClient;
  private final String namespace;

  public TektonClient(String namespace, String domain, TektonProxyClient tektonProxyClient) {
    this.namespace = namespace;
    this.tektonProxyClient = tektonProxyClient;
  }

  /**
   * Invoke Tekton service Clients and call auth api.
   *
   * @param appName appName
   * @param user OneopsUser
   * @return <code>true</code> if user has admin access.
   */
  @Override
  public boolean authorizeUser(@Nonnull String appName, @Nonnull OneOpsUser user)
      throws IOException {

    log.info("Tekton Client services : authorization started");

    String org;
    String project;
    String env;
    /**
     * Get the <name> from AppGroup Split the appGroup "{org}_{project_name}_{env} "name with "_"
     * and get name for org, project and env
     */
    String[] nsArray = namespace.split(SecretsConstants.GROUP_SEP);
    if (nsArray.length == 3) {
      org = nsArray[0];
      project = nsArray[1];
      env = nsArray[2];
    } else {
      throw new AuthorizationServiceException("Provided appName is not authenticated");
    }

    Result<Void> result = tektonProxyClient.doAuth(user.getUsername(), org, project, env);
    if (result != null && result.isSuccessful()) {
      return true;
    }
    return false;
  }
}
