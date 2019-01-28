package com.oneops.proxy.clients.proxy;

import com.oneops.proxy.config.OneOpsConfig;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.springframework.util.ResourceUtils;

/**
 * Helper class for proxy clients.
 *
 * @author Varsha
 */
public class ProxyClientUtil {

  private static Logger log = Logger.getLogger(ProxyClientUtil.class.getName());

  public static TrustManager[] getTrustManagers(OneOpsConfig.TrustStore config)
      throws GeneralSecurityException {
    final TrustManagerFactory trustManagerFactory =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    trustManagerFactory.init(keyStoreFromResource(config));
    return trustManagerFactory.getTrustManagers();
  }

  @Nullable
  private static KeyStore keyStoreFromResource(OneOpsConfig.TrustStore config) {
    try {
      try (InputStream ins = new FileInputStream(ResourceUtils.getFile(config.getPath()))) {
        log.info("Loading the trust-store: " + config.getPath());
        if (ins == null) {
          throw new IllegalStateException("Can't find the trust-store.");
        }
        KeyStore ks = KeyStore.getInstance(config.getType());
        ks.load(ins, config.getStorePassword());
        return ks;
      }
    } catch (IOException | GeneralSecurityException ex) {
      throw new IllegalStateException("Can't load the trust-store (" + config.getPath() + ").", ex);
    }
  }

  public static SSLSocketFactory getSocketfactory(TrustManager[] trustManagers)
      throws GeneralSecurityException {
    SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
    sslContext.init(null, trustManagers, new SecureRandom());
    return sslContext.getSocketFactory();
  }
}
