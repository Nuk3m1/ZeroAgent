package org.zeroagent.domain.core.aitask.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jodd.util.StringPool;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.springframework.data.annotation.Id;
import org.zeroagent.common.id.IdHelper;
import org.zeroagent.common.utils.json.JSON;
import org.zeroagent.domain.core.aitask.engine.model.AiTaskExecInfo;
import org.zeroagent.domain.core.aitask.engine.model.AiTaskParams;
import org.zeroagent.domain.core.aitask.engine.model.AiTaskResult;
import org.zeroagent.domain.core.aitask.model.enums.AiTaskExecStatus;
import org.zeroagent.domain.core.aitask.model.enums.AiTaskStatus;
import org.zeroagent.domain.core.aitask.model.enums.AiTaskType;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * AI生成型任务
 * @author Nuk3m1
 * @version 2026年04月26日  22时34分
 */
@Data
@Accessors(chain = true)
public class AiTask {
    /**
     * 任务默认优先级
     */
    public final static int DEFAULT_PRIORITY = 100;
    /**
     * 任务默认分片号 TODO 用于后续分配哈希槽
     */
    public final static int DEFAULT_SHARDING = 0;

    /**
     * 主键 - 唯一标识
     */
    @Id
    private Long id;
    /**
     * 任务发起人ID
     */
    private Long ownerId;
    /**
     * 任务发起者名称
     */
    private String ownerName;
    /**
     * 创建时间
     */
    private ZonedDateTime createdAt;
    /**
     * 更新时间
     */
    private ZonedDateTime updatedAt;
    /**
     * 任务名称
     */
    private String taskName;
    /**
     *  任务类型 (领域内部统一维护)
     */
    private AiTaskType taskType;
    /**
     * 关联外部业务状态 (没有，则存空字符串)
     */
    private String bizStatus;
    /**
     *  外部业务类型 (外部关联业务类型，infra层自行实现) (没有，则存空字符串)
     */
    private String bizType;
    /**
     * 外部关联 - 业务单号 (没有，则存空字符串)
     */
    private String bizNo;
    /**
     * 关联外部业务子单号 (没有，则存空字符串)
     */
    private String subBizNo;
    /**
     *  任务状态
     */
    private AiTaskStatus taskStatus;
    /**
     * 任务执行状态
     */
    private AiTaskExecStatus  execStatus;
    /**
     * 任务优先级 (默认100)
     */
    private Integer priority;
    /**
     * 任务分片号 (用于一致性哈希方案)
     */
    private Integer sharding;
    /**
     * 开始运行时间
     */
    @Nullable
    private ZonedDateTime startAt;
    /**
     * 运行完成时间
     */
    @Nullable
    private ZonedDateTime finishedAt;
    /**
     * 业务输入参数
     */
    private JSONObject bizParams;
    /**
     * 业务过程数据
     */
    private JSONObject bizExecInfo;
    /**
     * 业务输出结果
     */
    private JSONObject bizResult;
    /**
     * 系统执行参数
     */
    private AiTaskParams sysParams;
    /**
     * 系统执行过程中，任务参数
     */
    private AiTaskExecInfo execInfo;
    /**
     * 系统执行结果
     */
    private AiTaskResult   sysResult;

    /**
     * 是否属于某个用户
     * @param uid 用户ID
     * @return 是否属于
     */
    public boolean belongsTo(long uid) {
        return Objects.equals(this.getOwnerId(), uid);
    }

    @JsonIgnore
    public boolean isStarted() {
        return this.getTaskStatus() == AiTaskStatus.RUNNING;
    }

    @JsonIgnore
    public boolean isFinished() {
        return this.getTaskStatus() == AiTaskStatus.FINISHED;
    }

    @JsonIgnore
    public boolean isCanceled() {
        return this.getTaskStatus() == AiTaskStatus.CANCELED;
    }

    @JsonIgnore
    public boolean isCompleted() {
        return this.getExecStatus() == AiTaskExecStatus.COMPLETED;
    }

    @JsonIgnore
    public boolean isTimeoutFromCreated(Duration expectTime) {
        if (this.startAt == null) {
            return false;
        }
        return Duration.between(this.startAt, ZonedDateTime.now()).compareTo(expectTime) > 0;
    }

    public Duration durationFromStarted() {
        if (this.startAt == null) {
            return Duration.ZERO;
        }
        return Duration.between(this.startAt, ZonedDateTime.now());
    }

    /*-------------------------  业务输入参数   -------------------------------------*/
    public AiTask setBizParams(JSONObject bizParams) {
        this.bizParams = bizParams;
        return this;
    }
    public AiTask setBizInput(TaskBizInput bizInput) {
        this.bizParams = JSON.toJSONObject(bizInput);
        return this;
    }

    public <T extends TaskBizParams> T parseBizParams(Class<T> type) {
        return JSON.parseObject(bizParams.toString(), type);
    }
    public <T extends TaskBizInput> T parseBizInput(Class<T> type) {
        return JSON.parseObject(bizParams.toString(), type);
    }
    /*-------------------------  业务过程数据   -------------------------------------*/
    public AiTask setBizExecInfo(JSONObject bizExecInfo) {
        this.bizExecInfo = bizExecInfo;
        return this;
    }
    public AiTask setBizExecInfo(TaskBizVars bizVars) {
        this.bizExecInfo = JSON.toJSONObject(bizVars);
        return this;
    }
    public <T extends TaskBizVars> T parseBizExecInfo(Class<T> type) {
        return this.parseBizVars(type);
    }
    public <T extends TaskBizVars> T parseBizVars(Class<T> type) {
        return JSON.parseObject(bizExecInfo.toString(), type);
    }

    /*-------------------------  业务输出结果   -------------------------------------*/
    public AiTask setBizResult(JSONObject bizResult) {
        this.bizResult = bizResult;
        return this;
    }
    public AiTask setBizResult(TaskBizResult bizResult) {
        this.bizResult = JSON.toJSONObject(bizResult);
        return this;
    }

    public <T extends TaskBizResult> T parseBizResult(Class<T> type) {
        return JSON.parseObject(bizResult.toString(), type);
    }


    /**
     * 初始化新任务
     */
    public static AiTaskBuilder buildNew() {
        AiTask aiTask = new AiTask()
                .setId(IdHelper.getId())
                .setTaskStatus(AiTaskStatus.CREATED)
                .setBizStatus(StringPool.EMPTY)
                .setExecStatus(AiTaskExecStatus.WAITING)
                .setPriority(AiTask.DEFAULT_PRIORITY)
                .setSharding(AiTask.DEFAULT_SHARDING)
                .setBizResult(new JSONObject())
                .setBizExecInfo(new JSONObject())
                .setSysParams(new AiTaskParams())
                .setSysResult(new AiTaskResult())
                .setExecInfo(new AiTaskExecInfo().setExecCount(0));
        return new AiTaskBuilder(aiTask);
    }

}
