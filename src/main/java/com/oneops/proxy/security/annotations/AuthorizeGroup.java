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
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A meta annotation to enable Spring security's Pre-Authorization check for all the secure resource
 * access. It's configured to invoke {@link Authz#isAuthorized(String, OneOpsUser)} with oneops
 * application name and the current authenticated {@link OneOpsUser} as arguments.
 *
 * <ul>
 *   <li><code>appName</code> is the OneOps application name.
 *   <li><code>principal</code> is the current authenticated user, provided by spring security.
 * </ul>
 *
 * {@link PreAuthorize} uses <code>Spring Expression Language (SPEL)</code> to invoke the {@link
 * Authz#isAuthorized(String, OneOpsUser)} method.
 *
 * @author Suresh G
 */
@Target({METHOD, TYPE})
@Retention(RUNTIME)
@PreAuthorize("@authz.isAuthorized(#appName,principal)")
public @interface AuthorizeGroup {}
