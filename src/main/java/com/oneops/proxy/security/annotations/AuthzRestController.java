package com.oneops.proxy.security.annotations;

import com.oneops.proxy.auth.user.OneOpsUser;
import com.oneops.proxy.authz.Authz;
import com.oneops.proxy.model.AppGroup;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A meta annotation for {@link RestController} with a pre authorization for Keywhiz
 * application group name. All {@link AuthzRestController} methods should have both
 * {@link AppGroup} and {@link OneOpsUser} as mandatory method parameters for
 * Pre Authorization ({@link Authz#isAuthorized(AppGroup, OneOpsUser)}). These two arguments
 * will get injected automatically with proper values.
 *
 * @author Suresh G
 */
@RestController
@AuthorizeGroup
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthzRestController {
}
