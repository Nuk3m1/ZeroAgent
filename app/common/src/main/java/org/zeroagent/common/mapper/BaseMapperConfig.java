package org.zeroagent.common.mapper;

import org.mapstruct.*;

/**
 * @author Nuk3m1
 * @version 2026年03月08日  14时34分
 * @Description:
 */
@MapperConfig (
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = TimeMapper.class,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        builder = @Builder(disableBuilder = true)
)
public class BaseMapperConfig {
}
