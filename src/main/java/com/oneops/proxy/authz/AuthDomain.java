package com.oneops.proxy.authz;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.oneops.proxy.utils.SecretsConstants;

/**
 * OneOps auth domains. The user data source for each corresponding domain is configured in the
 * <b>application.yaml</b>.
 *
 * @author Suresh
 */
public enum AuthDomain {

    /*Oneops domain name*/
    PROD("prod"),
    MGMT("mgmt"),
    STG("stg"),
    DEV("dev"),

    /*Tekton domain name*/
    TEKTON_PROD("tekton-prod"),
    TEKTON_MGMT("tekton-mgmt"),
    TEKTON_STG("tekton-stg"),
    TEKTON_DEV("tekton-dev"),

    /*MS domain name*/
    MS_PROD("ms-prod"),
    MS_MGMT("ms-mgmt"),
    MS_STG("ms-stg"),
    MS_DEV("ms-dev");

  private final String type;

  AuthDomain(String type) {
    this.type = type;
  }

  /** This is for dealing with case insensitive enum in json. */
  @JsonCreator
  public static AuthDomain of(String type) {
    return AuthDomain.valueOf(type.replaceFirst(SecretsConstants.DOMAIN_SEP, SecretsConstants.GROUP_SEP).toUpperCase());
  }

  @JsonValue
  public String getType() {
    return type;
  }
}
