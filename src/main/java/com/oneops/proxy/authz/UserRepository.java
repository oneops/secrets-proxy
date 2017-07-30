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

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

import static com.oneops.user.Tables.TEAMS_USERS;
import static com.oneops.user.tables.CiProxies.CI_PROXIES;
import static com.oneops.user.tables.CiProxiesTeams.CI_PROXIES_TEAMS;
import static com.oneops.user.tables.Teams.TEAMS;
import static com.oneops.user.tables.Users.USERS;

/**
 * User repository class to fetch OneOps user/team/group info. This is mainly used to
 * enforce the ACL for application groups, which is the env nspath. Only the user with
 * "Admin" privilege (ie, part of <strong>keywhiz-admin</strong> team) for an assembly
 * is authorized to add/update/delete the secrets.
 * <p>
 * Note: JOOQ query has 1:1 mapping with SQL and is very easy to understand. If you want
 * to see the generated SQL (for debugging), add <b>org.jooq.tools: DEBUG</b> to
 * application.yaml.
 *
 * @author Suresh
 */
//@Transactional
@Repository
public class UserRepository {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private DSLContext dslContext;

    public UserRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    /**
     * A helper method to map the team record to {@link OneOpsTeam}
     */
    private static OneOpsTeam mapRecord(Record r) {
        return r.into(TEAMS.NAME, TEAMS.DESCRIPTION, TEAMS.DESIGN, TEAMS.TRANSITION, TEAMS.OPERATIONS).into(OneOpsTeam.class);
    }


    /**
     * Returns all the teams having the given user in the given org and assembly.
     *
     * @param userName oneops user name (usually it's your AD/LDAP user name)
     * @param org      oneops org name
     * @param assembly oneops assembly name
     * @return List of {@link OneOpsTeam}
     */
    public List<OneOpsTeam> getTeams(@Nonnull final String userName, @Nonnull final String org, @Nonnull final String assembly) {
        log.debug("Retrieving teams having user: " + userName + " in assembly: /" + org + "/" + assembly);
        Condition teamCondition = USERS.USERNAME.equalIgnoreCase(userName)
                .and(CI_PROXIES.NS_PATH.equalIgnoreCase("/" + org)
                        .and(CI_PROXIES.CI_NAME.equalIgnoreCase(assembly)
                                .and(CI_PROXIES.CI_CLASS_NAME.eq("account.Assembly"))));
        // Read like SQL :)
        Result<Record> records = dslContext.select(TEAMS.fields()).from(CI_PROXIES)
                .innerJoin(CI_PROXIES_TEAMS).on(CI_PROXIES.ID.eq(CI_PROXIES_TEAMS.CI_PROXY_ID))
                .innerJoin(TEAMS).on(TEAMS.ID.eq(CI_PROXIES_TEAMS.TEAM_ID))
                .innerJoin(TEAMS_USERS).on(TEAMS_USERS.TEAM_ID.eq(TEAMS.ID))
                .innerJoin(USERS).on(USERS.ID.eq(TEAMS_USERS.USER_ID))
                .where(teamCondition).fetch();

        return records.stream().map(UserRepository::mapRecord).collect(Collectors.toList());
    }

    /**
     * Returns all the teams in the given org and assembly.
     *
     * @param org      oneops org name
     * @param assembly oneops assembly name
     * @return List of {@link OneOpsTeam}
     */
    public List<OneOpsTeam> getAllTeams(@Nonnull final String org, @Nonnull final String assembly) {
        log.debug("Retrieving all teams for assembly: /" + org + "/" + assembly);
        Condition teamCondition = CI_PROXIES.NS_PATH.equalIgnoreCase("/" + org)
                .and(CI_PROXIES.CI_NAME.equalIgnoreCase(assembly)
                        .and(CI_PROXIES.CI_CLASS_NAME.eq("account.Assembly")));

        // Read like SQL :)
        Result<Record> records = dslContext.select(TEAMS.fields()).from(CI_PROXIES).
                innerJoin(CI_PROXIES_TEAMS).on(CI_PROXIES.ID.eq(CI_PROXIES_TEAMS.CI_PROXY_ID)).
                innerJoin(TEAMS).on(TEAMS.ID.eq(CI_PROXIES_TEAMS.TEAM_ID)).
                where(teamCondition).fetch();

        return records.stream().map(UserRepository::mapRecord).collect(Collectors.toList());
    }

}
