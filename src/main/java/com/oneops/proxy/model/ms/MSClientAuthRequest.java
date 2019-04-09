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
  private List<Credentials> credential;

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public List<Credentials> getCredential() {
    return credential;
  }

  public void setCredential(List<Credentials> credential) {
    this.credential = credential;
  }
}
