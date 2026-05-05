package org.zeroagent.api.config.security.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *  Token 管理接口
 * @author Nuk3m1
 * @version 2026年04月27日  00时26分
 */
@FunctionalInterface
public interface TokenAuthManager {

    /**
     * 身份认证
     * @param request -
     * @param response -
     * @return HttpServletRequest -
     */
    HttpServletRequest authenticate(final HttpServletRequest request, final HttpServletResponse response);
}
