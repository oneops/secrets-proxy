package com.oneops.proxy.security.annotations;

import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A meta annotation for {@link RestController} with a pre authorization
 * for Keywhiz application group name.
 *
 * @author Suresh G
 */
@RestController
@AuthorizeGroup
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthzRestController {
}
