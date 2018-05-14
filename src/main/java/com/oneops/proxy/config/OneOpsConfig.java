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
package com.oneops.proxy.config;

import java.util.List;
import javax.validation.constraints.*;
import org.springframework.boot.context.properties.*;
import org.springframework.http.HttpHeaders;

/**
 * OneOps proxy configuration properties,for communicating to the Keywhiz and LDAP.
 *
 * @author Suresh G
 */
@ConfigurationProperties(value = "oneops")
public class OneOpsConfig {

  /**
   * The base url used for different oneops services. The url will change depending on the domain.
   */
  @NotNull private String baseUrl;

  @NotNull private Keywhiz keywhiz;

  @NotNull private LDAP ldap;

  @NotNull private Auth auth;

  private Proxy proxy;

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

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

  public Auth getAuth() {
    return auth;
  }

  public void setAuth(Auth auth) {
    this.auth = auth;
  }

  public Proxy getProxy() {
    return proxy;
  }

  public void setProxy(Proxy proxy) {
    this.proxy = proxy;
  }

  @Override
  public String toString() {
    return "OneOpsConfig{"
        + "baseUrl='"
        + baseUrl
        + '\''
        + ", keywhiz="
        + keywhiz
        + ", ldap="
        + ldap
        + ", auth="
        + auth
        + ", proxy="
        + proxy
        + '}';
  }

  public static class Keywhiz {

    @NotNull private String baseUrl;

    private String svcUser;

    private char[] svcPassword;

    /** Secret max size in bytes. */
    private long secretMaxSize;

    @NotNull @NestedConfigurationProperty private TrustStore trustStore;

    @NotNull @NestedConfigurationProperty private Keystore keyStore;

    @NotNull @NestedConfigurationProperty private Cli cli;

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

    public Cli getCli() {
      return cli;
    }

    public void setCli(Cli cli) {
      this.cli = cli;
    }

    public long getSecretMaxSize() {
      return secretMaxSize;
    }

    public void setSecretMaxSize(long secretMaxSize) {
      this.secretMaxSize = secretMaxSize;
    }

    @Override
    public String toString() {
      return "Keywhiz{"
          + "baseUrl='"
          + baseUrl
          + '\''
          + ", svcUser=******"
          + ", svcPassword=******"
          + ", secretMaxSize="
          + secretMaxSize
          + ", trustStore="
          + trustStore
          + ", keyStore="
          + keyStore
          + ", cli="
          + cli
          + '}';
    }
  }

  public static class Cli {
    @NotNull private String version;

    @NotNull private String userAgentHeader;

    @NotNull private String downloadPath;

    @NotNull private String downloadUrl;

    public String getVersion() {
      return version;
    }

    public void setVersion(String version) {
      this.version = version;
    }

    public String getDownloadPath() {
      return downloadPath;
    }

    public void setDownloadPath(String downloadPath) {
      this.downloadPath = downloadPath;
    }

    public String getDownloadUrl() {
      return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
      this.downloadUrl = downloadUrl;
    }

    public String getUserAgentHeader() {
      return userAgentHeader;
    }

    public void setUserAgentHeader(String userAgentHeader) {
      this.userAgentHeader = userAgentHeader;
    }

    @Override
    public String toString() {
      return "Cli{"
          + "version='"
          + version
          + '\''
          + ", userAgentHeader='"
          + userAgentHeader
          + '\''
          + ", downloadPath='"
          + downloadPath
          + '\''
          + ", downloadUrl='"
          + downloadUrl
          + '\''
          + '}';
    }
  }

  public static class LDAP {
    @NotNull private String server;

    @Min(1)
    @Max(65535)
    private int port;

    @NotNull private String userDn;

    @NotNull private char[] password;

    @NotNull private String userBaseDn;

    @NotNull private String userAttribute;

    private String roleBaseDn;

    private String roleAttribute;

    private List<String> roles;

    @NotNull @NestedConfigurationProperty private TrustStore trustStore;

    @NotNull @NestedConfigurationProperty private Keystore keyStore;

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

    public String getRoleBaseDn() {
      return roleBaseDn;
    }

    public void setRoleBaseDn(String roleBaseDn) {
      this.roleBaseDn = roleBaseDn;
    }

    public String getRoleAttribute() {
      return roleAttribute;
    }

    public void setRoleAttribute(String roleAttribute) {
      this.roleAttribute = roleAttribute;
    }

    public List<String> getRoles() {
      return roles;
    }

    public void setRoles(List<String> roles) {
      this.roles = roles;
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
      return "LDAP{"
          + "server='"
          + server
          + '\''
          + ", port="
          + port
          + ", userDn=******"
          + ", password=******"
          + ", userBaseDn='"
          + userBaseDn
          + '\''
          + ", userAttribute='"
          + userAttribute
          + '\''
          + ", roleBaseDn='"
          + roleBaseDn
          + '\''
          + ", roleAttribute='"
          + roleAttribute
          + '\''
          + ", roles="
          + roles
          + ", trustStore="
          + trustStore
          + ", keyStore="
          + keyStore
          + '}';
    }
  }

  public static class TrustStore {
    @NotNull private String path;
    @NotNull private String type;
    @NotNull private char[] storePassword;

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
      return "TrustStore{"
          + "path='"
          + path
          + '\''
          + ", type='"
          + type
          + '\''
          + ", storePassword=******"
          + '}';
    }
  }

  public static class Keystore extends TrustStore {
    @NotNull private char[] keyPassword;

    public char[] getKeyPassword() {
      return keyPassword;
    }

    public void setKeyPassword(char[] keyPassword) {
      this.keyPassword = keyPassword;
    }

    @Override
    public String toString() {
      return "Keystore{"
          + "path='"
          + super.path
          + '\''
          + ", type='"
          + super.type
          + '\''
          + ", storePassword=******"
          + ", keyPassword=******"
          + '}';
    }
  }

  /** JWT (JWS) authentication properties. */
  public static class Auth {

    /** HMAC using SHA-512 */
    @NotNull private char[] signingKey;

    @NotNull private String header = HttpHeaders.AUTHORIZATION;

    @NotNull private String issuer = "OneOps-Proxy";

    @NotNull private String tokenType = "Bearer";

    /** Enable JWT body compression. */
    private boolean compressionEnabled;

    /** Token expiry in secs. */
    @NotNull private int expiresInSec;

    public char[] getSigningKey() {
      return signingKey;
    }

    public void setSigningKey(char[] signingKey) {
      this.signingKey = signingKey;
    }

    public String getHeader() {
      return header;
    }

    public void setHeader(String header) {
      this.header = header;
    }

    public int getExpiresInSec() {
      return expiresInSec;
    }

    public void setExpiresInSec(int expiresInSec) {
      this.expiresInSec = expiresInSec;
    }

    public String getIssuer() {
      return issuer;
    }

    public void setIssuer(String issuer) {
      this.issuer = issuer;
    }

    public String getTokenType() {
      return tokenType;
    }

    public void setTokenType(String tokenType) {
      this.tokenType = tokenType;
    }

    public boolean isCompressionEnabled() {
      return compressionEnabled;
    }

    public void setCompressionEnabled(boolean compressionEnabled) {
      this.compressionEnabled = compressionEnabled;
    }

    @Override
    public String toString() {
      return "Auth{"
          + "header='"
          + header
          + '\''
          + ", issuer='"
          + issuer
          + '\''
          + ", tokenType='"
          + tokenType
          + '\''
          + ", compressionEnabled="
          + compressionEnabled
          + ", expiresInSec="
          + expiresInSec
          + '}';
    }
  }

  /** OneOps Http Transparent Proxy configuration. */
  public static class Proxy {

    /** A mandatory URI like http://host:80/context to which the request is proxied. */
    @NotNull private String proxyTo;

    /** An optional URI prefix that is stripped from the start of the forwarded URI. */
    @NotNull private String prefix = "/proxy";

    /** The name to use in the Via header: Via: http/1.1 <viaHost> */
    private String viaHost = "OneOps Proxy";

    /** <code>true</code> if it should trust all https connection to {@link #proxyTo} server. */
    private boolean trustAll = true;

    /** <code>true</code> if the proxy is enabled. */
    private boolean enabled = false;

    public String getProxyTo() {
      return proxyTo;
    }

    public void setProxyTo(String proxyTo) {
      this.proxyTo = proxyTo;
    }

    public String getPrefix() {
      return prefix;
    }

    public void setPrefix(String prefix) {
      this.prefix = prefix;
    }

    public boolean isTrustAll() {
      return trustAll;
    }

    public String getViaHost() {
      return viaHost;
    }

    public void setViaHost(String viaHost) {
      this.viaHost = viaHost;
    }

    public void setTrustAll(boolean trustAll) {
      this.trustAll = trustAll;
    }

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    @Override
    public String toString() {
      return "Proxy{"
          + "proxyTo='"
          + proxyTo
          + '\''
          + ", prefix='"
          + prefix
          + '\''
          + ", viaHost='"
          + viaHost
          + '\''
          + ", trustAll="
          + trustAll
          + ", enabled="
          + enabled
          + '}';
    }
  }
}
