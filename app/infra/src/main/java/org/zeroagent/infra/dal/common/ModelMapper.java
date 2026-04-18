package org.zeroagent.infra.dal.common;

import org.jooq.UpdatableRecord;
import org.mapstruct.BeanMapping;
import org.mapstruct.ReportingPolicy;
import org.zeroagent.common.repository.PojoMapper;

import java.io.Serializable;

/**
 * @author Nuk3m1
 * @version 2026年03月08日  15时03分
 * @Description:
 */
public interface ModelMapper<MODEL, ENTITY extends Serializable, RECORD extends UpdatableRecord<RECORD>> extends PojoMapper<MODEL, ENTITY> {
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    RECORD entityToRecord(ENTITY entity);

    default RECORD toUpdatingRecord(MODEL model) {
        return this.toUpdatingRecord(model, false);
    }

    default RECORD toUpdatingRecord(MODEL model, boolean allowUpdateToNull) {
        UpdatableBuilder<RECORD> updatableBuilder = new UpdatableBuilder<>();
        this.updatable(updatableBuilder);
        return new UpdatingRecordBuilder<>(this)
                .toRecord(model)
                .allowUpdateToNull(allowUpdateToNull)
                .updatable(updatableBuilder)
                .build();
    }

    default void updatable(UpdatableBuilder<RECORD> builder) {
        throw new IllegalStateException("Must be overridden if updating is required");
    }


}
