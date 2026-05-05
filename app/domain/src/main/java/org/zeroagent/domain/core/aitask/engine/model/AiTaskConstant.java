package org.zeroagent.domain.core.aitask.engine.model;

import lombok.experimental.UtilityClass;

/**
 * AiTask 领域 - 常量
 * @author Nuk3m1
 * @version 2026年05月02日  17时49分
 */
@UtilityClass
public class AiTaskConstant {
    /**
     * 业务异常描述
     */
    public static final String BIZ_ERROR_MSG            = "bizErrorMsg";
    /**
     * 违禁中文词
     */
    public static final String BANNED_WORD              = "bannedWord";
    /**
     * 业务状态码-执行中
     */
    public static final String BIZ_STATUS_PROCESSING    = "PROCESSING";
    /**
     * 业务状态码-成功
     */
    public static final String BIZ_STATUS_SUCCESS       = "SUCCESS";
    /**
     * 业务状态码-失败
     */
    public static final String BIZ_STATUS_FAILURE       = "FAILURE";
    /**
     * 业务状态码-超时
     */
    public static final String BIZ_STATUS_TIMEOUT       = "TIMEOUT";

    /**
     * 业务状态码-超限
     */
    public static final String BIZ_STATUS_TRYOUT        = "TRYOUT";
    /**
     * 业务状态码-重试中
     */
    public static final String BIZ_STATUS_RETRYING      = "RETRYING";
}
