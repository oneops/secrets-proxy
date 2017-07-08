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
package com.oneops.proxy.config;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * OneOps proxy configuration properties,for communicating to the Keywhiz and LDAP.
 *
 * @author Suresh G
 */
@ConfigurationProperties(value = "oneops")
public class OneOpsConfig {

    @NotNull
    private Keywhiz keywhiz;

    @NotNull
    private LDAP ldap;

    public Keywhiz getKeywhiz() {
        return keywhiz;
    }

    public void setKeywhiz(Keywhiz keywhiz) {
        this.keywhiz = keywhiz;
    }

    public LDAP getLdap() {
        return ldap;
    }

    public void setLdap(LDAP ldap) {
        this.ldap = ldap;
    }

    @Override
    public String toString() {
        return "OneOpsConfig{" +
                "keywhiz=" + keywhiz +
                ", ldap=" + ldap +
                '}';
    }

    public static class Keywhiz {
        @NotNull
        private String baseUrl;

        private String svcUser;

        private char[] svcPassword;

        @NotNull
        @NestedConfigurationProperty
        private TrustStore trustStore;

        @NotNull
        @NestedConfigurationProperty
        private Keystore keyStore;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getSvcUser() {
            return svcUser;
        }

        public void setSvcUser(String svcUser) {
            this.svcUser = svcUser;
        }

        public char[] getSvcPassword() {
            return svcPassword;
        }

        public void setSvcPassword(char[] svcPassword) {
            this.svcPassword = svcPassword;
        }

        public TrustStore getTrustStore() {
            return trustStore;
        }

        public void setTrustStore(TrustStore trustStore) {
            this.trustStore = trustStore;
        }

        public Keystore getKeyStore() {
            return keyStore;
        }

        public void setKeyStore(Keystore keyStore) {
            this.keyStore = keyStore;
        }

        @Override
        public String toString() {
            return "Keywhiz{" +
                    "baseUrl='" + baseUrl + '\'' +
                    ", svcUser='" + svcUser + '\'' +
                    ", svcPassword=******" +
                    ", trustStore=" + trustStore +
                    ", keyStore=" + keyStore +
                    '}';
        }
    }

    public static class LDAP {
        @NotNull
        private String server;

        @NotNull
        private int port;

        @NotNull
        private String userDn;

        @NotNull
        private char[] password;

        @NotNull
        private String userBaseDn;

        @NotNull
        private String userAttribute;

        @NotNull
        @NestedConfigurationProperty
        private TrustStore trustStore;

        public String getServer() {
            return server;
        }

        public void setServer(String server) {
            this.server = server;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUserDn() {
            return userDn;
        }

        public void setUserDn(String userDn) {
            this.userDn = userDn;
        }

        public char[] getPassword() {
            return password;
        }

        public void setPassword(char[] password) {
            this.password = password;
        }

        public String getUserBaseDn() {
            return userBaseDn;
        }

        public void setUserBaseDn(String userBaseDn) {
            this.userBaseDn = userBaseDn;
        }

        public String getUserAttribute() {
            return userAttribute;
        }

        public void setUserAttribute(String userAttribute) {
            this.userAttribute = userAttribute;
        }

        public TrustStore getTrustStore() {
            return trustStore;
        }

        public void setTrustStore(TrustStore trustStore) {
            this.trustStore = trustStore;
        }

        @Override
        public String toString() {
            return "LDAP{" +
                    "server='" + server + '\'' +
                    ", port=" + port +
                    ", userDn=******" +
                    ", password=******" +
                    ", userBaseDn='" + userBaseDn + '\'' +
                    ", userAttribute='" + userAttribute + '\'' +
                    ", trustStore=" + trustStore +
                    '}';
        }
    }

    public static class TrustStore {
        @NotNull
        private String path;
        @NotNull
        private String type;
        @NotNull
        private char[] storePassword;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public char[] getStorePassword() {
            return storePassword;
        }

        public void setStorePassword(char[] storePassword) {
            this.storePassword = storePassword;
        }

        @Override
        public String toString() {
            return "TrustStore{" +
                    "path='" + path + '\'' +
                    ", type='" + type + '\'' +
                    ", storePassword=******" +
                    '}';
        }
    }

    public static class Keystore extends TrustStore {
        @NotNull
        private char[] keyPassword;

        public char[] getKeyPassword() {
            return keyPassword;
        }

        public void setKeyPassword(char[] keyPassword) {
            this.keyPassword = keyPassword;
        }

        @Override
        public String toString() {
            return "Keystore{" +
                    "path='" + super.path + '\'' +
                    ", type='" + super.type + '\'' +
                    ", storePassword=******" +
                    ", keyPassword=******" +
                    '}';
        }
    }
}
