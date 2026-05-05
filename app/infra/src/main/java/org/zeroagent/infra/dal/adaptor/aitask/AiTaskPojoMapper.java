package org.zeroagent.infra.dal.adaptor.aitask;

import org.jetbrains.annotations.Nullable;
import org.jooq.JSONB;
import org.json.JSONObject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.zeroagent.common.mapper.BaseMapperConfig;
import org.zeroagent.common.utils.json.JSON;
import org.zeroagent.domain.core.aitask.engine.model.AiTaskExecInfo;
import org.zeroagent.domain.core.aitask.engine.model.AiTaskParams;
import org.zeroagent.domain.core.aitask.engine.model.AiTaskResult;
import org.zeroagent.domain.core.aitask.model.AiTask;
import org.zeroagent.domain.core.aitask.model.UpdatableAiTask;
import org.zeroagent.infra.dal.common.JSONBMapper;
import org.zeroagent.infra.dal.common.ModelMapper;
import org.zeroagent.infra.dal.common.UpdatableBuilder;
import org.zeroagent.infra.dal.tables.pojos.AiTaskCreation;
import org.zeroagent.infra.dal.tables.records.AiTaskCreationRecord;

import static org.zeroagent.infra.dal.Tables.AI_TASK_CREATION;


/**
 *
 * @author Nuk3m1
 * @version 2026年05月01日  14时50分
 */
@Mapper(config = BaseMapperConfig.class, uses = JSONBMapper.class)
public interface AiTaskPojoMapper extends ModelMapper<AiTask, AiTaskCreation, AiTaskCreationRecord> {
    @Override
    AiTaskCreation toEntity(AiTask aiTask);
    @Mapping(target = "bizInput", ignore = true)
    @Override
    AiTask toModel(AiTaskCreation aiTaskCreation);

    default JSONB sysParamsToJsonb(AiTaskParams sysParams) {
        if (sysParams == null) {
            return null;
        }
        return JSONB.valueOf(JSON.toJSONString(sysParams));
    }
    default AiTaskParams jsonbToSysParams(JSONB sysParams) {
        if (sysParams == null) {
            return null;
        }
        return JSON.parseObject(sysParams.data(), AiTaskParams.class);
    }

    default JSONB execInfoToJsonb(AiTaskExecInfo aiTaskExecInfo) {
        if (aiTaskExecInfo == null) {
            return null;
        }
        return JSONB.valueOf(JSON.toJSONString(aiTaskExecInfo));
    }
    default AiTaskExecInfo jsonbToAiTaskExecInfo(JSONB aiTaskExecInfo) {
        if (aiTaskExecInfo == null) {
            return null;
        }
        return JSON.parseObject(aiTaskExecInfo.data(),  AiTaskExecInfo.class);
    }

    default JSONB sysResultToJsonb(AiTaskResult aiTaskResult) {
        if (aiTaskResult == null) {
            return null;
        }
        return JSONB.valueOf(JSON.toJSONString(aiTaskResult));
    }
    default AiTaskResult jsonbToAiTaskResult(JSONB sysResult) {
        if (sysResult == null) {
            return null;
        }
        return JSON.parseObject(sysResult.data(), AiTaskResult.class);
    }

    @Mapping(target = "bizInput", ignore = true)
    @Mapping(target = "bizType", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "priority", ignore = true)
    @Mapping(target = "execStatus", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "ownerName", ignore = true)
    @Mapping(target = "sharding", ignore = true)
    @Mapping(target = "startAt", ignore = true)
    @Mapping(target = "sysParams", ignore = true)
    @Mapping(target = "taskName", ignore = true)
    @Mapping(target = "taskType", ignore = true)
    @Mapping(target = "taskStatus", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    AiTask toModel(@Nullable UpdatableAiTask updatableAiTask, @Nullable AiTaskExecInfo execInfo, @Nullable AiTaskResult sysResult);



    @Override
    default void updatable(UpdatableBuilder<AiTaskCreationRecord> builder) {
        builder.updatable(AI_TASK_CREATION.UPDATED_AT);
        builder.updatable(AI_TASK_CREATION.BIZ_PARAMS);
        builder.updatable(AI_TASK_CREATION.BIZ_EXEC_INFO);
        builder.updatable(AI_TASK_CREATION.BIZ_RESULT);
        builder.updatable(AI_TASK_CREATION.BIZ_STATUS);
        builder.updatable(AI_TASK_CREATION.TASK_STATUS);
        builder.updatable(AI_TASK_CREATION.EXEC_STATUS);
        builder.updatable(AI_TASK_CREATION.PRIORITY);
        builder.updatable(AI_TASK_CREATION.BIZ_NO);
        builder.updatable(AI_TASK_CREATION.SUB_BIZ_NO);
        builder.updatable(AI_TASK_CREATION.START_AT);
        builder.updatable(AI_TASK_CREATION.FINISHED_AT);
        builder.updatable(AI_TASK_CREATION.SYS_PARAMS);
        builder.updatable(AI_TASK_CREATION.SYS_RESULT);
        builder.updatable(AI_TASK_CREATION.EXEC_INFO);
    }


}
