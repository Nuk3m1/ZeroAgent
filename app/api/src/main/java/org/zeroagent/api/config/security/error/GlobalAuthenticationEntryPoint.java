package org.zeroagent.api.config.security.error;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.zeroagent.common.problem.error.ErrorCode;

import org.zeroagent.common.result.ApiResult;
import org.zeroagent.common.utils.json.JSON;
import org.zeroagent.domain.core.user.error.UserErrorCode;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * 身份认证 全局异常处理器
 * @author Nuk3m1
 * @version 2026年04月28日  17时16分
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        ErrorCode errorCode = getErrorCode(authException);
        log.warn("Authentication failed , ErrorCode : {}", errorCode, authException);

        if (response.isCommitted()) {
            log.warn("Response has already been committed");
            return;
        }
        // 401
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        String result = JSON.toJSONString(ApiResult.error(errorCode));
        try (PrintWriter writer = response.getWriter()) {
            writer.write(result);
        }
        response.flushBuffer();

    }


    private static ErrorCode getErrorCode(AuthenticationException exception) {
        ErrorCode errorCode = UserErrorCode.USER_NOT_LOGIN;
        if (exception instanceof BizAuthenticationException bizAuthenticationException) {
            errorCode = bizAuthenticationException.getErrorCode();
        }
        return errorCode;
    }
}
