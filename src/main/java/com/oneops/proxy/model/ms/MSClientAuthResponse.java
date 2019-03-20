package com.oneops.proxy.model.ms;

import java.util.List;

/**
 * Response object for Auth Api in Managed Services. Return if true/false if user is authorized with
 * given namespace.
 *
 * @author Varsha
 */
public class MSClientAuthResponse {

  private String namespace;

  private List<AuthorizedUser> authorized;

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public List<AuthorizedUser> getAuthorized() {
    return authorized;
  }

  public void setAuthorized(List<AuthorizedUser> authorized) {
    this.authorized = authorized;
  }
}
