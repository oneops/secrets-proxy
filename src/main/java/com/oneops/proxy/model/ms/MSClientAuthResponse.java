package com.oneops.proxy.model.ms;

import java.util.Map;

/**
 * Response object for Auth Api in Managed Services. Return if true/false if user is authorized with
 * given namespace.
 *
 * @author Varsha
 */
public class MSClientAuthResponse {

  private String namespace;

  private String user;
  private boolean authorized;
  private String error;
  private Map<String, String> metadata;

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

  public boolean isAuthorized() {
    return authorized;
  }

  public void setAuthorized(boolean authorized) {
    this.authorized = authorized;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public Map<String, String> getMetadata() {
    return metadata;
  }

  public void setMetadata(Map<String, String> metadata) {
    this.metadata = metadata;
  }
}
