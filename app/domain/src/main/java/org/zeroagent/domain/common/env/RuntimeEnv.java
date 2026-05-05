package org.zeroagent.domain.common.env;

import lombok.RequiredArgsConstructor;

import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

/**
 * 环境控制服务 (本地/开发/生产)
 * @author Nuk3m1
 * @version 2026年04月27日  17时47分
 */
@Component
@RequiredArgsConstructor
public class RuntimeEnv {
    private static final Profiles LOCAL = Profiles.of("local");
    private static final Profiles DEV   = Profiles.of("dev");
    private static final Profiles PROD  = Profiles.of("prod");

    private final Environment environment;

    public StandardEnv getEnv() {
        if (this.isLocal()) {
            return StandardEnv.LOCAL;
        }
        if (this.isDev()) {
            return StandardEnv.DEV;
        }
        return StandardEnv.PROD;
    }

    /**
     * 是否为 LOCAL 环境
     * @return -
     */
    public boolean isLocal() {
        return environment.acceptsProfiles(LOCAL);
    }
    public boolean isNotLocal() {
        return !isLocal();
    }

    /**
     * 是否为 DEV 环境
     * @return -
     */
    public boolean isDev() {
        return environment.acceptsProfiles(DEV);
    }
    public boolean isNotDev() {
        return !isDev();
    }

    /**
     * 是否为 PROD 环境
     * @return -
     */
    public boolean isProd() {
        return environment.acceptsProfiles(PROD);
    }
    public boolean isNotProd() {
        return !isProd();
    }

    /**
     * 是否为线下环境
     * @return -
     */
    public boolean isOffLine() {
        return this.isLocal() || this.isDev();
    }

    /**
     * 是否为线上环境
     * @return -
     */
    public boolean isOnLine() {
        return this.isProd();
    }

}
