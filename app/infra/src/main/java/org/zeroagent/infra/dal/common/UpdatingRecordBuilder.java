package org.zeroagent.infra.dal.common;

import lombok.RequiredArgsConstructor;
import org.jooq.Field;
import org.jooq.UpdatableRecord;
import org.zeroagent.common.utils.Asserts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 *
 * @author Nuk3m1
 * @version 2026年03月08日  16时35分
 */
@RequiredArgsConstructor
public class UpdatingRecordBuilder<MODEL, ENTITY extends Serializable, RECORD extends UpdatableRecord<RECORD>> {

    private final ModelMapper<MODEL, ENTITY, RECORD> modelMapper;
    private final List<Field<?>> updatableFields     = new ArrayList<>();
    /**
     * 可以更新为null的字段列表
     */
    private final List<Field<?>>       nullUpdatableFields = new ArrayList<>();

    private MODEL model;
    private boolean allowUpdateToNull;


    public UpdatingRecordBuilder<MODEL, ENTITY, RECORD> toRecord(MODEL model) {
        this.model = model;
        return this;
    }

    public UpdatingRecordBuilder<MODEL, ENTITY, RECORD> allowUpdateToNull(boolean allowUpdateToNull) {
        this.allowUpdateToNull = allowUpdateToNull;
        return this;
    }

    public UpdatingRecordBuilder<MODEL, ENTITY, RECORD> updatable(UpdatableBuilder<RECORD> builder) {
        this.updatableFields.addAll(builder.updatableFields);
        this.nullUpdatableFields.addAll(builder.nullUpdatableFields);
        return this;
    }

    public RECORD build() {
        Asserts.notNull(model, "model must not be null");
        ENTITY entity = modelMapper.toEntity(model);
        // 转换为record，此时所有不为null的字段都会被标记为changed
        RECORD record = modelMapper.entityToRecord(entity);
        // 主键不能更新
        Field<?>[] pk = Objects.requireNonNull(record.getTable().getPrimaryKey()).getFieldsArray();
        for (Field<?> field : pk) {
            record.changed(field, false);
        }
        // 针对不在可更新字段中的字段，重置为原始值
        record.fieldStream().forEach(field -> {
            if (!updatableFields.contains(field) && record.changed(field)) {
                record.reset(field);
            }
        });
        if (this.allowUpdateToNull) {
            // 针对可更新为null的字段，始终标记为changed
            nullUpdatableFields.forEach(field -> {
                if (!record.changed(field)) {
                    record.changed(field, true);
                }
            });
            return record;
        }
        return record;
    }
}

