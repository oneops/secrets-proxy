package com.oneops.proxy.clients.connection;

import com.oneops.proxy.auth.user.OneOpsUser;
import com.oneops.proxy.config.ClientsAuthConfig;
import com.oneops.proxy.model.AppGroup;
import com.oneops.proxy.model.ms.AuthorizedUser;
import com.oneops.proxy.model.ms.Credentials;
import com.oneops.proxy.model.ms.MSClientAuthRequest;
import com.oneops.proxy.model.ms.MSClientAuthResponse;
import com.oneops.proxy.utils.SecretsConstants;
import okhttp3.HttpUrl;
import okhttp3.Request;
import org.springframework.security.access.AuthorizationServiceException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Proxy client to connect to Managed Services
 *
 * @author Varsha
 */
public class MSClientService extends ProxyClientConnection {

  private Logger log = Logger.getLogger(getClass().getSimpleName());

  public MSClientService() {
    super();
  }

  public boolean auth(
      OneOpsUser oneOpsUser, String appName, ClientsAuthConfig clientsAuthConfig, AppGroup appGroup)
      throws IOException, Exception {

    String url = "";
    String code = "";
    String domainPrefix = SecretsConstants.MS_APP;
    String domainSep = AppGroup.DOMAIN_SEP;

    okhttp3.Response response = null;

    /* Get the domainName from AppGroup.
     *  Output domainStr =  "ms-prod"
     *  */
    if (appGroup.getDomain().getType().contains(domainPrefix)) {
      domainPrefix = "".trim();
      domainSep = "".trim();
    }
    String domainStr = domainPrefix + domainSep + appGroup.getDomain().getType();

    /* Get appName from AppGroup
     * Output : {org}_{asmb}_{env}
     * */
    String namespace = appGroup.getName();

    log.info(
        "MS Client services : Auth : "
            + " Domain name="
            + domainStr
            + " and Namespace="
            + namespace);

    /* Create MS client request */
    MSClientAuthRequest authRequest = createRequest(oneOpsUser.getUsername(), namespace);

    /* Get the configuration properties for managed services only*/
    Optional<ClientsAuthConfig.ClientsAuthDomain> clientAuth =
        clientsAuthConfig
            .getConfigs()
            .stream()
            .filter(x -> x.getDomainNames().equals(domainStr))
            .findFirst();

    if (clientAuth.isPresent()) {
      url = clientAuth.get().getUrl() + SecretsConstants.MS_AUTH_PREFIX;
      code = clientAuth.get().getAuth().getToken();

      /* Create OkHttpClient request with URL and Auth header*/
      Request request =
          httpPost(HttpUrl.parse(url), authRequest)
              .newBuilder()
              .addHeader(SecretsConstants.MS_AUTH_HEADER, code)
              .build();

      try {
        response = createHttpsClientWithSSLContext().newCall(request).execute();
        log.info("Response status " + response.code() + " for Managed Service :" + url);
      } catch (Exception e) {
        response.body().close();
        throw new AuthorizationServiceException(
            "Error accessing authorization service: " + appGroup.getNsPath());
      }

      String responseStr = response.body().string();
      MSClientAuthResponse responseObj = mapper.readValue(responseStr, MSClientAuthResponse.class);
      if (null != responseObj && null != responseObj.getAuthorized()) {
        Optional<AuthorizedUser> authorizedUser =
            responseObj
                .getAuthorized()
                .stream()
                .filter(x -> x.getUser().equalsIgnoreCase(oneOpsUser.getUsername()))
                .findFirst();
        if (authorizedUser.isPresent()) return authorizedUser.get().isAuthorized();
      }
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
