package org.zeroagent.api.config.security.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.zeroagent.common.model.OperatorSource;
import org.zeroagent.domain.common.utils.crypto.JwtUtils;
import org.zeroagent.domain.core.auth.model.CustomizeUser;

/**
 * 登录认证
 * @author Nuk3m1
 * @version 2026年04月27日  00时26分
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebJwtTokenAuthManager implements TokenAuthManager {
    private final UserDetailsService userDetailsService;

    @Nullable
    public static String getToken(String bearerToken) {
        if (StringUtils.isBlank(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @Override
    public HttpServletRequest authenticate(HttpServletRequest request, HttpServletResponse response) {
        String token = getToken(request.getHeader("Authorization"));
        if (token == null) {
            return request;
        }
        // 校验 token 完整性和 channel
        if (!JwtUtils.validateToken(token)) {
            log.warn("[WEB_AUTH] token invalid , token :{}", token);
            return request;
        }
        if (JwtUtils.isQQLoginToken(token)) {
            log.warn("[WEB_AUTH] token channel incorrect, token : {}", token);
            return request;
        }
        final String userName = JwtUtils.getUserName(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
        // 设置登录态用户
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        CustomizeUser customizeUser = (CustomizeUser) userDetails;
        customizeUser.setOperatorSource(OperatorSource.WEB);

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        request.setAttribute("AUTHENTICATED_USERNAME", userName);
        return request;
    }
}
