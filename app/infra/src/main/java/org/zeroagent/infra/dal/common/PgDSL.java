package org.zeroagent.infra.dal.common;

import lombok.experimental.UtilityClass;
import org.jooq.Field;
import org.jooq.JSONB;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.zeroagent.common.utils.FormatUtil;
import org.zeroagent.common.utils.json.JSON;

/**
 * PG数据库 - 操作工具类
 * @author Nuk3m1
 * @version 2026年05月02日  22时32分
 */
@UtilityClass
public class PgDSL {
    /**
     * 更新 JSONB 字段中的指定属性
     * @param field JSONB字段
     * @param path 属性路径
     * @param value 属性值
     * @return 更新后的JSONB字段
     */
    public Field<JSONB> jsonbSet(TableField<?, JSONB> field, String path, Object value) {
        return jsonbFunction("jsonb_set", field, path, value);
    }

    public Field<JSONB> jsonbInsert(TableField<?, JSONB> field, String path, Object value) {
        return jsonbFunction("jsonb_insert", field, path, value);
    }

    public static Field<JSONB> jsonbArrayElements(TableField<?, JSONB> field) {
        return DSL.field("jsonb_array_elements(?::jsonb)", JSONB.class, field);
    }

    private Field<JSONB> jsonbFunction(String function, TableField<?, JSONB> field, String path, Object value) {
        final String sql = FormatUtil.format("{}({}, ?::text[], ?::jsonb)", function, field.getName());
        final String queryPath = "{" + path + "}";
        final String jsonValue;
        if (value instanceof JSONB jsonb) {
            jsonValue = jsonb.data();
        } else {
            jsonValue = JSON.toJSONString(value);
        }
        return DSL.field(sql, JSONB.class, queryPath, jsonValue);
    }
}
