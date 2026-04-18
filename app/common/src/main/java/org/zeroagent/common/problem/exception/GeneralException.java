package org.zeroagent.common.problem.exception;

import org.zeroagent.common.problem.error.ErrorCode;

public interface GeneralException {
    ErrorCode getErrorCode();
}
