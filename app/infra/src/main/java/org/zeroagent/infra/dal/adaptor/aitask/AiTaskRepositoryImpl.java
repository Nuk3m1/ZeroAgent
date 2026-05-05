package org.zeroagent.infra.dal.adaptor.aitask;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.jooq.exception.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.zeroagent.common.id.IdHelper;
import org.zeroagent.common.problem.error.CommonErrorCode;
import org.zeroagent.common.problem.exception.SysException;
import org.zeroagent.common.utils.Asserts;
import org.zeroagent.domain.core.aitask.engine.model.AiTaskExecInfo;
import org.zeroagent.domain.core.aitask.model.AiTask;
import org.zeroagent.domain.core.aitask.model.UpdatableAiTask;
import org.zeroagent.domain.core.aitask.model.enums.AiTaskExecStatus;
import org.zeroagent.domain.core.aitask.model.enums.AiTaskStatus;
import org.zeroagent.domain.core.aitask.model.enums.AiTaskType;
import org.zeroagent.domain.core.aitask.service.AiTaskRepository;
import org.zeroagent.infra.dal.common.PgDSL;
import org.zeroagent.infra.dal.tables.daos.AiTaskCreationDao;
import org.zeroagent.infra.dal.tables.pojos.AiTaskCreation;
import org.zeroagent.infra.dal.tables.records.AiTaskCreationRecord;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.zeroagent.infra.dal.Tables.AI_TASK_CREATION;

/**
 *
 * @author Nuk3m1
 * @version 2026年05月01日  14时49分
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class AiTaskRepositoryImpl implements AiTaskRepository {
    private final DSLContext            dsl;
    private final AiTaskPojoMapper      aiTaskPojoMapper;
    private final AiTaskCreationDao     aiTaskCreationDao;
    @Override
    public long create(AiTask aiTask) {
        if (aiTask.getId() == null) {
            aiTask.setId(IdHelper.getId());
        }
        AiTaskCreation aiTaskCreation = aiTaskPojoMapper.toEntity(aiTask);
        aiTaskCreationDao.insert(aiTaskCreation);
        return aiTask.getId();
    }

    @Override
    public AiTask createIdempotent(AiTask aiTask) {
        try {
            this.create(aiTask);
            return aiTask;
        } catch (DuplicateKeyException e) {
            aiTask = this.queryByUnique(aiTask.getTaskType(), aiTask.getBizType(), aiTask.getBizNo(), aiTask.getSubBizNo()).orElse(null);
            Asserts.notNull(aiTask, "will not happen");
            return aiTask;
        }
    }

    @Override
    public long countInRunning(AiTaskType aiTaskType, String bizType) {
        if (Objects.isNull(bizType)) {
            return dsl.selectCount()
                    .from(AI_TASK_CREATION)
                    .where(AI_TASK_CREATION.TASK_TYPE.eq(aiTaskType.name()))
                    .and(AI_TASK_CREATION.TASK_STATUS.in(AiTaskStatus.RUNNING.name()))
                    .fetchSingleInto(Long.class);
        }
        return dsl.selectCount()
                .from(AI_TASK_CREATION)
                .where(AI_TASK_CREATION.TASK_TYPE.eq(aiTaskType.name()))
                .and(AI_TASK_CREATION.BIZ_TYPE.eq(bizType))
                .and(AI_TASK_CREATION.TASK_STATUS.in(AiTaskStatus.RUNNING.name()))
                .fetchSingleInto(Long.class);
    }

    @Override
    public void updateExecuteStatusById(long id, AiTaskExecStatus status) {
        dsl.update(AI_TASK_CREATION)
                .set(AI_TASK_CREATION.EXEC_STATUS, status.name())
                .where(AI_TASK_CREATION.ID.eq(id))
                .execute();
    }

    @Override
    public void updateById(AiTask aiTask) {
        AiTaskCreationRecord record = aiTaskPojoMapper.toUpdatingRecord(aiTask);
        if (!record.changed()) {
            return;
        }
        dsl.update(AI_TASK_CREATION)
                .set(record)
                .where(AI_TASK_CREATION.ID.eq(aiTask.getId()))
                .execute();
    }

    @Override
    public Optional<AiTask> queryOptionalById(long id) {
        return aiTaskCreationDao.fetchOptionalById(id).map(aiTaskPojoMapper::toModel);
    }

    @Override
    public Optional<AiTask> queryOptionalByIdAndTaskId(long id, long taskId) {
        try {
            return dsl.selectFrom(AI_TASK_CREATION)
                    .where(AI_TASK_CREATION.ID.eq(taskId))
                    .and(AI_TASK_CREATION.OWNER_ID.eq(id))
                    .fetchOptionalInto(AiTaskCreation.class)
                    .map(aiTaskPojoMapper::toModel);
        } catch (DataAccessException e) {
            log.error("Query AiTask failed by taskId and Id", e);
            throw new SysException(CommonErrorCode.ILLEGAL_PARAM, e);
        }
    }

    @Override
    public void startById(long id, @Nullable UpdatableAiTask updatableAiTask, AiTaskExecInfo execInfo) {
        AiTask aiTask = aiTaskPojoMapper.toModel(updatableAiTask, execInfo, null);
        AiTaskCreationRecord record = aiTaskPojoMapper.toUpdatingRecord(aiTask);
        final ZonedDateTime startedAt;
        if (aiTask != null && aiTask.getStartAt() != null) {
            startedAt = aiTask.getStartAt();
        } else {
            startedAt = ZonedDateTime.now();
        }
        dsl.update(AI_TASK_CREATION)
                .set(AI_TASK_CREATION.TASK_STATUS, AiTaskStatus.RUNNING.name())
                .set(AI_TASK_CREATION.EXEC_STATUS, AiTaskExecStatus.WAITING.name())
                .set(AI_TASK_CREATION.START_AT, startedAt)
                .set(record)
                .where(AI_TASK_CREATION.ID.eq(id))
                .and(AI_TASK_CREATION.TASK_STATUS.eq(AiTaskStatus.CREATED.name()))
                .and(AI_TASK_CREATION.EXEC_STATUS.in(AiTaskExecStatus.WAITING.name(), AiTaskExecStatus.EXECUTING.name()))
                .execute();
    }

    @Override
    public void rollbackRunningToCreatedById(long id, @Nullable UpdatableAiTask updatableAiTask, AiTaskExecInfo execInfo) {
        AiTask aiTask = aiTaskPojoMapper.toModel(updatableAiTask, execInfo, null);
        AiTaskCreationRecord record = aiTaskPojoMapper.toUpdatingRecord(aiTask);
        dsl.update(AI_TASK_CREATION)
                .set(AI_TASK_CREATION.TASK_STATUS, AiTaskStatus.CREATED.name())
                .set(AI_TASK_CREATION.EXEC_STATUS, AiTaskExecStatus.WAITING.name())
                .setNull(AI_TASK_CREATION.START_AT)
                // 清空业务数据
                .set(AI_TASK_CREATION.BIZ_EXEC_INFO, JSONB.jsonb("{}"))
                // 清空业务结果数据
                .set(AI_TASK_CREATION.BIZ_RESULT, JSONB.jsonb("{}"))
                // 清空系统结果数据
                .set(AI_TASK_CREATION.SYS_RESULT, JSONB.jsonb("{}"))
                .set(record)
                .where(AI_TASK_CREATION.ID.eq(id))
                .and(AI_TASK_CREATION.TASK_STATUS.eq(AiTaskStatus.RUNNING.name()))
                .and(AI_TASK_CREATION.EXEC_STATUS.in(AiTaskExecStatus.WAITING.name(), AiTaskExecStatus.EXECUTING.name()))
                .execute();
    }

    @Override
    public void cancelById(long id, String reason, @Nullable UpdatableAiTask updatableAiTask, AiTaskExecInfo execInfo) {
        AiTask aiTask = aiTaskPojoMapper.toModel(updatableAiTask, execInfo, null);
        AiTaskCreationRecord record = aiTaskPojoMapper.toUpdatingRecord(aiTask);
        dsl.update(AI_TASK_CREATION)
                .set(AI_TASK_CREATION.TASK_STATUS, AiTaskStatus.CANCELED.name())
                .set(AI_TASK_CREATION.EXEC_STATUS, AiTaskExecStatus.WAITING.name())
                .set(AI_TASK_CREATION.SYS_RESULT, PgDSL.jsonbSet(AI_TASK_CREATION.SYS_RESULT, "cancelReason", reason))
                .set(record)
                .where(AI_TASK_CREATION.ID.eq(id))
                .and(AI_TASK_CREATION.TASK_STATUS.in(AiTaskStatus.CREATED.name(), AiTaskStatus.RUNNING.name()))
                .and(AI_TASK_CREATION.EXEC_STATUS.in(AiTaskExecStatus.WAITING.name(), AiTaskExecStatus.EXECUTING.name()))
                .execute();
    }

    @Override
    public void finishById(long id, ZonedDateTime createdAt, @Nullable UpdatableAiTask updatableAiTask, AiTaskExecInfo execInfo) {
        AiTask aiTask = aiTaskPojoMapper.toModel(updatableAiTask, execInfo, null);
        AiTaskCreationRecord record = aiTaskPojoMapper.toUpdatingRecord(aiTask);
        if (record.getFinishedAt() == null) {
            record.setFinishedAt(ZonedDateTime.now());
        }
        Duration finishedDuration = Duration.between(createdAt, record.getFinishedAt());
        dsl.update(AI_TASK_CREATION)
                .set(AI_TASK_CREATION.TASK_STATUS, AiTaskStatus.FINISHED.name())
                .set(AI_TASK_CREATION.EXEC_STATUS, AiTaskExecStatus.WAITING.name())
                .set(AI_TASK_CREATION.SYS_RESULT, PgDSL.jsonbSet(AI_TASK_CREATION.SYS_RESULT, "finishedDuration", finishedDuration))
                .set(record)
                .where(AI_TASK_CREATION.ID.eq(id))
                .and(AI_TASK_CREATION.TASK_STATUS.in(AiTaskStatus.CREATED.name(), AiTaskStatus.RUNNING.name(), AiTaskStatus.FINISHED.name()))
                .and(AI_TASK_CREATION.EXEC_STATUS.in(AiTaskExecStatus.WAITING.name(), AiTaskExecStatus.EXECUTING.name()))
                .execute();
    }

    @Override
    public void dryRunById(long id, @Nullable UpdatableAiTask updatableAiTask, @Nullable AiTaskExecInfo execInfo) {
        AiTask aiTask = aiTaskPojoMapper.toModel(updatableAiTask, execInfo, null);
        AiTaskCreationRecord record = aiTaskPojoMapper.toUpdatingRecord(aiTask);
        boolean updated = dsl.update(AI_TASK_CREATION)
                .set(AI_TASK_CREATION.EXEC_STATUS, AiTaskExecStatus.WAITING.name())
                .set(record)
                .where(AI_TASK_CREATION.ID.eq(id))
                .and(AI_TASK_CREATION.EXEC_STATUS.in(AiTaskExecStatus.WAITING.name(), AiTaskExecStatus.EXECUTING.name()))
                .execute() == 1;
        if (!updated) {
            log.warn("[dryRunById] Dry Run AiTask failed by taskId = {}", id);
        }
    }

    @Override
    public void completeById(long id, @Nullable UpdatableAiTask updatableAiTask, AiTaskExecInfo execInfo) {
        AiTask aiTask = aiTaskPojoMapper.toModel(updatableAiTask, execInfo, null);
        AiTaskCreationRecord record = aiTaskPojoMapper.toUpdatingRecord(aiTask);
        dsl.update(AI_TASK_CREATION)
                .set(AI_TASK_CREATION.EXEC_STATUS, AiTaskExecStatus.COMPLETED.name())
                .set(record)
                .where(AI_TASK_CREATION.ID.eq(id))
                .and(AI_TASK_CREATION.TASK_STATUS.in(AiTaskStatus.FINISHED.name(), AiTaskStatus.CANCELED.name()))
                .and(AI_TASK_CREATION.EXEC_STATUS.in(AiTaskExecStatus.WAITING.name(), AiTaskExecStatus.EXECUTING.name()))
                .execute();
    }

    @Override
    public Optional<AiTask> queryByUnique(AiTaskType taskType, String bizType, String bizNo, String subBizNo) {
        Asserts.notNull(taskType, "taskType must not be null");
        Asserts.notNull(bizType, "bizType must not be null");
        Asserts.notNull(bizNo, "bizNo must not be null");
        Asserts.notNull(subBizNo, "subBizNo must not be null");
        return dsl.selectFrom(AI_TASK_CREATION)
                .where(AI_TASK_CREATION.TASK_TYPE.eq(taskType.name()))
                .and(AI_TASK_CREATION.BIZ_TYPE.eq(bizType))
                .and(AI_TASK_CREATION.BIZ_NO.eq(bizNo))
                .and(AI_TASK_CREATION.SUB_BIZ_NO.eq(subBizNo))
                .fetchOptionalInto(AiTaskCreation.class)
                .map(aiTaskPojoMapper::toModel);
    }

    @Override
    public List<AiTask> queryWaitingForAutoExec(AiTaskType aiTaskType, String bizType, int limit) {
        Condition condition = AI_TASK_CREATION.EXEC_STATUS.eq(AiTaskExecStatus.WAITING.name())
                .and(AI_TASK_CREATION.TASK_TYPE.eq(aiTaskType.name()));
        if (Objects.nonNull(bizType)) {
            condition = condition.and(AI_TASK_CREATION.BIZ_TYPE.eq(bizType));
        }
        return dsl.selectFrom(AI_TASK_CREATION)
                .where(condition)
                .orderBy(AI_TASK_CREATION.PRIORITY.asc(), AI_TASK_CREATION.CREATED_AT.asc())
                .limit(limit)
                .fetchInto(AiTaskCreation.class)
                .stream()
                .map(aiTaskPojoMapper::toModel)
                .collect(Collectors.toList());
    }


}
