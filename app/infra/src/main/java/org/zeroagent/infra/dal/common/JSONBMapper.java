package org.zeroagent.infra.dal.common;

import org.jooq.JSONB;
import org.json.JSONObject;
import org.mapstruct.Mapper;
import org.zeroagent.common.mapper.BaseMapperConfig;
import org.zeroagent.common.utils.json.JSON;

/**
 *
 * @author Nuk3m1
 * @version 2026年05月04日  21时03分
 */
@Mapper(config = BaseMapperConfig.class)
public interface JSONBMapper {

    default JSONB jsonObjectToJSONB(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        return JSONB.valueOf(JSON.toJSONString(jsonObject));
    }

    default JSONObject toJSONObject(JSONB jsonb) {
        if (jsonb == null) {
            return null;
        }
        return JSON.parseJSONObject(jsonb.data());
    }
}
