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

import com.oneops.proxy.model.AppGroup;
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
 * "Admin" privilege (ie, part of <strong>secrets-admin</strong> team) for an assembly
 * is authorized to add/update/delete the secrets.
 * <p>
 * Note: JOOQ query has 1:1 mapping with SQL and is very easy to understand. If you want
 * to see the generated SQL (for debugging), add <b>org.jooq.tools: DEBUG</b> in
 * application.yaml file.
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
     * Returns all teams having given user in the application group assembly.
     *
     * @param userName oneops user name (usually it's your AD/LDAP user name)
     * @param appGroup {@link AppGroup}
     * @return List of {@link OneOpsTeam}
     */
    public List<OneOpsTeam> getTeams(@Nonnull final String userName, @Nonnull final AppGroup appGroup) {
        log.debug("Retrieving teams having user: " + userName + " for application group: " + appGroup.getNsPath());
        Condition teamCondition = USERS.USERNAME.equalIgnoreCase(userName)
                .and(CI_PROXIES.NS_PATH.equalIgnoreCase(appGroup.getOrgNsPath())
                        .and(CI_PROXIES.CI_NAME.equalIgnoreCase(appGroup.getAssembly())
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
     * Returns all teams in the application group assembly.
     *
     * @param appGroup {@link AppGroup}
     * @return List of {@link OneOpsTeam}
     */
    public List<OneOpsTeam> getAllTeams(@Nonnull final AppGroup appGroup) {
        log.debug("Retrieving all teams for application group: " + appGroup.getNsPath());
        Condition teamCondition = CI_PROXIES.NS_PATH.equalIgnoreCase(appGroup.getOrgNsPath())
                .and(CI_PROXIES.CI_NAME.equalIgnoreCase(appGroup.getAssembly())
                        .and(CI_PROXIES.CI_CLASS_NAME.eq("account.Assembly")));

        // Read like SQL :)
        Result<Record> records = dslContext.select(TEAMS.fields()).from(CI_PROXIES).
                innerJoin(CI_PROXIES_TEAMS).on(CI_PROXIES.ID.eq(CI_PROXIES_TEAMS.CI_PROXY_ID)).
                innerJoin(TEAMS).on(TEAMS.ID.eq(CI_PROXIES_TEAMS.TEAM_ID)).
                where(teamCondition).fetch();

        return records.stream().map(UserRepository::mapRecord).collect(Collectors.toList());
    }

}
