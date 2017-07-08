/*******************************************************************************
 *
 *   Copyright 2017 Walmart, Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *******************************************************************************/
package com.oneops.proxy.keywhiz.security;

import com.oneops.proxy.config.OneOpsConfig;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;


/**
 * Holds PKCS12 trust-store/keystore to communicate with Keywhiz server.
 * The keystore is optional and is required only for Keywhiz Automation client.
 *
 * @author Suresh
 */
public class KeywhizKeyStore {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(KeywhizKeyStore.class);
    private final KeyStore trustStore;
    private final KeyStore keyStore;
    private final char[] keyPassword;
    private final OneOpsConfig.Keywhiz config;
    private final ResourceLoader loader;

    /**
     * Create a keywhiz key store to communicate with Keywhiz server.
     *
     * @param config {@link OneOpsConfig.Keywhiz} Keywhiz config properties
     * @param loader {@link ResourceLoader} for loading resources from classpath for file system.
     */
    public KeywhizKeyStore(OneOpsConfig.Keywhiz config, ResourceLoader loader) {
        this.config = config;
        this.loader = loader;
        if (config.getTrustStore() != null) {
            trustStore = keyStoreFromResource(config.getTrustStore());
        } else {
            trustStore = null;
        }
        if (config.getKeyStore() != null) {
            keyStore = keyStoreFromResource(config.getKeyStore());
            keyPassword = config.getKeyStore().getKeyPassword();
        } else {
            keyStore = null;
            keyPassword = null;
        }
    }

    /**
     * Load the keystore (PKCS12) from the given resource path.
     * Returns <code>null</code> if the resource path doesn't exist.
     *
     * @param config keystore config
     * @return {@link KeyStore} instance or <code>null</code> if the resource doesn't exist.
     */
    private @Nullable
    KeyStore keyStoreFromResource(OneOpsConfig.TrustStore config) {
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
    public @Nullable
    KeyStore getTrustStore() {
        return trustStore;
    }

    /**
     * Returns the keystore for Keywhiz automation client
     *
     * @return {@link KeyStore} if it's configured, else returns <code>null</code>
     */
    public @Nullable
    KeyStore getKeyStore() {
        return keyStore;
    }

    /**
     * Returns the keystore password.
     *
     * @return password or <code>null</code> if the keystore is empty.
     */
    public @Nullable
    char[] getKeyPassword() {
        return keyPassword;
    }
}
