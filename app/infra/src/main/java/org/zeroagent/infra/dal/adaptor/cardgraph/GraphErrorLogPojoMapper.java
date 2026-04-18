package org.zeroagent.infra.dal.adaptor.cardgraph;

import org.jooq.JSONB;
import org.json.JSONObject;
import org.mapstruct.Mapper;
import org.zeroagent.common.mapper.BaseMapperConfig;
import org.zeroagent.common.utils.json.JSON;
import org.zeroagent.domain.core.grapherror.model.GraphErrorLog;
import org.zeroagent.infra.dal.common.ModelMapper;

import org.zeroagent.infra.dal.common.UpdatableBuilder;
import org.zeroagent.infra.dal.tables.pojos.GraphSyncErrorLog;
import org.zeroagent.infra.dal.tables.records.GraphSyncErrorLogRecord;

import java.util.Objects;

import static org.zeroagent.infra.dal.tables.GraphSyncErrorLog.GRAPH_SYNC_ERROR_LOG;

/**
 *
 * @author Nuk3m1
 * @version 2026年03月18日  21时05分
 */
@Mapper(config = BaseMapperConfig.class)
public interface GraphErrorLogPojoMapper extends ModelMapper<GraphErrorLog, GraphSyncErrorLog, GraphSyncErrorLogRecord> {
    @Override
    GraphSyncErrorLog toEntity(GraphErrorLog model);
    @Override
    GraphErrorLog toModel(GraphSyncErrorLog entity);

    default JSONObject JsonbToLlmRawResponse(JSONB jsonb) {
        if (jsonb == null) {
            return null;
        }
        return JSON.parseJSONObject(jsonb.data());
    }

    default JSONB llmRawResponseToJsonb(JSONObject response) {
        if (response == null) {
            return null;
        }
        return JSONB.jsonb(response.toString());
    }

    @Override
    default void updatable(UpdatableBuilder<GraphSyncErrorLogRecord> builder) {
        builder.updatable(GRAPH_SYNC_ERROR_LOG.ERROR_MESSAGE);
        builder.updatable(GRAPH_SYNC_ERROR_LOG.ERROR_TYPE);
        builder.updatable(GRAPH_SYNC_ERROR_LOG.SOURCE_CARD_ID);
        builder.updatable(GRAPH_SYNC_ERROR_LOG.SOURCE_CARD_NAME);
        builder.updatable(GRAPH_SYNC_ERROR_LOG.RESOLVED);
        builder.updatable(GRAPH_SYNC_ERROR_LOG.LLM_RAW_RESPONSE);
    }
}
