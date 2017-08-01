package com.oneops.proxy.security.annotations;

import com.oneops.proxy.auth.user.OneOpsUser;
import com.oneops.proxy.authz.Authz;
import com.oneops.proxy.model.AppGroup;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A meta annotation to enable Spring security's <b>Pre-Authorization</b>
 * check for all the secure resource access. It's configured to invoke
 * {@link Authz#isAuthorized(AppGroup, OneOpsUser)} with keywhiz application
 * group name as first argument and the current authenticated {@link OneOpsUser}
 * as second argument.
 * <ul>
 * <li> <code>appGroup</code> is the name of path argument used in RestControllers.
 * <li> <code>principal</code> is the current authenticated user, provided by spring
 * security.
 * </ul>
 * {@link PreAuthorize} uses <code>Spring Expression Language (SPEL)</code> to invoke
 * the {@link Authz#isAuthorized(AppGroup, OneOpsUser)} method.
 *
 * @author Suresh G
 */
@Target({METHOD, TYPE})
@Retention(RUNTIME)
@PreAuthorize("@authz.isAuthorized(#appGroup,principal)")
public @interface AuthorizeGroup {
}
