package org.zeroagent.api.config.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.filter.OncePerRequestFilter;
import org.zeroagent.api.common.util.MvcUtil;
import org.zeroagent.domain.support.notification.app.AppAlertHelper;

import java.io.IOException;

/**
 * 全局访问 鉴权链
 * @author Nuk3m1
 * @version 2026年04月28日  13时39分
 */
@Slf4j
@RequiredArgsConstructor
public class PublicAuthTokenIfExistFilter extends OncePerRequestFilter {
    public final static String AUTHENTICATED_USERNAME = MvcUtil.AUTHENTICATED_USERNAME;
    private final static String JWT                   = "Bearer ";

    private final WebJwtTokenAuthManager webJwtTokenAuthManager;
    private final AppAlertHelper         appAlertHelper;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        try {
            if (this.isJwtWeb(authorization)) {
                request = webJwtTokenAuthManager.authenticate(request, response);
            }
        } catch (Exception e) {
            // 全局访问 直接放行
        }
        filterChain.doFilter(request, response);

    }
    private boolean isJwtWeb(@Nullable String token) {
        return StringUtils.startsWith(token, JWT);
    }
}
