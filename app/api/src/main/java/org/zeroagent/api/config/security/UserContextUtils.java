package org.zeroagent.api.config.security;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.zeroagent.domain.core.auth.model.UserContext;

import java.util.Optional;

/**
 *
 * @author Nuk3m1
 * @version 2026年03月15日  20时39分
 */
@UtilityClass
public class UserContextUtils {
    public static Optional<UserContext> getOptionalUserContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return null;
    }

}
