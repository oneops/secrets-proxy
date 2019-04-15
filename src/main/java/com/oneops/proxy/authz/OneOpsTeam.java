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
package com.oneops.proxy.authz;

import org.springframework.beans.factory.annotation.Value;

/**
 * OneOps team model, used for authorization check.
 *
 * @author Suresh
 */
public class OneOpsTeam {

  /** The default team name the users to be part of to manage keywhiz secrets. */
  public static final String SECRETS_ADMIN_TEAM = "secrets-admin";

  /** Prefix for restricted team name in OneOps */
  @Value("${oneops.restricted-team-prefix:sox-}")
  private String restrictedTeamPrefix;

  private final String name;
  private final String description;
  private final boolean design;
  private final boolean transition;
  private final boolean operation;

  public OneOpsTeam(
      String name, String description, boolean design, boolean transition, boolean operation) {
    this.name = name;
    this.description = description;
    this.design = design;
    this.transition = transition;
    this.operation = operation;
  }

  /**
   * Accessor for team name.
   *
   * @return team name
   */
  public String getName() {
    return name;
  }

  /**
   * Accessor for team description.
   *
   * @return team description.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Is design permission enabled for this team.
   *
   * @return <code>true</code> if the permission is enabled.
   */
  public boolean isDesign() {
    return design;
  }

  /**
   * Is transition permission enabled for this team.
   *
   * @return <code>true</code> if the permission is enabled.
   */
  public boolean isTransition() {
    return transition;
  }

  /**
   * Is operation permission enabled for this team.
   *
   * @return <code>true</code> if the permission is enabled.
   */
  public boolean isOperation() {
    return operation;
  }

  /**
   * Checks if it's Secrets Admin team.
   *
   * @return <code>true</code> if the team name is {@link #SECRETS_ADMIN_TEAM}
   */
  public boolean isSecretsAdmin() {
    return SECRETS_ADMIN_TEAM.equalsIgnoreCase(name);
  }

  /**
   * Checks if team is a Secrets admin for the given assembly. For a team to be Secrets Admin, the
   * following naming conventions are used.
   *
   * <ul>
   *   <li>Team name should <b>secrets-admin</b> OR
   *   <li>Team name should <b>secrets-admin-${AssemblyName}</b>
   *   <li>For a SOX assembly, team name should <b>sox-secrets-admin-${AssemblyName}</b>
   * </ul>
   *
   * @return <code>true</code> if the team is a Secrets admin for given assembly.
   */
  public boolean isSecretsAdmin(String assembly) {
    String assemblySecretsAdmin = SECRETS_ADMIN_TEAM + "-" + assembly;
    return isSecretsAdmin()
        || assemblySecretsAdmin.equalsIgnoreCase(name)
        || (restrictedTeamPrefix + assemblySecretsAdmin).equalsIgnoreCase(name);
  }

  @Override
  public String toString() {
    return "OneOpsTeam{"
        + "name='"
        + name
        + '\''
        + ", description='"
        + description
        + '\''
        + ", design="
        + design
        + ", transition="
        + transition
        + ", operation="
        + operation
        + '}';
  }
}
