package org.zeroagent.common.mapper;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 *
 * @author Nuk3m1
 * @version 2026年03月11日  14时50分
 */
@AutoConfiguration
public class TimeMapperAutoConfiguration {
    @Bean
    public TimeMapper timeMapper() {
        return TimeMapper.INSTANCE;
    }
}
