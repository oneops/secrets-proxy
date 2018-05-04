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

import com.oneops.proxy.web.GroupController;

import javax.annotation.Nonnull;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static org.apache.commons.lang3.StringUtils.isBlank;

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

  private final String domain;
  private final String name;
  private final String org;
  private final String assembly;
  private final String env;

  /**
   * Creates new {@link AppGroup} from the given domain and app group name.
   *
   * @param domain OneOps mgmt domain
   * @param name application group name. OneOps environment name with <b>{org}_{assembly}_{env}</b>
   *     format.
   * @return {@link AppGroup}
   * @throws IllegalArgumentException if the app group name format is not valid.
   */
  public static AppGroup from(@Nonnull String domain, @Nonnull String name) {
    return new AppGroup(domain, name);
  }

  /**
   * Constructor for {@link AppGroup}.
   *
   * @param domain OneOps mgmt domain
   * @param name application group name
   * @throws IllegalArgumentException if the app group name format is not valid.
   */
  public AppGroup(@Nonnull String domain, @Nonnull String name) {
    this.domain = domain;
    this.name = name;

    String[] paths = name.split(GROUP_SEP);
    if (paths.length != 3 || isBlank(paths[0]) || isBlank(paths[1]) || isBlank(paths[2])) {
      throw new IllegalArgumentException(
          "Invalid application group name: " + name + ". The format is 'org_assembly_env'.");
    }

    org = paths[0].trim();
    assembly = paths[1].trim();
    env = paths[2].trim();
  }

  /** Returns oneOps mgmt domain for the application group. */
  public String getDomain() {
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
    return String.format("/%s/%s/%s/%s", domain, org, assembly, env).toLowerCase();
  }

  /**
   * Returns the http url encoded {@link #getGroupName()}. Use this method when making requests to
   * keywhiz servers. The URL encoded group name is used for request con
   */
  public String getKeywhizGroup() throws UnsupportedEncodingException {
    return URLEncoder.encode(getGroupName(), "UTF-8");
  }

  @Override
  public String toString() {
    return "AppGroup{"
        + "domain='"
        + domain
        + '\''
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
