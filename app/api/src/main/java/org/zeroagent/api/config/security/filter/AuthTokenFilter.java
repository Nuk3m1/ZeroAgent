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
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.filter.OncePerRequestFilter;
import org.zeroagent.api.common.util.MvcUtil;
import org.zeroagent.api.config.security.error.BizAuthenticationException;
import org.zeroagent.common.problem.exception.BizException;
import org.zeroagent.domain.core.user.error.UserErrorCode;
import org.zeroagent.domain.support.notification.app.AppAlertHelper;

import java.io.IOException;
import java.nio.file.AccessDeniedException;

/**
 * 身份认证 鉴权链
 * @author Nuk3m1
 * @version 2026年04月27日  19时33分
 */
@Slf4j
@RequiredArgsConstructor
public class AuthTokenFilter extends OncePerRequestFilter {
    public final static String AUTHENTICATED_USERNAME = MvcUtil.AUTHENTICATED_USERNAME;

    private final static String JWT                   = "Bearer ";

    private final WebJwtTokenAuthManager webJwtTokenAuthManager;
    private final AppAlertHelper         appAlertHelper;
    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        @Nullable String authorization = request.getHeader("Authorization");
        try {
            if (this.isJwtWeb(authorization)) {
                request = webJwtTokenAuthManager.authenticate(request, response);
            } else {
                // TODO 后续 其他端 的 鉴权链条 在这里分离
                throw new BizAuthenticationException(UserErrorCode.USER_NOT_LOGIN);
            }
        } catch (BizAuthenticationException e) {
            throw e;
        } catch (AuthenticationException e) {
            String ip = MvcUtil.getClientIp(request);
            String requestLog = MvcUtil.toLogMessage(request);
            log.warn("[Authenticate] auth error , request = [{}], auth = [{}], ip = [{}]", requestLog, authorization, ip, e);
            throw e;
        } catch (BizException e) {
            // 此处抛出 AuthenticationException 或子类型 到下游鉴权
            String ip =  MvcUtil.getClientIp(request);
            String requestLog = MvcUtil.toLogMessage(request);
            log.error("[Authenticate] biz error , request = [{}], auth = [{}], ip = [{}]", requestLog, authorization, ip, e);
            throw new BizAuthenticationException(e.getErrorCode(), e);
        } catch (Exception e) {
            String ip = MvcUtil.getClientIp(request);
            String requestLog = MvcUtil.toLogMessage(request);
            log.error("[Authenticate] system error , request = [{}], auth = [{}], ip = [{}]", requestLog, authorization, ip, e);
            appAlertHelper.alertText("[Authenticate] system error , request = [{}], auth = [{}], ip = [{}]", requestLog, authorization, ip, e);
            throw new BizAuthenticationException(UserErrorCode.USER_NOT_LOGIN, e);
        }
        filterChain.doFilter(request, response);

    }

    private boolean isJwtWeb(@Nullable String token) {
        return StringUtils.startsWith(token, JWT);
    }
}
