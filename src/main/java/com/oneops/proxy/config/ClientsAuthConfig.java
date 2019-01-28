package com.oneops.proxy.config;

import java.util.List;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Proxy configuration properties for communicating to the secret-proxy clients like Managed
 * Services, Tekton etc.
 *
 * @author Varsha
 */
@Configuration
@ConfigurationProperties(value = "services")
public class ClientsAuthConfig {

  private static List<ClientsAuthDomain> configs;

  public List<ClientsAuthDomain> getConfigs() {
    return configs;
  }

  public void setConfigs(List<ClientsAuthDomain> configs) {
    this.configs = configs;
  }

  public static class ClientsAuthDomain {
    @NotNull private String domainNames;
    @NotNull private String url;
    @NestedConfigurationProperty private ClientsAuthData auth;
    private int timeout;

    public String getDomainNames() {
      return domainNames;
    }

    public void setDomainNames(String domainNames) {
      this.domainNames = domainNames;
    }

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public ClientsAuthData getAuth() {
      return auth;
    }

    public void setAuth(ClientsAuthData auth) {
      this.auth = auth;
    }

    public int getTimeout() {
      return timeout;
    }

    public void setTimeout(int timeout) {
      this.timeout = timeout;
    }

    @Override
    public String toString() {
      return "ClientAuthConfig{" + "domainNames=" + domainNames + ", url=" + url + '}';
    }
  }

  public static class ClientsAuthData {

    private String token;
    private String username;

    public String getToken() {
      return token;
    }

    public void setToken(String token) {
      this.token = token;
    }

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    @Override
    public String toString() {
      return "ClientAuthConfig{" + " username=" + username + '}';
    }
  }

  public Optional<ClientsAuthDomain> getAuthClients(String domain) {
    return configs.stream().filter(x -> x.getDomainNames().equals(domain)).findFirst();
  }
}
