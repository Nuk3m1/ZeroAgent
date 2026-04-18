package org.zeroagent.domain.common.reactive;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.zeroagent.common.problem.error.ErrorCode;
import org.zeroagent.common.problem.exception.SysException;
import org.zeroagent.common.utils.FormatUtil;

import java.util.function.Function;

/**
 * @author Nuk3m1
 * @version 2026年03月10日  17时05分
 */
@Slf4j
@UtilityClass
public class ErrorMapper {
    public Function<? super Throwable, ? extends Throwable> sysException(ErrorCode errorCode) {
        return throwable -> {
            log.error(errorCode.getMsg(), throwable);
            return new SysException(errorCode, throwable);
        };
    }

    public Function<? super Throwable, ? extends Throwable> sysException(ErrorCode errorCode, Object... args) {
        return throwable -> {
          log.error(errorCode.getMsg(), args, throwable);
          return new SysException(errorCode, FormatUtil.buildMessage(errorCode.getMsg(), args), throwable);
        };
    }
}
