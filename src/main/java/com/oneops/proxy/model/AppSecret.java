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

import static com.oneops.proxy.model.AppGroup.GROUP_SEP;

/**
 * A domain model to represents keywhiz application group secret in {@link GroupController}. It
 * basically contain information about application group and secret name extracted from user
 * request.
 *
 * @author Suresh
 */
public class AppSecret {

  /** Secret path param name used in rest controller. */
  public static final String APP_SECRET_PARAM = "secretName";

  /** The attributes used in secret metadata */
  public static final String FILENAME_METADATA = "filename";

  public static final String USERID_METADATA = "_userId";
  public static final String DESC_METADATA = "_desc";

  /** '@' is used as unique name separator as it's url safe. */
  public static final String UNIQ_NAME_SEP = "@";

  private final String secretName;
  private final AppGroup group;

  /**
   * Constructor for app secret.
   *
   * @param secretName secret name.
   * @param domain Application domain
   * @param appName Application name.
   */
  public AppSecret(@Nonnull String secretName, @Nonnull String domain, @Nonnull String appName) {
    this.secretName = secretName;
    this.group = AppGroup.from(domain, appName);
  }

  /**
   * Constructor for app secret.
   *
   * @param secretName secret name.
   * @param group {@link AppGroup}
   */
  public AppSecret(@Nonnull String secretName, @Nonnull AppGroup group) {
    this.secretName = secretName;
    this.group = group;
  }

  /**
   * Create {@link AppSecret} from the globally unique secret name.
   *
   * @param uniqSecretName globally unique secret name. It has the format
   *     <b>{domain}_{AppGroupName}@{secretName}</b>
   */
  public AppSecret(@Nonnull String uniqSecretName) {
    String[] names = uniqSecretName.split(UNIQ_NAME_SEP, 2);
    if (names.length != 2) {
      throw new IllegalArgumentException("Invalid secret name: " + uniqSecretName);
    }

    String prefix = names[0];
    secretName = names[1];

    String[] groups = prefix.split(GROUP_SEP, 2);
    if (groups.length != 2) {
      throw new IllegalArgumentException("Invalid app group prefix: " + prefix);
    }
    group = new AppGroup(groups[0], groups[1]);
  }

  /**
   * Returns the secret name used by the user. The secret name is usually used as the secret file
   * name in applications.
   *
   * @return secret name.
   */
  public String getSecretName() {
    return secretName;
  }

  /**
   * Returns the application group (env) details for the secret ({@link #secretName})
   *
   * @return {@link AppGroup}
   */
  public AppGroup getGroup() {
    return group;
  }

  /**
   * Keywhiz is designed to have globally unique names. This method returns a globally unique secret
   * name based on your application group.
   *
   * <p>The unique secret name format is := <b>{domain}_{AppGroupName}@{secretName}</b>
   *
   * <p>Eg: If user uploads a secret with name <b>db-password.txt</b> for an env <b>prod</b>,
   * assembly <b>my-app</b> and org <b>oneops</b>, the app id is <b>oneops_my-app_prod</b> and the
   * secret name stored in keywhiz server will be <b>prod_oneops_my-app_prod@db-password.txt</b>.
   * The initial <b>prod_</b> is the default domain. The domain is mainly used to support multiple
   * OneOps instances.
   *
   * @return String globally unique secret name.
   */
  public String getUniqSecretName() {
    return String.format(
        "%s%s%s%s%s", group.getDomain(), GROUP_SEP, group.getName(), UNIQ_NAME_SEP, secretName);
  }

  /**
   * Returns the keywhiz group name.
   *
   * @return Group name.
   */
  public String getGroupName() {
    return group.getGroupName();
  }

  /**
   * Returns the app name.
   *
   * @return App name..
   */
  public String getAppName() {
    return group.getName();
  }

  @Override
  public String toString() {
    return "AppSecret{" + "secretName='" + secretName + '\'' + ", group=" + group + '}';
  }
}
