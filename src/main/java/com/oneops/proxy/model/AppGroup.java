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
package com.oneops.proxy.model;

import static org.apache.commons.lang3.StringUtils.isBlank;

import com.oneops.proxy.authz.AuthDomain;
import com.oneops.proxy.web.GroupController;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.annotation.Nonnull;

/**
 * A domain model to represents keywhiz application group in {@link GroupController}.
 *
 * @author Suresh G
 */
public class AppGroup {

  /** Application group path name used by default. */
  public static final String APP_NAME_PARAM = "appName";

  /** Application group name separator. */
  public static final String GROUP_SEP = "_";

  /** Additional metadata used when creating new groups. */
  public static final String USERID_METADATA = "_userId";

  public static final String DOMAIN_METADATA = "_domain";

    private final AuthDomain domain;
    private final String name;
    private final String org;
    private final String assembly;
    private final String env;

  /*TODO*/
  private String sysName;

  /**
   * Creates new {@link AppGroup} from the given domain and app group name.
   *
   * @param domain OneOps auth domain
   * @param name application group name. OneOps environment name with <b>{org}_{assembly}_{env}</b>
   *     format.
   * @return {@link AppGroup}
   * @throws IllegalArgumentException if the app group name format is not valid.
   */
  public static AppGroup from(@Nonnull AuthDomain domain, @Nonnull String name) {
    return new AppGroup(domain, name);
  }

  /**
   * Constructor for {@link AppGroup}.
   *
   * @param domain OneOps auth domain
   * @param name application group name
   * @throws IllegalArgumentException if the app group name format is not valid.
   */
  public AppGroup(@Nonnull AuthDomain domain, @Nonnull String name) {
    this.domain = domain;
    this.name = name;
    this.sysName = "";

    String[] paths = name.split(GROUP_SEP);

    if (paths.length == 4
        && !(isBlank(paths[0]) || isBlank(paths[1]) || isBlank(paths[2]) || isBlank(paths[3]))) {
      sysName = paths[0].trim();
      org = paths[1].trim();
      assembly = paths[2].trim();
      env = paths[3].trim();
    } else if (paths.length == 3
        && !(isBlank(paths[0]) || isBlank(paths[1]) || isBlank(paths[2]))) {
      org = paths[0].trim();
      assembly = paths[1].trim();
      env = paths[2].trim();
    } else
      throw new IllegalArgumentException(
          "Invalid application group name: "
              + name
              + ". The format is 'systemName_org_assembly_env'.");
  }

  /** Returns oneOps auth domain for the application group. */
  public AuthDomain getDomain() {
    return domain;
  }

  /** Returns application group name. */
  public String getName() {
    return name;
  }

  /** Returns OneOps org for the application group. */
  public String getOrg() {
    return org;
  }

  /** Returns OneOps org nspath for the application group. */
  public String getOrgNsPath() {
    return "/" + org;
  }

  /** Returns OneOps assembly for the application group. */
  public String getAssembly() {
    return assembly;
  }

  /** Returns OneOps env for the application group. */
  public String getEnv() {
    return env;
  }

  /** Returns OneOps env nspath for the application group. */
  public String getNsPath() {
    return String.format("/%s/%s/%s", org, assembly, env);
  }

  /**
   * Returns the keywhiz group name. The keywhiz group name is using the format
   * <b>/{domain}/{org}/{assembly}/{env}</b>. The domain is used to support multiple OneOps
   * instances and is defaults to <b>prod</b>.
   */
  public String getGroupName() {
    return String.format("/%s/%s/%s/%s", domain.getType(), org, assembly, env).toLowerCase();
  }

  public String removeSysNameFromAppName(String appName) {
    String[] paths = appName.split(GROUP_SEP);
    if (paths.length == 4) {
      sysName = paths[0].trim();
      return appName.replaceFirst(sysName + GROUP_SEP, "").trim();
    }
    if (paths.length == 3) return appName;

    return "";
  }

  /**
   * Returns the http url encoded {@link #getGroupName()}. Use this method when making requests to
   * keywhiz servers. The URL encoded group name is used for request con
   */
  public String getKeywhizGroup() throws UnsupportedEncodingException {
    return URLEncoder.encode(getGroupName(), "UTF-8");
  }

  public String getSysName() {
    return sysName;
  }

  @Override
  public String toString() {
    return "AppGroup{"
        + "domain="
        + domain.getType()
        + ", name='"
        + name
        + '\''
        + ", org='"
        + org
        + '\''
        + ", assembly='"
        + assembly
        + '\''
        + ", env='"
        + env
        + '\''
        + '}';
  }
}
