package com.oneops.proxy.clients.connection;

import com.oneops.proxy.auth.user.OneOpsUser;
import com.oneops.proxy.config.ClientsAuthConfig;
import com.oneops.proxy.model.AppGroup;
import com.oneops.proxy.utils.SecretsConstants;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Proxy client to connect Tekton Services
 *
 * @author Varsha
 */
@Component
public class TektonClientService extends ProxyClientConnection {

  private Logger log = Logger.getLogger(getClass().getSimpleName());

  public TektonClientService() {}

  public boolean auth(
      OneOpsUser oneOpsUser, String appName, ClientsAuthConfig clientsAuthConfig, AppGroup appGroup)
      throws IOException {

    String url = "";
    String token = "";
    String userName = oneOpsUser.getUsername();

    /* Get the domainName from AppGroup.
     *  Output domainStr =  "tekton-prod"
     */
    String domainStr = appGroup.getDomain().getType();

    /* Get the <name> from AppGroup
     * Tekton auth URL contains "/" in appName instead "_" , replace the underscore with slash
     * Output : {org}/{project_name}/{env}
     * */

    String namespace = appGroup.getName().replace(AppGroup.GROUP_SEP, AppGroup.NSPATH_SEP).trim();

    log.info(
        "Tekton Client services :" + " Domain name=" + domainStr + " and Namespace=" + namespace);

    /* Get the configuration properties for Tekton only*/
    Optional<ClientsAuthConfig.ClientsAuthDomain> clientAuth =
        clientsAuthConfig
            .getConfigs()
            .stream()
            .filter(x -> x.getDomainNames().equals(domainStr))
            .findFirst();

    if (clientAuth.isPresent()) {
      url =
          clientAuth.get().getUrl()
              + SecretsConstants.TEKTON_AUTH_PREFIX
              + userName
              + "/"
              + namespace;
      token = clientAuth.get().getAuth().getToken();

      Request request =
          new Request.Builder()
              .url(url)
              .addHeader(SecretsConstants.TEKTON_AUTH_HEADER, token)
              .build();

      try {
        Response response = makeCall(request);

        log.info("Response status " + response.code() + " for Tekton :" + url);
        return response.isSuccessful();

      } catch (IOException e) {
        log.warning(e.getMessage());
        throw new AuthorizationServiceException(
            "Error accessing authorization service: " + appGroup.getNsPath());
      }
    }
    return false;
  }
}
