package com.oneops.proxy.clients.auth;

import com.oneops.proxy.auth.user.OneOpsUser;
import com.oneops.proxy.clients.model.Result;
import com.oneops.proxy.clients.proxy.TektonProxyClient;
import com.oneops.proxy.config.ClientsAuthConfig;
import com.oneops.proxy.model.AppGroup;
import java.io.IOException;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authorization provides for Tekton services.
 *
 * @author Varsha
 */
public class TektonClient implements Client {

  private final Logger log = LoggerFactory.getLogger(getClass());
  private final ClientsAuthConfig config;
  private final AppGroup appGroup;
  private TektonProxyClient client;

  public TektonClient(TektonProxyClient client, ClientsAuthConfig config, AppGroup appGroup) {
    this.client = client;
    this.config = config;
    this.appGroup = appGroup;
  }

  /* Invoke Tekton service Clients and call auth api.
   *
   * @param appName appName
   * @param user OneopsUser
   * @return <code>true</code> if user has admin access.
   */

  @Override
  public boolean authorizeUser(@Nonnull String appName, @Nonnull OneOpsUser user)
      throws IOException {

    String org = "";
    String project = "";
    String env = "";
    /* Get the domainName from AppGroup.
     *  Output domainStr =  "tekton-prod"
     */
    String domainStr = appGroup.getDomain().getType();

    /* Get the <name> from AppGroup
     * Split the appGroup "{org}_{project_name}_{env} "name with "_" and get name for org, project and env
     *
     * */

    String[] namespace = appGroup.getName().split("_");
    if (namespace.length == 3) {
      org = namespace[0];
      project = namespace[1];
      env = namespace[2];
    }

    log.info(
        "Tekton Client services :"
            + " Domain name="
            + domainStr
            + " and Namespace="
            + org
            + "/"
            + project
            + "/"
            + env);

    /* Get the configuration properties for Tekton only*/
    Optional<ClientsAuthConfig.ClientsAuthDomain> clientAuth =
        config.getConfigs().stream().filter(x -> x.getDomainNames().equals(domainStr)).findFirst();

    Result<Void> result = client.doAuth(user.getUsername(), org, project, env);
    if (result.isSuccessful()) {
      return true;
    }

    return false;
  }
}
