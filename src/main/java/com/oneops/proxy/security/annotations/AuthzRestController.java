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
package com.oneops.proxy.security.annotations;

import com.oneops.proxy.auth.user.OneOpsUser;
import com.oneops.proxy.authz.Authz;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.*;

/**
 * A meta annotation for {@link RestController} with a pre authorization for Keywhiz application
 * group name. All {@link AuthzRestController} methods should have {@link OneOpsUser} as mandatory
 * method parameter for Pre Authorization ({@link Authz#isAuthorized(String, OneOpsUser)}).
 *
 * @author Suresh G
 */
@RestController
@AuthorizeGroup
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthzRestController {}
