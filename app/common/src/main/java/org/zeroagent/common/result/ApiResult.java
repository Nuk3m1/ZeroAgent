package org.zeroagent.common.result;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.zeroagent.common.problem.error.ErrorCode;

import java.beans.ConstructorProperties;
import java.util.function.Function;

/**
 * @author chenhua
 * @version 2026年03月03日  20时07分
 * @Description:
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ApiResult<E> extends ApiCoreResult<E>{
    /**
     * @param success   是否成功
     * @param errorCode 错误码
     * @param errMsg    错误信息
     * @param data      实际对象
     */
    @ConstructorProperties({"success", "errorCode", "errMsg", "data"})
    public ApiResult(boolean success, String errorCode, String errMsg, E data) {
        super(success, errorCode, errMsg, data);
    }
    /**
     * 处理实际对象
     *
     * @param mapper 处理函数
     * @param <T>    实际对象类型
     * @return 结果
     */
    public <T> ApiResult<T> map(Function<E, T> mapper) {
        T data = mapper.apply(getData());
        return new ApiResult<>(isSuccess(), getErrorCode(), getErrorMsg(), data);
    }

    /**
     * 构造成功结果
     */
    public static <E> ApiResult<E> success(E data) {
        return build(true, null, null, data);
    }

    public static <E> ApiResult<E> success() {
        return success((E) null);
    }

    /**
     * 构造失败结果
     */
    public static <E> ApiResult<E> error(String errorCode, String errorMsg) {
        return build(false, errorCode, errorMsg, null);
    }

    public static <E> ApiResult<E> error(ErrorCode errorCode) {
        return error(errorCode.getCode(), errorCode.getMsg());
    }


    private static <E> ApiResult<E> build(boolean success, String errorCode, String errorMsg, E data) {
        return new ApiResult<>(success, errorCode, errorMsg, data);
    }
}
