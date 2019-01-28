package com.oneops.proxy.clients.auth;

import com.oneops.proxy.authz.UserRepository;
import com.oneops.proxy.clients.proxy.MSProxyClient;
import com.oneops.proxy.clients.proxy.TektonProxyClient;
import com.oneops.proxy.model.AppGroup;
import com.oneops.proxy.utils.SecretsConstants;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * ClientSelector selects client based on appName {@link Client}.
 *
 * @author Varsha
 */
@Component
public class ClientSelector {

  private final Logger log = LoggerFactory.getLogger(getClass());
  private final Map<String, Client> clientMap = new ConcurrentHashMap<>();
  private MSProxyClient msProxyClient;
  private TektonProxyClient tektonProxyClient;

  public ClientSelector(MSProxyClient msProxyClient, TektonProxyClient tektonProxyClient) {
    this.msProxyClient = msProxyClient;
    this.tektonProxyClient = tektonProxyClient;
  }

  /**
   * Method used to select clients
   *
   * @param appGroup
   * @param userRepo
   * @return Client
   */
  public Client selectClient(AppGroup appGroup, UserRepository userRepo) {
    if (appGroup.getName().startsWith(SecretsConstants.MS_APP)) {
      log.info("Selected Managed Service client");
      return clientMap.computeIfAbsent(
          appGroup.getName(),
          v -> {
            log.info("Create cache for managed service");
            Client msClient = null;
            try {
              msProxyClient.setup(appGroup.getDomain().getType());
              msClient = new MSClient(appGroup.getName(), msProxyClient);
            } catch (GeneralSecurityException e) {
              log.error("MS Client can't be authorize in the system.");
            }
            return msClient;
          });
    } else if (appGroup.getDomain().getType().startsWith(SecretsConstants.TEKTON_APP)) {
      log.info("Selected Tekton client");
      return clientMap.computeIfAbsent(
          appGroup.getName(),
          v -> {
            log.info("Create cache for tekton service");
            Client tektonClient = null;
            try {
              tektonProxyClient.setup(appGroup.getDomain().getType());
              tektonClient =
                  new TektonClient(
                      appGroup.getName(), appGroup.getDomain().getType(), tektonProxyClient);
            } catch (GeneralSecurityException e) {
              log.error("Tekton Client can't be authorize in the system.");
            }
            return tektonClient;
          });
    } else {
      log.info("Selected Oneops client");
      return clientMap.computeIfAbsent(appGroup.getName(), v -> new OneopsClient(userRepo));
    }
  }
}
