package org.zeroagent.common.problem.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {
    ILLEGAL_PARAM("参数错误"),
    UNSPECIFIED("系统繁忙"),
    SLA_LIMITED("访问量过大"),
    FETCH_IP_ERROR("获取IP地址失败"),
    ID_GENERATE_FAIL("ID生成失败"),
    RECORD_NOT_FOUND("记录不存在")
    ;


    private final String msg;

    @Override
    public String getCode() {
        return this.name();
    }

}
