package org.zeroagent.common.result;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.zeroagent.common.problem.error.ErrorCode;
import org.zeroagent.common.problem.error.ErrorCodeImpl;

/**
 * @author chenhua
 * @version 2026年03月03日  19时54分
 * @Description:
 */
@Data
@RequiredArgsConstructor
public abstract class ApiCoreResult<E> {
    /**
     *  是否成功
     */
    private final boolean success;
    /**
     *  错误码
     */
    private final String errorCode;
    /**
     *  错误信息
     */
    private final String errorMsg;
    /**
     *  实体数据
     */
    private final E data;
    public ErrorCode toErrorCode() {
        return ErrorCodeImpl.of(this.errorCode, this.errorMsg);
    }
}
