# ZeroAgent

> 面向游戏王 OCG 场景的 AI Agent 与视觉互动实验项目

ZeroAgent 是一个围绕 **游戏王 OCG 专业知识、智能问答、知识图谱、自主任务调度和视觉互动生成** 持续演进的 AI 应用项目。

它并不满足于做一个“能聊天的大模型壳子”，而是尝试把：

- 大模型的推理能力
- OCG 卡牌事实库
- Neo4j 图谱关系
- 飞书交互终端
- 图像生成能力

组合成一个真正理解垂直领域语境、能持续生长、也能与用户产生视觉互动的 Agent 系统。

---

## Why ZeroAgent

游戏王 OCG 是一个信息密度极高、知识结构极复杂的垂直领域。

玩家真正遇到的问题，往往不是“找不到模型”，而是：

- **查卡仍然低效**
  传统卡查工具更擅长关键词匹配，不擅长理解自然语言描述、模糊别称和实际对局语境。

- **规则、判例、展开理解高度碎片化**
  很多问题不是单张卡效果，而是卡组结构、关系链条、时点处理和博弈点判断。

- **通用大模型在 OCG 领域极易幻觉**
  一旦问到冷门卡、复杂连锁、卡组动点或特定构筑，模型经常会“像知道一样胡说”。

- **视觉互动内容仍然缺乏垂直语义**
  普通图生图只能把图片“变好看”，但难以真正回应 OCG 世界观、卡组风格和角色感。

ZeroAgent 的目标，就是把这些问题拆成一套可持续积累的能力体系：

- 用结构化数据与图谱降低幻觉
- 用 Agent 和工具调用提升专业性
- 用 `AiTask` 把复杂 AI 能力收敛到统一任务底座
- 用图像生成能力，把“问答”扩展成“视觉互动”

---

## What Makes It Different

### 1. 不只是聊天，而是垂直领域 Agent

ZeroAgent 的定位从来不是通用聊天机器人。

它更像一个专注于游戏王 OCG 的 AI 应用底座，当前已经覆盖：

- 垂直领域问答
- 流式聊天回复
- 工具调用
- 卡牌信息检索
- 知识图谱补充
- 异步 AI 任务调度

后续还将继续扩展到视觉互动、场景化创作和更复杂的智能内容生成。

### 2. 用 PostgreSQL + Neo4j 给大模型“补地基”

ZeroAgent 不把领域真实性完全交给模型记忆。

项目通过：

- PostgreSQL 存储卡牌客观事实
- Neo4j 建立卡牌关系网络
- Agent 在需要时调用结构化能力补充事实依据

让模型输出不再只靠参数记忆，而是建立在可查询、可更新、可校验的知识底座之上。

### 3. AiTask 让 AI 能力变成统一任务系统

很多 AI 项目一开始能跑 demo，但一旦进入：

- 异步提交
- 状态流转
- 重试和取消
- 多类任务并存
- 多种提供方接入

就会迅速失控。

ZeroAgent 在领域层内抽象了统一的 `AiTask` 任务体系，把 AI 能力接入收敛到：

- 统一任务模型
- 统一调度器
- 统一状态机
- 统一 `AiTaskHandler` 扩展点

这样后续无论是生图、生成式处理，还是更多 AI 能力接入，都有可复用的框架底座。

### 4. 从“文本问答”走向“视觉互动”

ZeroAgent 当前正在推进的一个重要方向，是把 AI 从文本互动拓展到视觉互动。

在第一阶段，项目聚焦一个轻量但有辨识度的玩法：

- 用户在飞书发送图片
- 系统基于原图生成带有游戏王 OCG 风格的视觉改造图
- 在都市、星空、角色姿态等场景下，自动贴近元素英雄、银河眼等典型卡组气质
- 适时加入“背后灵”或守护灵式的动漫表达

这条链路的价值不只是“生成一张图”，而是在探索：

> 当用户向 AI 发送一张图片时，AI 如何回一张真正有世界观、有角色感、有互动意味的专属内容。

---

## Core Capabilities

### 流式 AI 聊天

- 基于 Spring WebFlux + Reactor
- 支持大模型流式输出
- 已打通飞书交互端
- 支持推理内容与最终内容的流式展示

### 工具调用与结构化检索

- 支持面向 OCG 场景的工具调用扩展
- 能把自然语言问题转成结构化查询和知识补充
- 为复杂卡牌问答提供更强的事实支撑

### 卡牌知识库与知识图谱

- 通过外部卡牌数据源同步 OCG 数据
- 写入 PostgreSQL 构建客观事实库
- 写入 Neo4j 构建关系网络
- 持续增强 Agent 的专业性与可解释性

### 统一 AI 任务调度

- 已具备 `AiTask` 异步任务框架
- 支持按 `taskType + bizType` 路由处理器
- 支持任务轮询、执行、状态流转与持久化
- 为后续图像类、生成类能力提供统一底座

### 视觉互动生成

- 基于飞书图片输入做互动式图生图
- 当前聚焦 Seedream 方向的基础能力接入
- 目标是把普通图片转化为更具 OCG 世界观氛围的互动内容

---

## Architecture

ZeroAgent 采用清晰的分层设计，保证核心语义和基础设施解耦：

- `bootstrap`
  启动装配层
- `api`
  HTTP / SSE 接口接入层
- `biz`
  业务编排层
- `domain`
  核心领域语义、任务状态机、协议抽象
- `infra`
  数据库、飞书、Neo4j、TOS、LLM、图像生成等具体实现
- `common`
  通用能力模块

这意味着：

- 更换交互终端，不需要推翻领域模型
- 更换大模型提供方，不需要重写核心语义
- 新增一种 AI 任务，不需要重搭调度框架

---

## Current Directions

ZeroAgent 当前最值得关注的三个方向：

### 1. OCG 专业问答 Agent

持续强化卡牌检索、规则理解、图谱关系补充和自然语言问答能力。

### 2. 自生长知识图谱

通过任务调度不断补充和维护 OCG 结构化关系，使知识底座持续增强。

### 3. AI 视觉互动

围绕“用户发图，AI回图”的交互方式，探索更具社交传播力的垂直视觉内容生成。

相比单纯滤镜或普通动漫化，这个方向更强调：

- 风格模板
- 世界观映射
- 角色感
- 场景回应
- 持续互动潜力

---

## Tech Stack

- **Java 21**
- **Spring Boot 3**
- **Spring WebFlux / Reactor**
- **PostgreSQL**
- **jOOQ**
- **Neo4j**
- **Feishu Open Platform**
- **Volcengine Doubao**
- **Volcengine TOS**


---

## Quick Start

### 环境准备

请先准备：

- Java 21+
- PostgreSQL
- Neo4j
- 飞书应用配置
- 豆包 API Key
- 火山云 TOS 配置

### 配置文件

项目默认使用 `dev` profile。

请补全：

[`app/bootstrap/src/main/resources/application-dev.yml`](./app/bootstrap/src/main/resources/application-dev.yml)

最小配置项包括：

```yaml
llm:
  api-key: your_doubao_api_key

feishu:
  app-id: your_feishu_app_id
  app-secret: your_feishu_app_secret

datasource:
  url: jdbc:postgresql://localhost:5432/ZeroAgent
  username: your_pg_user

neo4j:
  uri: bolt://localhost:7687
  authentication:
    username: neo4j
    password: your_neo4j_password

volcengine:
  cloud:
    region: your_region
    access-key: your_access_key
    secret-key: your_secret_key
    tos:
      bucket: your_bucket
```

### 启动

```bash
./mvnw -pl app/bootstrap spring-boot:run
```

---

## Vision

ZeroAgent 想做的，不是一个只会回答问题的 OCG 机器人。

它更接近一个面向垂直知识、视觉互动和持续任务执行能力的 AI 应用原型：

- 既能说
- 也能查
- 还能长
- 并逐步学会用视觉内容回应用户

如果你也对 **垂直 Agent、知识图谱、异步 AI 任务框架、视觉互动生成** 这些方向感兴趣，这个项目值得继续往下看。
