package org.zeroagent.infra.dal.common;

import org.jooq.TableField;
import org.jooq.Record;
import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 * @author Nuk3m1
 * @version 2026年03月08日  16时29分
 */
public class UpdatableBuilder<RECORD extends Record> {
    /**
     *  允许更新,但不能更新为null的字段列表
     */
    final List<TableField<RECORD, ?>> updatableFields     = new ArrayList<>();
    /**
     * 可以更新为null的字段列表
     */
    final List<TableField<RECORD, ?>> nullUpdatableFields = new ArrayList<>();

    public UpdatableBuilder<RECORD> updatable(TableField<RECORD, ?> field) {
        return this.updatable(field, false);
    }

    public UpdatableBuilder<RECORD> updatable(TableField<RECORD, ?> field, boolean allowNullSet) {
        this.updatableFields.add(field);
        if (allowNullSet) {
            this.nullUpdatableFields.add(field);
        }
        return this;
    }
}
