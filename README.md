# 🤖 ZeroAgent：企业级游戏王 OCG 智能决策引擎

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)
![WebFlux](https://img.shields.io/badge/Spring%20WebFlux-Reactive-blue.svg)
![Feishu Open API](https://img.shields.io/badge/Feishu-CardKit%202.0-blue.svg)
![Neo4j](https://img.shields.io/badge/Neo4j-Knowledge%20Graph-048ebd.svg)
![Doubao LLM](https://img.shields.io/badge/LLM-Doubao%20Pro-purple.svg)

**ZeroAgent** 是一款基于 Spring WebFlux 响应式架构驱动，深度整合火山引擎（豆包大模型）与飞书开放平台的垂直领域（游戏王 OCG）AI 智能体助手。

它不仅是一个聊天机器人，更是一个具备**深度思考（CoT**)、**工具调用（Tool Calling**)与 **知识图谱（GraphRAG)**)检索能力的企业级专家系统。

## 🌍 现实背景与业务空缺（Why ZeroAgent?）

在游戏王 OCG 庞大且复杂的卡牌生态中，玩家面临着极其严重的信息碎片化问题：
1. **查卡痛点**：全网有上万张卡牌，传统卡查软件只能进行机械的关键词匹配，无法理解“十星同调的深渊神兽”或"同调的斩机终端"这种人类自然语言描述，以及搜索"烙印"却没有"冰剑龙"的情况。
2. **规则与判例黑盒**：复杂的 K 社裁定（如连锁结算、取对象判定）往往散落在各大论坛，新手难以快速获取准确的判例指导。
3. **大模型在OCG卡牌领域的严重幻觉**：直接询问大模型关于游戏王的冷门卡牌或具体 combo，极易遭遇**严重**的“知识幻觉”（胡编乱造卡牌效果）。
4. **复杂的动点认知和阻抗博弈** : 牌手缺少对部分卡组的了解，从而无法知道对方卡组的动点，系统的调度点运转点，导致无效阻抗或不知道如何阻抗。

**ZeroAgent 的诞生正是为了填补这一业务空缺。** 我们通过引入 Agent 架构，将大语言模型的“逻辑推理能力”，耦合PG关系性数据库的绝对事实与 Neo4j 图数据库的卡牌关系网络相结合，打造了一个**既懂人话、又绝不胡编乱造**的资深卡牌架构师。

## ✨ 核心架构与技术亮点

### 1. DDD领域驱动设计与高度解耦的模块化设计
* **代码规范和设计隔离** ： 系统的核心业务逻辑（AI 聊天流转、工具链编排、意图决策）被极其严密地保护在 Domain 层，没有任何对外部框架的依赖。无论是飞书的 Webhook、豆包大模型的底层 HTTP 调用，还是 Neo4j 的 Cypher 语法，统统被放逐在 Infra（基础设施层）。领域层不关心底层的实现和拓展，拓展类之间也互不影响，完全隔离。

* **防腐层设计**： 外部系统的脏数据与复杂协议绝对无法污染核心业务。例如，飞书极其复杂的 Card 2.0 JSON，或是大模型的原始 SSE 报文，在跨越边界进入核心链路前，均被统一拦截并转化为纯净的 UserMessage、Conversation 与 MessageChunk 等领域实体。

* **高内聚的充血模型**： 彻底告别传统 Spring MVC 的“贫血模型”。例如，我们在处理并发背压时定义的 ChatState 实体，不仅封装了思考链和内容的拼接状态，更在内部高度自治了 Sequence 序列号的自增控制与格式清理逻辑，将数据的变化规则牢牢封印在实体内部。

* **极低重构成本和拓展难度**： 得益于高度解耦的依赖倒置（DIP）原则，ZeroAgent 拥有高度的可扩展性。如果明天需要将交互终端从“飞书”无缝迁移到“企业微信”或“钉钉”，亦或是将大模型从“豆包”切换为“DeepSeek / ChatGpt”，核心的领域层代码将保持零修改，开发者只需在拓展层infra中构造对领域层Domain的新实现即可。

### 2. 极致流畅的原生流式渲染（Reactive Typewriter）
完全摒弃了传统的阻塞式 HTTP 轮询。底层采用 **Spring WebFlux + Reactor** 架构，对接大模型 SSE 流。
* **底层响应式网络模型开发** : 脱离 **Spring Ai** , **LangChain4J** 等Agent开发框架，在底层的WebFlux上实现流式对话和工具调用，高自由度，高拓展性。
* **双轨输出机制**：完美适配大模型推理模型，在飞书卡片上实现了“大脑思考链（Reasoning）”与“最终总结（Content）”的视觉隔离与双轨流式输出。
* **背压控制和兜底机制**：针对高并发下的大模型吐字洪流，独创性地引入了 `sample` 与 `onBackpressureLatest` 响应式算子组合，配合精准的 `sequence` 序列号控制，消灭了并发更新以及数据流消费过慢导致的飞书 API 报错，支持长文的丝滑渲染。


### 3. 精准的意图识别与防幻觉护城河
* **严苛的 Tool Calling 规范**：通过高强度的 System Prompt 约束与字段级 Description 设计，彻底阻断了大模型“过早调用工具”和“参数幻觉”的业界通病。
* **多模态技能网（Skills）**：Agent 具备自主判断能力，能将模糊的描述性问题转化为图数据库（Neo4j）或关系型数据库（PostgreSQL）的精准聚合查询（GraphQL/SQL），打通了“意图识别 -> 知识检索 -> 整合输出”的完整链路。
* **厚实知识库底蕴** : Neo4j图数据库内存储的14000+节点和网络关系，为Agent的回复提供坚实的后盾，保证回答的客观性真实性，极大减少了幻觉的产生。

### 4. 具备自生长性的知识图谱与系统迭代
* **绝对客观的知识骨架** : 通过对 百鸽API 的调用，载入最新的卡牌信息到PG数据库中，并且通过定时的MD5校验实现自动更新数据库内容，载入 Konami 新发布的卡片信息，保证实效性和客观性。
* **定时任务调度图谱自生长** : 系统感知PG数据库内的卡牌信息状态机，自动打捞数据并在Neo4j图数据库内生成实体节点和客观的结构化关系，从而实现知识图谱的自生长。
* **语义化关系生长耦合Agent成长** : 定时捞取卡牌节点触发Agent对话，集成Agent Skill，能够自动捞取卡牌并生成复杂的语义关系，如"SEARCH""AS_MATERIAL""COST"等。这些关系能够强化卡牌知识图谱的复杂性和专业性，得到了强化的知识图谱又能反哺Agent的专业性真实性，从而实现系统运转的良性循环和逻辑自洽。



## 🛠️ 技术栈清单

* **核心框架**：Spring Boot 3.x, Spring WebFlux, Project Reactor
* **AI 大脑**：火山引擎（Volcengine）Doubao-Pro
* **交互终端**：飞书（Lark）开放平台，CardKit 2.0 交互式卡片，WebSocket 长连接订阅
* **数据持久层**：PostgreSQL, JOOQ, MyBatis-Plus
* **知识引擎**：Neo4j
* **基础设施**：火山云 TOS 对象存储


## 🔁 迭代方向 - OCG线上社区及二创论坛
* **AIGC平台的功能拓展** : 基于复杂且全面的OCG卡牌知识网络和已经接入的TOS对象存储，能够实现更轻量级的游戏王卡图生成的功能，比如"生成阿不思和艾克莉西娅并肩作战的图片"，Agent能够自动检索知识图谱内的卡图信息，或基于丰富的卡图故事背景进行同人创作或卡牌diy
* **禁卡表预测和卡牌强度打分** ： 由于Neo4j图数据库保证的卡牌节点的强关系性，Agent能够根据卡与卡之间的联动给出一套卡组内的梯度排名，以及对一张卡在系统内的运转重要性进行打分，在这种紧密联系的背景中，这套架构的Agent大有可为。
* **CUA-Agent演进** : 基于丰富的OCG卡牌知识库和截图-图像识别技术，实现对 决斗链接（Duel Link），大师决斗（MD）等游戏王网络竞技游戏的"代打"

## 🚀 快速开始

本项目严格遵循环境隔离规范，拉取代码后请按照以下步骤初始化本地开发环境：

### 1. 环境准备
确保您的本地已安装 Java 21+、PostgreSQL 和 Neo4j。
在infra模块中的pom.xml文件的 build.plugins.plugin.configuration.jdbc 内填入数据库相关配置属性

### 2. 配置您的“密码本”
本项目的所有敏感密钥均已从 `application.yml` 中抽离。请在 `src/main/resources` 目录下的 `application-dev.yml` 文件：

```yaml
# application-dev.yml
secrets:
  feishu:
    app-id: your_feishu_app_id_here
    app-secret: your_feishu_app_secret_here
  doubao:
    api-key: your_doubao_api_key_here
  datasource:
    url: jdbc:postgresql://localhost:5432/zeroagent
    username: postgres
    password: your_db_password
  neo4j:
    uri: bolt://localhost:7687
    authentication:
      username: neo4j
      password: your_neo4j_password
```
### 3. 飞书机器人接入
在飞书后台开通应用，并输入应用相关密钥后启动项目，在机器人对话界面输入开始对话。
