package org.zeroagent.common.problem.error;

/**
 * @author chenhua
 * @version 2026年03月03日  19时56分
 * @Description:
 */
public interface ErrorCode {
    /**
     * 错误码
     *
     * @return 错误码
     */
    String getCode();

    /**
     * 错误信息
     *
     * @return 错误信息
     */
    String getMsg();
}
