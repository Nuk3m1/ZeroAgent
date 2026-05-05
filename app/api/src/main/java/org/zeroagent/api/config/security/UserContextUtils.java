package org.zeroagent.api.config.security;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.zeroagent.common.utils.Validates;
import org.zeroagent.domain.core.auth.model.CustomizeUser;
import org.zeroagent.domain.core.auth.model.UserContext;
import org.zeroagent.domain.core.user.error.UserErrorCode;

import java.util.Objects;
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
        if (Objects.isNull(authentication) || Objects.isNull(authentication.getPrincipal())
            || !(authentication.getPrincipal() instanceof CustomizeUser user)) {
            return Optional.empty();
        }
        UserContext userContext = UserContext.builder()
                .sessionUid(user.getUid())
                .sessionUserName(user.getUsername())
                .phone(user.getPhone())
                .email(user.getEmail())
                .build();

        return Optional.of(userContext);
    }

    public static UserContext getUserContext() {
        UserContext userContext = getOptionalUserContext().orElse(null);
        Validates.notNull(userContext, UserErrorCode.USER_NOT_LOGIN);
        return userContext;
    }

    /**
     * 获取登录用户账号uid
     */
    public static long getSessionUid() {
        return getUserContext().getSessionUid();
    }



}
