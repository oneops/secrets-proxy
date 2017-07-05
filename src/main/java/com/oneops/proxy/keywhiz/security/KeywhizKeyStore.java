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

import com.oneops.config.KeyStoreConfig;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.logging.Logger;

import static com.oneops.config.CliConfig.keywhiz;

/**
 * Holds PKCS12 trust-store/keystore to communicate with Keywhiz server.
 * The keystore is optional and is required only for {@link com.oneops.keywhiz.KeywhizAutomationClient}.
 * Both the keystore and trust-store are configured in <strong>application.conf</strong>.
 *
 * @author Suresh
 */
public class KeywhizKeyStore {

    /**
     * Logger instance.
     */
    private static Logger log = Logger.getLogger(KeywhizKeyStore.class.getSimpleName());

    /**
     * Keywhiz trust store.
     */
    private static KeyStore trustStore;

    /**
     * Keywhiz key store.
     */
    private static KeyStore keyStore;

    /**
     * Private key password.
     */
    private static char[] keyPassword;

    static {
        trustStore = keyStoreFromResource(keywhiz.getTrustStore());
        // Keystore is optional.
        if ((keywhiz.getKeyStore() != null)) {
            keyStore = keyStoreFromResource(keywhiz.getKeyStore());
            keyPassword = keywhiz.getKeyStore().getPassword();
        } else {
            keyStore = null;
            keyPassword = null;
        }
    }

    private KeywhizKeyStore() {
    }

    /**
     * Load the keystore (PKCS12) from the given resource path.
     * Returns <code>null</code> if the resource path doesn't exist.
     *
     * @param config keystore config
     * @return {@link KeyStore} instance or <code>null</code> if the resource doesn't exist.
     */
    private static @Nullable
    KeyStore keyStoreFromResource(KeyStoreConfig config) {
        try {
            try (InputStream ins = KeywhizKeyStore.class.getResourceAsStream(config.getName())) {
                log.info("Loading the keystore: " + config.getName());
                if (ins == null) return null;
                KeyStore ks = KeyStore.getInstance(config.getType());
                ks.load(ins, config.getPassword());
                return ks;
            }
        } catch (IOException | GeneralSecurityException ex) {
            throw new IllegalStateException("Can't load the keystore (" + config.getName() + ").", ex);
        }
    }

    /**
     * Returns the keywhiz server trust store.
     *
     * @return {@link KeyStore}
     */
    public static KeyStore getTrustStore() {
        return trustStore;
    }

    /**
     * Returns the keystore for Keywhiz automation client
     *
     * @return {@link KeyStore} if it's configured, else returns <code>null</code>
     */
    public static @Nullable
    KeyStore getKeyStore() {
        return keyStore;
    }

    /**
     * Returns the keystore password.
     *
     * @return password or <code>null</code> if the keystore is empty.
     */
    public static @Nullable
    char[] getKeyPassword() {
        return keyPassword;
    }
}
