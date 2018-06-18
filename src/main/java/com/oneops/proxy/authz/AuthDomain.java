package com.oneops.proxy.authz;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * OneOps auth domains. The user data source for each corresponding domain is configured in the
 * <b>application.yaml</b>.
 *
 * @author Suresh
 */
public enum AuthDomain {
  PROD("prod"),
  MGMT("mgmt"),
  STG("stg"),
  DEV("dev");

  private final String type;

  AuthDomain(String type) {
    this.type = type;
  }

  /** This is for dealing with case insensitive enum in json. */
  @JsonCreator
  public static AuthDomain of(String type) {
    return AuthDomain.valueOf(type.toUpperCase());
  }

  @JsonValue
  public String getType() {
    return type;
  }
}
