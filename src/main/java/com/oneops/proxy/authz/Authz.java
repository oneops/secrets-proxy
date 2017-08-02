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

import com.oneops.proxy.auth.user.OneOpsUser;
import com.oneops.proxy.model.AppGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;

import static com.oneops.proxy.authz.OneOpsTeam.SECRETS_ADMIN_TEAM;

/**
 * Implements Keywhiz application group authorization logic. The application group
 * name is the <b>env nspath</b>, which is of the format.
 * <p>
 * <b>{org}_{assembly}_{env}</b>
 *
 * @author Suresh
 */
@Component
public class Authz {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final UserRepository userRepo;

    public Authz(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    /**
     * Checks if {@link OneOpsUser} is authorized to manage secrets for the given application group.
     * Env nspath is used as the application group with the <b>{org}_{assembly}_{env}</b> format.
     *
     * @param appGroup Application group.
     * @param user     Authenticated user.
     * @return <code>true</code> if the user is authorized.
     */
    public boolean isAuthorized(@Nonnull AppGroup appGroup, @Nonnull OneOpsUser user) {
        if (log.isDebugEnabled()) {
            log.debug("Checking the authz for user: " + user.getUsername() + " and application group: " + appGroup.getName());
        }

        List<OneOpsTeam> teams = userRepo.getTeams(user.getUsername(), appGroup);
        boolean hasAccess = teams.stream().anyMatch(t -> t.isSecretsAdmin(appGroup.getAssembly()));
        if (!hasAccess) {
            throw new AuthorizationServiceException("OneOps user '" + user.getCn() + "' is not a '"
                    + SECRETS_ADMIN_TEAM + "' or not authorized to manage the secrets for environment: "
                    + appGroup.getNsPath());
        }
        return true;
    }
}
