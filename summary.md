# ZeroAgent `app` 模块功能总结与功能 Review（更新版）

## 变更记录
- 2026-04-23（v1.1）：
  - 按 owner 最终口径统一模块定义与架构基线（业务五模块 + `common` 通用模块定位）。
  - 明确区分“已修复项”与“预案/暂不实现项”，避免将阶段性预设误判为缺陷。
  - 补充对高优先级 4.1.1/4.1.2/4.1.3 修复状态与代码证据说明。

## 0. 本次修订说明
- 本文已按项目 owner 最新说明修订：  
  - “五个业务模块”指 `bootstrap/api/biz/domain/infra`。  
  - `common` 为通用工具模块，刻意与业务语义解耦，目标是可复用到其他项目。  
  - 架构底线明确为：`api -> biz -> domain (-> common)` 与 `api -> infra -> domain`；`domain` 只定义语义与协议，`infra` 负责实现。
- 代码层面实际 Maven 模块仍为 6 个：`api/biz/domain/infra/common/bootstrap`（`pom.xml:12-19`）。

## 1. 架构与模块定位（最终口径）

### 1.1 业务五层
1. `bootstrap`：启动装配层，负责 Spring Boot 启动与模块聚合（`app/bootstrap/pom.xml:17-29`）。
2. `api`：接口接入层，承接 HTTP/SSE 请求并做入参转发（`app/api/src/main/java/org/zeroagent/api/core/**`）。
3. `biz`：业务编排层，组织领域服务调用，承接流程控制（`app/biz/src/main/java/org/zeroagent/biz/**`）。
4. `domain`：核心语义层，定义实体、领域服务接口、仓储接口与业务协议（`app/domain/src/main/java/org/zeroagent/domain/**`）。
5. `infra`：基础设施实现层，落地 domain 定义的能力（DB/LLM/TOS/Neo4j/Feishu 等，`app/infra/src/main/java/org/zeroagent/infra/**`）。

### 1.2 `common` 模块定位
- `common` 不承载业务语义，作为可迁移的通用能力库存在：分页、异常、并发工具、JSON、ID 等（`app/common/src/main/java/org/zeroagent/common/**`）。

### 1.3 依赖事实（pom 维度）
- `api -> biz + common`（`app/api/pom.xml:17-25`）
- `biz -> domain + common`（`app/biz/pom.xml:17-25`）
- `infra -> domain + common`（`app/infra/pom.xml:17-25`）
- `domain -> common`（`app/domain/pom.xml:17-21`）
- `bootstrap -> api + infra + common`（`app/bootstrap/pom.xml:17-29`）

## 2. 核心功能链路（按当前实现）

### 2.1 Web 聊天链路
1. `AiChatResource.chat` 接收请求，返回 `Flux<ApiResult<MessageChunk>>`（`app/api/src/main/java/org/zeroagent/api/core/chat/AiChatResource.java:30-34`）。
2. `AiChatManager.chat` 处理会话 ID、构建 `UserMessage`（`app/biz/src/main/java/org/zeroagent/biz/chat/AiChatManager.java:34-49`）。
3. `AiChatServiceImpl.DouBaoChatStream` 处理会话落库、历史载入、LLM 流式调用、工具调用递归与回写（`app/infra/src/main/java/org/zeroagent/infra/core/ai/chat/AiChatServiceImpl.java:61-245`）。

### 2.2 卡牌拉取与图谱构建
1. `POST /api/card/fetch-all` 触发拉取（`app/api/src/main/java/org/zeroagent/api/core/card/CardInformationResource.java:25-28`）。
2. `BaiGeClientImpl.getCards` 拉取并解析 `cards.zip`，批量写入 PG（`app/infra/src/main/java/org/zeroagent/infra/core/card/service/BaiGeClientImpl.java:47-74`）。
3. 调度任务轮询 PENDING 卡牌并异步建图（`app/domain/src/main/java/org/zeroagent/domain/core/task/CardTaskScheduler.java:43-56`）。

### 2.3 关系抽取工具链路（测试入口）
1. `POST /api/user/ai-chat/extract` 调用测试抽取（`app/api/src/main/java/org/zeroagent/api/core/chat/AiChatResource.java:36-39`）。
2. 工具定义与执行在 `ExtractSearchRulesTool`，按结构化条件检索卡牌（`app/infra/src/main/java/org/zeroagent/infra/core/ai/toolcalling/ExtractSearchRulesTool.java:38-159`）。

## 3. Review 结论（结合代码与 owner 说明）

### 3.1 已修复项（确认通过）
1. 高优先级 4.1.1：首轮会话 `conversationId` 丢失问题已修复。  
   证据：首轮生成 ID 后回写 `conversation.setId(conversationId)`（`AiChatServiceImpl.java:74-76`）。
2. 高优先级 4.1.2：继续对话时用户消息重复注入问题已修复。  
   证据：先加载历史，再持久化本轮用户消息，避免重复进入历史集（`AiChatServiceImpl.java:99-111`）。
3. 高优先级 4.1.3：数值条件过滤逻辑失效已修复。  
   证据：`appendNumericCondition` 改为 `!StringUtils.hasText(operator)` 才跳过（`CardInformationRepositoryImpl.java:186-204`）。
4. 中优先级：线程池拒绝策略 fallback 后仍抛异常问题已修复。  
   证据：保留 `handler.rejectedExecution(...)`，不再追加固定抛错（`ThreadPools.java:28-32`）。
5. 中优先级：分页 `needContent` 配置笔误已修复。  
   证据：`needContent` 兜底改为读取 `needContent()`（`PageRequest.java:85-87`）。
6. 低优先级：种族文案不一致已修复。  
   证据：`ExtractSearchRulesTool` 枚举改为 `魔法师族`，与位掩码解析一致（`ExtractSearchRulesTool.java:94` 与 `CardTypeBitMaskUtil.java:24`）。
7. 高优先级“明文敏感信息入库”在当前仓库判定为误报。  
   证据：`.gitignore` 已忽略 `**/src/main/resources/application-local.yml`（`.gitignore:36`），且当前 Git 跟踪记录中无该文件。

### 3.2 预案/暂不实现项（按当前阶段接受）
- 以下条目按 owner 说明属于“计划与预案”，当前阶段不要求落地完整逻辑：
1. 鉴权放行策略（`SecurityConfig` 目前 `requestMatchers("/api/**").permitAll()`，`app/api/src/main/java/org/zeroagent/api/config/security/SecurityConfig.java:24-29`）。
2. `UserContextUtils` 暂未完成（`app/api/src/main/java/org/zeroagent/api/config/security/UserContextUtils.java:17-20`）。
3. `AppAlertHelperImpl.alertText` 暂为空实现（`app/infra/src/main/java/org/zeroagent/infra/notification/alert/AppAlertHelperImpl.java:21-24`）。
4. `TosFileManager.upload` 固定 key（`app/biz/src/main/java/org/zeroagent/biz/file/TosFileManager.java:28-33`）。
5. `TosTemplateImpl.getHttpUrl(..., fileProcessOption)` 尚未将 `fileProcessOption` 应用于最终 URL（`app/infra/src/main/java/org/zeroagent/infra/integration/tos/TosTemplateImpl.java:141-167`）。
6. `BaiGeClientImpl.getCardInformationByCardId` 暂未实现（`app/infra/src/main/java/org/zeroagent/infra/core/card/service/BaiGeClientImpl.java:79-82`）。
7. Controller 层未启用 `@Valid` 触发 `AiChatRequestVO` 校验（`AiChatResource.java:31`，`AiChatRequestVO.java:18-19`）。
8. `/api/user/ai-chat/extract` 作为测试入口对外开放（`AiChatResource.java:36-39`）。
9. `app/**/src/test` 当前为空，测试体系暂未补齐。

## 4. 最终结论
- 当前版本已经完成“必须修复”的高优先级核心逻辑问题（4.1.1/4.1.2/4.1.3）与部分中低优先级修复。
- 未修复项并非遗漏，而是按项目阶段主动保留的预案能力。
- 架构基线已明确：`domain` 负责语义，`infra` 负责实现；`common` 保持业务无关与可迁移性。
