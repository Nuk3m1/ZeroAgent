package org.zeroagent.domain.core.aitask.engine.handler.forward.post;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeroagent.common.utils.Asserts;
import org.zeroagent.common.utils.json.JSON;
import org.zeroagent.domain.core.aitask.model.UpdatableAiTask;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月30日  13时09分
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TaskPostAction {
    public static final TaskPostAction KEEP_STILL = new TaskPostAction(ActionTypeEnum.KEEP_STILL, null);
    public static final TaskPostAction COMPLETE   = new TaskPostAction(ActionTypeEnum.COMPLETE, null);

    private final ActionTypeEnum type;
    @Nullable
    private final UpdatableAiTask updatableAiTask;

    public TaskPostAction withAiTask(@NotNull UpdatableAiTask updatableAiTask) {
        Asserts.notNull(updatableAiTask, "updatableAiTask must not be null");
        Asserts.isTrue(!"{}".equals(JSON.toJSONString(updatableAiTask)), "updatableAiTask must have changes");
        return new TaskPostAction(this.type, updatableAiTask);
    }

    public enum ActionTypeEnum {
        KEEP_STILL,
        COMPLETE
    }
}
