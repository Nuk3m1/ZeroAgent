package org.zeroagent.common.problem.error;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * @author chenhua
 * @version 2026年03月03日  19时57分
 * @Description:
 */

@Data
@RequiredArgsConstructor(staticName = "of")
public class ErrorCodeImpl implements ErrorCode {
    private final String code;
    private final String msg;
}
