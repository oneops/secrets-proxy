/*******************************************************************************
 *
 *   Copyright 2017 Walmart, Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *******************************************************************************/
package com.oneops.proxy.authz;

/**
 * OneOps team model, used for authorization check.
 *
 * @author Suresh
 */
public class OneOpsTeam {

    /**
     * The default team name the users to be part of to manage keywhiz secrets.
     */
    public static final String KEYWHIZ_ADMIN_TEAM = "keywhiz-admin";

    private final String name;
    private final String description;
    private final boolean design;
    private final boolean transition;
    private final boolean operation;

    public OneOpsTeam(String name, String description, boolean design, boolean transition, boolean operation) {
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
     * Checks if it's Keywhiz Admin team.
     *
     * @return <code>true</code> if the team name is {@link #KEYWHIZ_ADMIN_TEAM}
     */
    public boolean isKeywhizAdmin() {
        return KEYWHIZ_ADMIN_TEAM.equalsIgnoreCase(name);
    }

    /**
     * Checks if team is a Keywhiz admin for the given assembly. For a team to be
     * Keywhiz Admin, the following naming conventions are used.
     * <ul>
     * <p>
     * <li> Team name should <b>keywhiz-admin</b>  OR
     * <li> Team name should <b>keywhiz-admin-${AssemblyName}</b>
     *
     * @return <code>true</code> if the team is a Keywhiz admin for given assembly.
     */
    public boolean isKeywhizAdmin(String assembly) {
        String assemblyKeywhizAdmin = KEYWHIZ_ADMIN_TEAM + "-" + assembly;
        return isKeywhizAdmin() || assemblyKeywhizAdmin.equalsIgnoreCase(name);
    }


    @Override
    public String toString() {
        return "OneOpsTeam{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", design=" + design +
                ", transition=" + transition +
                ", operation=" + operation +
                '}';
    }
}
