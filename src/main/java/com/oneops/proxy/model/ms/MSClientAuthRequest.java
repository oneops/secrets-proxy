package com.oneops.proxy.model.ms;

import java.util.List;

/**
 * Request object for Managed Services Auth Api. Uses namespace as appName and List of Credentials.
 * username Credentials
 *
 * @author Varsha
 */
public class MSClientAuthRequest {

  private String namespace;
  private String user;
  private String password;

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
