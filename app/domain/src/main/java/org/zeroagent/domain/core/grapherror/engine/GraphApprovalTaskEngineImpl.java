package org.zeroagent.domain.core.grapherror.engine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.zeroagent.domain.core.aitask.model.AiTask;
import org.zeroagent.domain.core.cardgraph.model.GraphRelationTypeEnum;
import org.zeroagent.domain.core.cardgraph.service.CardGraphRepository;
import org.zeroagent.domain.core.grapherror.model.GraphErrorLog;
import org.zeroagent.domain.core.grapherror.model.GraphErrorLogStatus;
import org.zeroagent.domain.core.grapherror.service.GraphErrorLogRepository;

import java.util.List;

/**
 * 图谱审批任务执行引擎实现
 * @author Nuk3m1
 * @version 2026年04月23日  15时27分
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GraphApprovalTaskEngineImpl implements GraphApprovalTaskEngine {
    private final GraphErrorLogRepository graphErrorLogRepository;
    private final CardGraphRepository    cardGraphRepository;

    @Override
    public void execute(@NotNull List<GraphErrorLog> graphErrorLogs) {
        for (GraphErrorLog graphErrorLog : graphErrorLogs) {
            try {
                // 阶段策略调整：关闭审核 Agent 介入，改为字段校验后直接建边。
                // GraphApprovalDecision decision = graphApprovalService.approve(graphErrorLog);
                // if (decision == null || !decision.isApproved()) {
                //     String reason = decision == null ? "审批结果为空" : decision.getReason();
                //     markFailed(graphErrorLog, reason);
                //     continue;
                // }

                String validationError = validateForDirectBuild(graphErrorLog);
                if (StringUtils.hasText(validationError)) {
                    markFailed(graphErrorLog, validationError);
                    continue;
                }


                GraphRelationTypeEnum relationType = parseRelationType(graphErrorLog.getGraphRelationType());
                String sourceCardPassCode = String.valueOf(graphErrorLog.getSourceCardId());
                String targetCardPassCode = String.valueOf(graphErrorLog.getTargetCardId());
                drawRelation(relationType, sourceCardPassCode, targetCardPassCode);
                markSuccess(graphErrorLog, "DIRECT_BUILD_NO_AGENT");
                log.info("语义关系建立完成 {} -> {}", sourceCardPassCode, targetCardPassCode);
            } catch (Exception e) {
                log.error("审批执行失败，logId={}", graphErrorLog.getId(), e);
                markFailed(graphErrorLog, e.getMessage());
            }
        }
    }

    private String validateForDirectBuild(GraphErrorLog graphErrorLog) {
        GraphRelationTypeEnum relationType = parseRelationType(graphErrorLog.getGraphRelationType());
        if (relationType == null) {
            return "无效的关系类型: " + graphErrorLog.getGraphRelationType();
        }
        if (graphErrorLog.getSourceCardId() == null || graphErrorLog.getTargetCardId() == null) {
            return "卡密缺失，无法建边";
        }
        if (graphErrorLog.getSourceCardId().equals(graphErrorLog.getTargetCardId())) {
            return "源卡与目标卡相同，拒绝建边";
        }
        return null;
    }

    private GraphRelationTypeEnum parseRelationType(String rawRelationType) {
        if (!StringUtils.hasText(rawRelationType)) {
            return null;
        }
        try {
            return GraphRelationTypeEnum.valueOf(rawRelationType.trim());
        } catch (Exception ignore) {
            return null;
        }
    }

    private void drawRelation(GraphRelationTypeEnum relationType, String sourceCardPassCode, String targetCardPassCode) {
        switch (relationType) {
            case SEARCH -> cardGraphRepository.drawSearchArrow(sourceCardPassCode, targetCardPassCode);
        }
    }

    private void markSuccess(GraphErrorLog graphErrorLog, String reason) {
        graphErrorLogRepository.update(new GraphErrorLog()
                .setId(graphErrorLog.getId())
                .setStatus(GraphErrorLogStatus.SUCCESS)
                .setErrorMessage(StringUtils.hasText(reason) ? reason : "审批成功")
                .setErrorType("SUCCESS"));
    }

    private void markFailed(GraphErrorLog graphErrorLog, String reason) {
        graphErrorLogRepository.update(new GraphErrorLog()
                .setId(graphErrorLog.getId())
                .setStatus(GraphErrorLogStatus.FAILED)
                .setErrorMessage(StringUtils.hasText(reason) ? reason : "审批失败")
                .setErrorType("FAILED"));
    }
}
