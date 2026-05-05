package org.zeroagent.domain.core.aitask.model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.zeroagent.common.utils.json.JSON;

import java.time.ZonedDateTime;

/**
 * 可更新的AI任务部分
 * @author Nuk3m1
 * @version 2026年04月30日  12时29分
 */
@Data
@Accessors(chain = true)
public class UpdatableAiTask {
    /**
     * 开始运行时间
     */
    @Nullable
    private ZonedDateTime startedAt;
    /**
     * 运行完成时间
     */
    @Nullable
    private ZonedDateTime finishedAt;
    /**
     * 关联外部业务状态
     */
    private String        bizStatus;
    /**
     * 关联外部业务单号
     */
    private String        bizNo;
    /**
     * 关联外部业务子单号
     */
    private String        subBizNo;
    /**
     * 业务输入参数
     */
    private JSONObject    bizParams;
    /**
     * 业务过程数据
     */
    private JSONObject    bizExecInfo;
    /**
     * 业务输出结果
     */
    private JSONObject    bizResult;

    public UpdatableAiTask setBizParams(JSONObject bizParams) {
        this.bizParams = bizParams;
        return this;
    }
    public UpdatableAiTask setBizParams(TaskBizParams bizParams) {
        this.bizParams = JSON.toJSONObject(bizParams);
        return this;
    }
    public UpdatableAiTask setBizExecInfo(JSONObject bizExecInfo) {
        this.bizExecInfo = bizExecInfo;
        return this;
    }
    public UpdatableAiTask setBizExecInfo(TaskBizVars bizVars) {
        this.bizExecInfo = JSON.toJSONObject(bizVars);
        return this;
    }
    public UpdatableAiTask setBizVars(TaskBizVars bizVars) {
        this.bizExecInfo = JSON.toJSONObject(bizVars);
        return this;
    }
    public UpdatableAiTask setBizResult(TaskBizResult bizResult) {
        this.bizResult = JSON.toJSONObject(bizResult);
        return this;
    }

    @Nullable
    public <T extends TaskBizResult> T parseBizResult(Class<T> type) {
        if (this.bizResult == null) {
            return null;
        }
        return JSON.parseObject(this.bizResult.toString(), type);
    }
}
