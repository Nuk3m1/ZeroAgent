package org.zeroagent.domain.core.aitask.engine.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 任务执行过程信息 - 载体类
 * @author Nuk3m1
 * @version 2026年05月01日  15时32分
 */
@Data
@Accessors(chain = true)
public class AiTaskExecInfo {
    private static final int MAX_ERROR_MSG_LENGTH = 200;
    private static final int MAX_ROLLBACK_REASON_MSG_LENGTH = 200;

    /**
     * 最后一次执行时间 (进入 AiTaskEngine 的时间)
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime   execTime;
    /**
     * 累计执行次数
     */
    private Integer         execCount;
    /**
     * 最后一次异常时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime   errorTime;
    /**
     * 最后一次异常信息
     */
    private String          errorMsg;
    /**
     * 累计异常次数
     */
    private Integer         errorCount;

    /**
     * 最后一次回滚时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime   rollbackTime;
    /**
     * 最后一次回滚原因
     */
    private String          rollbackReason;
    /**
     * 累计回滚次数
     */
    private Integer         rollbackCount;

    /**
     * 累加执行次数
     * @return this
     */
    public AiTaskExecInfo incExecCount() {
        if (execCount == null) {
            execCount = 1;
        } else {
            execCount = execCount + 1;
        }
        return this;
    }
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = StringUtils.substring(errorMsg, 0, AiTaskExecInfo.MAX_ERROR_MSG_LENGTH);
    }

    /**
     * 累加异常执行次数
     * @return this
     */
    public AiTaskExecInfo incErrorCount() {
        if (errorCount == null) {
            errorCount = 1;
        } else {
            errorCount = errorCount + 1;
        }
        return this;
    }

    /**
     * 是否超过最大回滚次数
     * @param maxRollbackCount 最大回滚次数
     * @return 是否超过
     */
    public boolean exceedMaxRollbackCount(int maxRollbackCount) {
        return rollbackCount != null && rollbackCount > maxRollbackCount;
    }

    /**
     * 设置回滚原因
     * @param rollbackReason 回滚原因
     */
    public void setRollbackReason(String rollbackReason) {
        this.rollbackReason = StringUtils.substring(rollbackReason, 0, MAX_ROLLBACK_REASON_MSG_LENGTH);
    }

    /**
     * 累加回滚次数
     * @return this
     */
    public AiTaskExecInfo incRollbackCount() {
        if (rollbackCount == null) {
            rollbackCount = 1;
        } else {
            rollbackCount = rollbackCount + 1;
        }
        return this;
    }

    public int getRollbackCount() {
        return rollbackCount == null ? 0 : rollbackCount;
    }




}
