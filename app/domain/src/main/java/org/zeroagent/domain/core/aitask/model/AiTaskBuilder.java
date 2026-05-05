package org.zeroagent.domain.core.aitask.model;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.zeroagent.common.enums.ICode;
import org.zeroagent.common.model.OperatorSource;
import org.zeroagent.domain.core.aitask.model.enums.AiTaskType;
import org.zeroagent.domain.core.auth.model.UserContext;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月26日  23时10分
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class AiTaskBuilder {
    private final AiTask aiTask;


    public AiTaskStep0Builder owner(UserContext userContext) {
        aiTask.setOwnerId(userContext.getUid())
                .setOwnerName(userContext.getUserName());

        return new AiTaskStep0Builder(aiTask);
    }
    public AiTaskStep0Builder owner(Long ownerId, String ownerName) {
        aiTask.setOwnerId(ownerId)
                .setOwnerName(ownerName);
        return new AiTaskStep0Builder(aiTask);
    }
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class AiTaskStep0Builder {
        private final AiTask aiTask;
        public AiTaskStep1Builder taskType(AiTaskType taskType) {
            return new AiTaskStep1Builder(aiTask.setTaskType(taskType).setTaskName(taskType.getDesc()));
        }
    }
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class AiTaskStep1Builder {
        private final AiTask aiTask;
        public AiTaskStep2Builder bizType(ICode bizType) {
            String taskName = aiTask.getTaskName() + "-" + bizType.getDesc();
            return new AiTaskStep2Builder(aiTask.setBizType(bizType.getCode()).setTaskName(taskName));
        }
    }
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class AiTaskStep2Builder {
        private final AiTask aiTask;
        public AiTaskStep3Builder bizNo(long bizNo) {
            return new AiTaskStep3Builder(aiTask.setBizNo(String.valueOf(bizNo)));
        }
        public AiTaskStep3Builder bizNo(String bizNo) {
            return new AiTaskStep3Builder(aiTask.setBizNo(bizNo));
        }
    }
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class AiTaskStep3Builder {
        private final AiTask aiTask;
        public AiTaskStep4Builder subBizNo(long subBizNo) {
            return new AiTaskStep4Builder(aiTask.setSubBizNo(String.valueOf(subBizNo)));
        }
        public AiTaskStep4Builder subBizNo(String subBizNo) {
            return new AiTaskStep4Builder(aiTask.setSubBizNo(subBizNo));
        }
    }
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class AiTaskStep4Builder {
        private final AiTask aiTask;
        public AiTaskStep5Builder bizInput(TaskBizInput bizInput) {
            return new AiTaskStep5Builder(aiTask.setBizInput(bizInput));
        }
        public AiTaskStep5Builder bizParams(TaskBizInput bizInput) {
            return new AiTaskStep5Builder(aiTask.setBizInput(bizInput));
        }
        public AiTaskStep5Builder bizParams(JSONObject bizParams) {
            return new AiTaskStep5Builder(aiTask.setBizParams(bizParams));
        }
    }
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class AiTaskStep5Builder {
        private final AiTask aiTask;
        public AiTaskStep5Builder bizStatus(@Nullable String bizStatus) {
            if (bizStatus != null) {
                aiTask.setBizStatus(bizStatus);
            }
            return this;
        }
        public AiTaskStep5Builder bizVars(@Nullable TaskBizVars bizVars) {
            if (bizVars != null) {
                aiTask.setBizExecInfo(bizVars);
            }
            return this;
        }

        public AiTask build() {
            return aiTask;
        }
    }

}
