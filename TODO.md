# ZeroAgent 迭代方向（DDD 规划）

## 0. 范围与约束
1. 当前讨论优先聚焦迭代一，但文档保留迭代二。
2. 迭代一不改 `bootstrap/api/biz`，仅涉及 `domain + infra`。
3. `/api/user/ai-chat/extract` 是测试接口，后续链路完善后删除，不纳入正式方案。
4. 职责边界：`domain` 负责调度语义与数据库状态轮转；`infra` 负责 Agent 对话与外部实现。

## 1. 迭代一：审批 Agent 闭环（抽取 -> 审批 -> 建图）

### 1.1 目标描述（对齐版）
1. 只对已完成结构化关系的卡牌执行语义抽取，即仅打捞 `CardInformationStatusEnum.SUCCESS`。
2. 语义抽取结果先落 `GRAPH_SYNC_ERROR_LOG` 审批工单，不直接写 Neo4j 语义关系。
3. 通过第二个调度器执行审批 Agent，审批通过后再建图。
4. 当前只打通 `SEARCH` 关系全链路。

### 1.2 已确认口径（最终）
1. `CardInformation` 状态：
   1. 抢占任务后先改 `EXECUTING`（事务内 + `for update skip locked`）。
   2. 抽取完成后异步回写 `COMPLETED`（终态）。
   3. 抽取异常回写 `FAILURE`。
2. `GraphErrorLog` 状态：
   1. 抽取落库为 `CREATED`。
   2. 审批调度抢占后改 `WAITING`。
   3. 审批通过且建图成功改 `SUCCESS`。
   4. 审批拒绝或建图失败统一改 `FAILED`（暂不细分颗粒度）。
3. 关系范围：
   1. 当前严格只做 `SEARCH`。
   2. 必须预留扩展位，后续可无痛挂载 `AS_MATERIAL_FOR` 等新关系类型。

### 1.3 第一阶段：语义抽取调度（domain 调度 + infra 抽取）
1. `domain` 新增调度器（参考 `CardTaskScheduler`）：
   1. 周期捞取 `CardInformation.SUCCESS`。
   2. 事务内使用 `for update skip locked` 抢占并置 `EXECUTING`。
   3. 异步提交抽取任务。
2. `domain` 新增执行方法（引擎/服务）：
   1. 只负责数据库相关处理和流程控制。
   2. 调用 `ExtractRelationshipService`（domain 接口，infra 实现）执行抽取。
   3. 根据结果回写 `COMPLETED/FAILURE`。
3. `infra` 继续实现抽取：
   1. `ExtractRelationshipServiceImpl` 负责 Agent 对话与工具调用。
   2. 结果写入 `GRAPH_SYNC_ERROR_LOG(status=CREATED)`。
   3. 不直接写 Neo4j 语义边。

### 1.4 第二阶段：审批调度（domain 调度 + infra 审批/建图）
1. `domain` 新增审批调度器：
   1. 周期捞取 `GraphErrorLog.CREATED`。
   2. 事务内 `for update skip locked` 抢占并置 `WAITING`。
   3. 异步提交审批任务。
2. `domain` 新增审批执行方法（引擎/服务）：
   1. 驱动审批流程与状态轮转。
   2. 仅审批通过才允许建图。
   3. 回写 `SUCCESS/FAILED`。
3. `infra` 审批与建图实现：
   1. 审批 Agent 输出结构化结论。
   2. 当前仅调用 `CardGraphRepository.drawSearchArrow` 建立 `SEARCH` 关系。
   3. 建图失败回写 `FAILED`，记录失败原因。

### 1.5 扩展性设计要求（本轮必须考虑）
1. 工单与执行流程需携带“关系类型”语义，不要把流程写死在 `SEARCH` 分支上。
2. 调度器、审批引擎、建图执行三段流程都要可插拔地扩展新关系类型。
3. 新关系（如 `AS_MATERIAL_FOR`）接入时应仅新增抽取器/审批规则/建图映射，不重写主流程。

### 1.6 验收标准（迭代一）
1. 多机并发下不重复消费（`CardInformation` 与 `GraphErrorLog` 都成立）。
2. 全链路可追溯：源卡牌 -> 抽取工单 -> 审批结果 -> 图谱关系。
3. 当前链路仅 `SEARCH`，但新增关系类型无需推翻现有调度主干。

## 2. 迭代二：游戏王 OCG 残局生图（保留）

### 2.1 目标描述
1. 用户上传对局截图后，系统识别场面元素（我方/对方场地、手牌、墓地、关键卡）。
2. Agent 基于识别结果与图谱知识，给出可执行的展开建议。
3. 输出带战术箭头与步骤标注的指导图，并附文字说明。

### 2.2 当前处理策略
1. 保留在路线图中，后续单独细化实现细则。
2. 当前研发优先级先完成迭代一闭环。
