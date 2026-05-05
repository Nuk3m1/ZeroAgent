package org.zeroagent.domain.core.aitask.engine.handler.forward;

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
 * @version 2026年04月29日  15时46分
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class TaskActionImpl implements TaskAction {
    private final ActionTypeEnum type;
    @Nullable
    private final UpdatableAiTask updatableAiTask;

    protected void checkNotNull(@NotNull UpdatableAiTask updatableAiTask) {
        Asserts.notNull(updatableAiTask, "updatableAiTask must not be null");
        Asserts.isTrue(!"{}".equals(JSON.toJSONString(updatableAiTask)), "updatableAiTask must have changes");
        Asserts.isTrue(this.updatableAiTask == null, "updatableAiTask already set");
    }

    protected void checkNullable(@Nullable UpdatableAiTask updatableAiTask) {
        if (updatableAiTask != null) {
            this.checkNotNull(updatableAiTask);
        }
    }
    @Override
    public TaskAction withAiTask(@NotNull UpdatableAiTask updatableAiTask) {
        this.checkNotNull(updatableAiTask);
        return new TaskActionImpl(this.type, updatableAiTask);
    }

}
