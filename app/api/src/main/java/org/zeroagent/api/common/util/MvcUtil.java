package org.zeroagent.api.common.util;

import io.micrometer.common.KeyValue;
import jakarta.annotation.Nullable;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jodd.util.StringPool;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.observation.ServerHttpObservationDocumentation;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.web.filter.ServerHttpObservationFilter;
import org.springframework.web.util.WebUtils;

import java.util.Objects;

/**
 * Spring MVC 网络容器管理工具类
 * @author Nuk3m1
 * @version 2026年04月27日  19时59分
 */
@UtilityClass
public class MvcUtil {

    public static final String AUTHENTICATED_USERNAME = "AUTHENTICATED_USERNAME";

    public String toLogMessage(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        StringBuilder requestStr = new StringBuilder()
                .append("{")
                .append(getServerName(request))
                .append("} ");
        final String uri;
        final String method;
        if (request.getDispatcherType() == DispatcherType.ERROR) {
            uri = getOriginalUrl(request);
            method = getOriginalMethod(request);
        } else {
            uri = request.getRequestURI();
            method = request.getMethod();
        }
        if (request.getDispatcherType() != DispatcherType.REQUEST) {
            requestStr.append(request.getDispatcherType()).append(" ");
        }
        requestStr.append(method).append(" ").append(uri);
        String queryString = request.getQueryString();
        if (StringUtils.isNotBlank(queryString)) {
            requestStr.append(StringPool.QUESTION_MARK).append(queryString);
        }
        return requestStr.toString();
    }


    public String toLogMessage(@Nullable HttpServletResponse response) {
        if (response == null) {
            return "";
        }
        return response.getStatus() + "committed=" + response.isCommitted();
    }

    public boolean isCommitted(@Nullable HttpServletResponse response) {
        return response != null && response.isCommitted();
    }

    private String getOriginalUrl(HttpServletRequest request) {
        return String.valueOf(request.getAttribute(WebUtils.ERROR_REQUEST_URI_ATTRIBUTE));
    }


    @Nullable
    private String getOriginalMethod(HttpServletRequest request) {
        Object observationContext = request.getAttribute(ServerHttpObservationFilter.CURRENT_OBSERVATION_CONTEXT_ATTRIBUTE);
        if (observationContext instanceof ServerRequestObservationContext context) {
            String keyName = ServerHttpObservationDocumentation.LowCardinalityKeyNames.METHOD.asString();
            KeyValue originalMethod = context.getLowCardinalityKeyValue(keyName);
            return originalMethod.getValue();
        }
        return null;
    }

    public String getAuthenticatedUserName(HttpServletRequest request) {
        return Objects.toString(request.getAttribute(AUTHENTICATED_USERNAME), "");
    }

    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            if (ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
        } else {
            ip = request.getHeader("X-Real-IP");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
        }
        return ip;
    }

    public static String getServerName(HttpServletRequest request) {
        String serverName = request.getHeader("X-Server-Name");
        if (serverName == null || serverName.isEmpty()) {
            serverName = request.getServerName();
        }
        return serverName;
    }
}
