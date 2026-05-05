package org.zeroagent.domain.common.env;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.zeroagent.common.enums.ICode;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月27日  17时49分
 */
@Getter
@RequiredArgsConstructor
public enum StandardEnv implements ICode {
    LOCAL("本地环境"),
    DEV("开发环境"),
    PROD("生产环境")

    ;
    private final String desc;
    @Override
    public String getCode() {
        return this.name();
    }

    @Override
    public String getDesc() {
        return this.desc;
    }
}
