package org.zeroagent.common.page;

import java.io.Serial;

/**
 * 分页条件请求参数异常
 */
public class PageRequestException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 6488519515373456609L;

    /**
     * 构造器
     */
    public PageRequestException(String message) {
        super(message);
    }
}
