/**
 * *****************************************************************************
 *
 * <p>Copyright 2017 Walmart, Inc.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * <p>*****************************************************************************
 */
package com.oneops.proxy.security;

import com.oneops.proxy.config.OneOpsConfig;
import java.io.*;
import java.security.*;
import javax.annotation.Nullable;
import javax.net.ssl.*;
import org.slf4j.*;
import org.springframework.core.io.ResourceLoader;

/**
 * Holds PKCS12 trust-store/keystore to communicate with Keywhiz server. The keystore is optional
 * and is required only for Keywhiz Automation client.
 *
 * @author Suresh
 */
public class KeywhizKeyStore {

  private static final Logger log = LoggerFactory.getLogger(KeywhizKeyStore.class);
  private final Name name;
  private final KeyStore trustStore;
  private final KeyStore keyStore;
  private final char[] keyPassword;
  private final ResourceLoader loader;

  /**
   * Create a keywhiz key store to communicate with Keywhiz server.
   *
   * @param name Keystore name
   * @param trustStoreConfig {@link OneOpsConfig.TrustStore} trust-store properties
   * @param keystoreConfig {@link OneOpsConfig.Keystore} keystore properties
   * @param loader {@link ResourceLoader} for loading resources from classpath for file system.
   */
  public KeywhizKeyStore(
      Name name,
      OneOpsConfig.TrustStore trustStoreConfig,
      OneOpsConfig.Keystore keystoreConfig,
      ResourceLoader loader) {
    this.name = name;
    this.loader = loader;
    if (trustStoreConfig != null) {
      trustStore = keyStoreFromResource(trustStoreConfig);
    } else {
      trustStore = null;
    }
    if (keystoreConfig != null) {
      keyStore = keyStoreFromResource(keystoreConfig);
      keyPassword = keystoreConfig.getKeyPassword();
    } else {
      keyStore = null;
      keyPassword = null;
    }
  }

  /**
   * Load the keystore (PKCS12) from the given resource path. Returns <code>null</code> if the
   * resource path doesn't exist.
   *
   * @param config keystore config
   * @return {@link KeyStore} instance or <code>null</code> if the resource doesn't exist.
   */
  private @Nullable KeyStore keyStoreFromResource(OneOpsConfig.TrustStore config) {
    try {
      try (InputStream ins = loader.getResource(config.getPath()).getInputStream()) {
        log.info("Loading the keystore: " + config.getPath());
        if (ins == null) return null;
        KeyStore ks = KeyStore.getInstance(config.getType());
        ks.load(ins, config.getStorePassword());
        return ks;
      }
    } catch (IOException | GeneralSecurityException ex) {
      throw new IllegalStateException("Can't load the keystore (" + config.getPath() + ").", ex);
    }
  }

  /**
   * Returns the keywhiz server trust store.
   *
   * @return {@link KeyStore} if it's configured, else returns <code>null</code>
   */
  public @Nullable KeyStore getTrustStore() {
    return trustStore;
  }

  /**
   * Returns the keystore for Keywhiz automation client
   *
   * @return {@link KeyStore} if it's configured, else returns <code>null</code>
   */
  public @Nullable KeyStore getKeyStore() {
    return keyStore;
  }

  /**
   * Returns the keystore password.
   *
   * @return password or <code>null</code> if the keystore is empty.
   */
  public @Nullable char[] getKeyPassword() {
    return keyPassword;
  }

  /** Return new trust managers from the trust-store. */
  public TrustManager[] getTrustManagers() throws GeneralSecurityException {
    final TrustManagerFactory trustManagerFactory =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    trustManagerFactory.init(trustStore);
    return trustManagerFactory.getTrustManagers();
  }

  /** Return new key managers from the keystore. */
  public KeyManager[] getKeyManagers() throws GeneralSecurityException {
    final KeyManagerFactory kmfactory =
        KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    kmfactory.init(keyStore, keyPassword);
    return kmfactory.getKeyManagers();
  }

  /** Keystore names. */
  public enum Name {
    Keywhiz,
    LDAP
  }
}
