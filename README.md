# Darwin 架构原理及关键技术说明

## 一、项目概述

Darwin（达尔文树皮蜘蛛）是一个企业级分布式网络爬虫系统，采用 Java 17 + Spring Boot 3.4 构建。系统名取自"达尔文树皮蜘蛛"——能结出世界上最大最牢固的网。

**核心能力：**

- 多类型数据抓取：文本（HTML/JSON/XML/CSS/JS）、资源（图片/视频/音频/PDF）、流媒体（M3U8/HLS）
- 自定义脚本解析：支持 Groovy 和 JavaScript，针对特定站点定制内容解析和链接提取逻辑
- 精细化并发控制：域名/站点粒度的抓取并发度控制和抓取间隔管理，保证抓取礼貌性
- 多层次数据管理：应用 -> 计划 -> 任务 -> 抓取记录，层级清晰

**产品地址：** https://darwin.manong.xin

---

## 二、系统架构总览

```
┌──────────────────────────────────────────────────────────────────────────┐
│                         darwin-vue (Vue 3 前端)                          │
│          Element Plus + GoJS + CodeMirror + ECharts/ApexCharts           │
└──────────────────────────────────┬───────────────────────────────────────┘
                                   │ REST API
┌──────────────────────────────────▼───────────────────────────────────────┐
│                       darwin-web (RESTful 接口层)                         │
│                  Jersey 3.1 + Jetty / Spring Boot 3.4                    │
└────┬───────────┬────────────┬───────────────┬────────────────────────────┘
     │           │            │               │
┌────▼─────┐ ┌──▼───────┐ ┌──▼──────────┐ ┌──▼──────────────────────┐
│ darwin-  │ │ darwin-  │ │   darwin-   │ │      darwin-runner      │
│ spider   │ │ parser   │ │   service   │ │     (后台调度线程)       │
│(爬虫引擎) │ │(脚本解析) │ │  (业务服务)  │ │  Allocator/PlanRunner   │
└────┬─────┘ └────┬─────┘ └──────┬──────┘ └──────────┬──────────────┘
     │            │              │                    │
     └────────────┴──────┬───────┴────────────────────┘
                         │
          ┌──────────────┼──────────────┐
     ┌────▼─────┐  ┌─────▼──────┐ ┌─────▼───────┐
     │ darwin-  │  │  darwin-   │ │   darwin-   │
     │  queue   │  │  common    │ │     log     │
     │(并发控制) │  │ (公共模型)  │ │  (切面日志)  │
     └──────────┘  └────────────┘ └─────────────┘

外部依赖:
┌──────────┐ ┌────────┐ ┌───────────┐ ┌───────┐ ┌───────┐
│  MySQL   │ │ Redis  │ │  Kafka /  │ │  OSS  │ │ etcd  │
│ (持久化)  │ │ (队列)  │ │ RocketMQ  │ │ (存储) │ │ (配置) │
└──────────┘ └────────┘ └───────────┘ └───────┘ └───────┘
```

---

## 三、模块职责与依赖关系

### 3.1 模块一览

| 模块 | 职责 | 核心依赖 |
|------|------|---------|
| **darwin-common** | 公共领域模型（App/Plan/Job/URLRecord/Rule 等）、常量和工具类 | 无内部依赖 |
| **darwin-log** | AOP 切面日志，涵盖 URL、任务、计划和并发操作 | common |
| **darwin-queue** | Redis 多级优先队列 + 并发连接池管理 + 抓取延迟控制 | common, Redisson |
| **darwin-service** | 业务服务层（CRUD + 事件监听 + 消息推送） | common, log, queue, MyBatis Plus |
| **darwin-parser** | Groovy/JavaScript 脚本编译、缓存和执行引擎 | common, service |
| **darwin-spider** | 爬虫核心（Router 路由 + Text/Resource/M3U8 Spider） | service, queue, parser, OkHttp3 |
| **darwin-runner** | 后台守护线程（URL 分配、周期计划调度、监控清理） | service, queue |
| **darwin-spider-boot** | Spider 独立部署启动模块，消费 MQ 消息执行抓取 | spider |
| **darwin-web** | RESTful API，聚合所有模块提供 Web 管理服务 | 全部模块 |
| **darwin-vue** | Vue 3 前端应用 | 独立前端工程 |
| **darwin-client** | 外部系统集成 SDK | common |

### 3.2 模块依赖图

```
darwin-common (基础层 - 无内部依赖)
    ^
    ├── darwin-log (切面日志)
    │
    ├── darwin-queue (并发管理)
    │       依赖: common, Redisson
    │
    ├── darwin-service (业务服务)
    │       依赖: common, log, queue
    │
    ├── darwin-parser (脚本解析)
    │       依赖: common, service
    │
    ├── darwin-spider (爬虫引擎)
    │       依赖: service, queue, parser
    │
    ├── darwin-runner (后台线程)
    │       依赖: service, queue
    │
    ├── darwin-spider-boot (爬虫应用)
    │       依赖: spider
    │
    ├── darwin-web (Web服务 - 聚合全部模块)
    │       依赖: service, parser, spider, runner
    │
    └── darwin-client (外部SDK)
            依赖: common
```

---

## 四、核心数据模型

### 4.1 领域模型层次

```
App (应用)                             ← 业务归属单位
 ├── Plan (计划)                       ← 一次型 / 周期型
 │    ├── Rule[] (规则)                ← 正则匹配 + Groovy/JS 解析脚本
 │    │    └── RuleHistory[] (历史)    ← 版本追溯与回滚
 │    ├── Seed[] (种子URL)             ← 起始链接, 含优先级/自定义头/请求体
 │    └── Job[] (任务)                 ← 由计划生成的抓取执行单元
 │         └── URLRecord[] (抓取记录)   ← 抓取状态/内容/解析结果/子链
 ├── AppUser[] (应用用户)
 └── AppSecret[] (应用密钥)            ← AK/SK 认证
```

### 4.2 核心实体说明

| 实体 | 说明 |
|------|------|
| **App** | 应用是业务归属单位，所有计划、任务及链接均归属于某个应用 |
| **Plan** | 计划是任务的元数据定义，分为**一次型**（生成一次性任务）和**周期型**（按 crontab 周期生成任务） |
| **Job** | 数据抓取调度及归属单元，由计划通过手动或调度触发生成 |
| **Rule** | 规则负责页面解析，包含匹配 URL 的正则表达式和解析脚本（Groovy/JavaScript） |
| **RuleHistory** | 规则每次修改产生一个历史版本，支持追溯和回滚 |
| **Seed** | 种子 URL 记录，包含 URL、优先级、自定义请求头、请求体、链接范围等配置 |
| **URLRecord** | URL 抓取记录，包含抓取信息、内容类型、解析结果、父子链关系 |
| **Proxy** | 代理 IP 配置，区分长效代理（长期有效）和短效代理（有过期时间） |

### 4.3 URL 生命周期

```
CREATE ──→ QUEUING ──→ FETCHING ──→ SUCCESS
                                  ├─→ FAIL
                                  └─→ OVERFLOW (排队超时)
```

### 4.4 数据库 Schema

系统使用 MySQL 8.0（InnoDB, utf8mb4），核心表结构：

| 表名 | 主键 | 说明 |
|------|------|------|
| `app` | id (auto_increment) | 应用信息 |
| `app_user` | id (auto_increment) | 应用用户映射 |
| `app_secret` | id (auto_increment) | 应用 AK/SK 密钥 |
| `plan` | plan_id (varchar 32) | 抓取计划，含 crontab 表达式和状态 |
| `job` | job_id (varchar 32) | 抓取任务，关联 plan_id |
| `rule` | id (auto_increment) | 解析规则，关联 plan_id |
| `rule_history` | id (auto_increment) | 规则历史版本，关联 rule_id |
| `seed` | key (varchar 32) | 种子 URL，关联 plan_id |
| `url` | key (varchar 32) | URL 抓取记录，含状态/内容/字段等 |
| `proxy` | id (auto_increment) | 代理 IP 配置 |
| `message` | id (auto_increment) | 系统消息日志 |
| `trend` | id (auto_increment) | 趋势统计数据 |

---

## 五、关键技术详解

### 5.1 并发控制模型（darwin-queue）

并发控制是 Darwin 最核心的设计，通过 Redis 实现分布式并发控制和优先级调度，保证对目标站点的抓取礼貌性。

#### 5.1.1 ConcurrencyQueue — 多级优先队列

以**并发单元**（host 或 domain）为粒度，每个并发单元维护 3 个 Redis 阻塞队列：

```
┌─────────────────────────────────────────────┐
│            ConcurrencyQueue                 │
│                                             │
│  并发单元: example.com                       │
│  ┌───────────────────────────────────────┐  │
│  │ HIGH_PRIORITY_example.com   [URL...]  │  │  ← 优先出队
│  ├───────────────────────────────────────┤  │
│  │ NORMAL_PRIORITY_example.com [URL...]  │  │
│  ├───────────────────────────────────────┤  │
│  │ LOW_PRIORITY_example.com    [URL...]  │  │  ← 最后出队
│  └───────────────────────────────────────┘  │
│                                             │
│  并发单元: another.host.com                  │
│  ┌───────────────────────────────────────┐  │
│  │ HIGH_PRIORITY_another.host.com  [...] │  │
│  ├───────────────────────────────────────┤  │
│  │ NORMAL_PRIORITY_another.host.com [...] │  │
│  ├───────────────────────────────────────┤  │
│  │ LOW_PRIORITY_another.host.com   [...] │  │
│  └───────────────────────────────────────┘  │
│                                             │
│  CONCURRENCY_UNIT_SET: {example.com, ...}   │
└─────────────────────────────────────────────┘
```

**核心特性：**

- **优先级抢占调度：** 出队时先清空高优先级队列，再处理中、低优先级，保证高优先级链接优先抓取
- **分布式锁：** 提供全局入队锁和出队锁（基于 Redisson），保证同一时刻只有一个线程执行入队/出队操作
- **内存水位监控：** 计算 `usedMemoryRss / maxMemory`，超过危险阈值时拒绝入队，防止 Redis OOM
- **Snappy 压缩：** 使用 Snappy 编解码器压缩队列数据，降低 Redis 内存占用

#### 5.1.2 ConcurrencyControl — 连接池管理

以 Redis Map 管理每个并发单元的活跃抓取连接：

```
Redis Key: CONCURRENCY_CONTROL_{concurrency_unit}
Redis Value: Map<url_record_key, creation_timestamp>

可用连接数 = 最大并发数 - 当前活跃连接数
```

**核心功能：**

- `putConnection`：注册活跃连接（抓取开始时调用）
- `removeConnection`：释放连接（抓取完成时调用）
- `allowFetching`：判断并发单元是否还有可用连接
- `getAvailableConnections`：获取并发单元可用连接数
- 连接具有 TTL，超时由 ConcurrencyQueueMonitor 强制回收，防止异常导致连接池耗尽

#### 5.1.3 CrawlDelayControl — 抓取延迟控制

```
记录每个并发单元最近一次抓取时间
  → 下次抓取前检查间隔是否满足配置要求
  → 不满足则等待，实现对目标站点的礼貌性控制
```

### 5.2 调度分配机制（darwin-runner）

#### 5.2.1 Allocator — URL 链接分配器

Allocator 是连接 ConcurrencyQueue 和消息队列的桥梁，定期从队列中弹出 URL 并分发至下游爬虫。

**执行流程：**

```
┌──────────────────────────────────────────────────────┐
│                  Allocator.execute()                  │
│                                                      │
│  1. 获取 ConcurrencyQueue 出队锁                      │
│     └─ 失败 → 放弃本轮调度                             │
│                                                      │
│  2. 检查 MQ 消费端积压                                 │
│     └─ lag > 2000 → 跳过本轮，防止下游过载              │
│                                                      │
│  3. 遍历所有并发单元:                                   │
│     ├─ 查询可用连接数 (ConcurrencyControl)             │
│     │   └─ 可用连接数 ≤ 0 → 跳过该并发单元              │
│     ├─ 按可用数从队列弹出 URL                          │
│     └─ 针对每条 URL:                                  │
│         ├─ 排队时间 > 阈值(默认2小时)                   │
│         │   → handleOverflow: 标记 OVERFLOW 状态       │
│         └─ 正常 URL                                   │
│             → handleNormal: 状态改为 FETCHING          │
│             → 注册连接至 ConcurrencyControl            │
│             → 推送至 Kafka Topic                      │
│                                                      │
│  4. 释放出队锁                                        │
└──────────────────────────────────────────────────────┘
```

#### 5.2.2 PlanRunner — 周期型计划调度器

PlanRunner 负责定期检查和执行到期的周期型计划。

**执行流程：**

```
┌──────────────────────────────────────────────────────┐
│                  PlanRunner.execute()                 │
│                                                      │
│  1. 获取 ConcurrencyQueue 入队锁                      │
│     └─ 失败 → 放弃本轮调度                             │
│                                                      │
│  2. 分页加载周期型计划 (每页100条):                      │
│     ├─ 筛选: plan.nextTime ≤ 当前时间                  │
│     ├─ 去重: 跳过本轮已执行的计划                       │
│     └─ 执行:                                         │
│         ├─ PlanExecutor.execute(plan) → 生成 Job      │
│         ├─ 种子 URL 入 ConcurrencyQueue               │
│         └─ 更新 plan.nextTime (基于 crontab 表达式)    │
│                                                      │
│  3. 释放入队锁                                        │
└──────────────────────────────────────────────────────┘
```

### 5.3 爬虫路由与多类型抓取（darwin-spider）

#### 5.3.1 Router — 路由引擎

Router 是爬虫的统一入口，采用**媒体类型路由 + 处理链**设计模式：

```
┌──────────────────────────────────────────────────────┐
│                  Router.route(record)                 │
│                                                      │
│  1. 并发控制检查                                      │
│     └─ 超出限制 → 推回 ConcurrencyQueue               │
│                                                      │
│  2. 抓取延迟检查 (CrawlDelayControl)                  │
│     └─ 不满足间隔要求 → 等待                           │
│                                                      │
│  3. 加载计划上下文 (最大深度/分发配置)                   │
│                                                      │
│  4. 选择输入源:                                       │
│     ├─ 已缓存内容 → OSSInput (从 OSS 读取)            │
│     └─ 未缓存    → HTTPInput (发起网络请求)            │
│                                                      │
│  5. 分析 Content-Type, 路由到对应 Spider               │
│     ┌─────────────┬────────────────┬──────────────┐  │
│     │ TextSpider  │ ResourceSpider │  M3U8Spider  │  │
│     │ HTML/JSON.. │ 图片/视频/音频  │  HLS视频流   │  │
│     └──────┬──────┴────────┬───────┴──────┬───────┘  │
│            │               │              │          │
│  6. 处理链循环                                        │
│     └─ Spider 可返回新 MediaType 触发下一轮路由         │
│        (如 TextSpider 检测到 M3U8 → 路由至 M3U8Spider) │
│                                                      │
│  7. 完成处理:                                         │
│     ├─ 更新 URLRecord 至数据库 (含3次重试)             │
│     ├─ 从 ConcurrencyControl 移除连接                 │
│     └─ 触发 URLEventListener / JobEventListener       │
└──────────────────────────────────────────────────────┘
```

#### 5.3.2 Spider 实现体系

Spider 采用**模板方法模式**，基类定义处理骨架，子类实现具体逻辑：

```java
// 抽象基类
abstract class Spider {
    abstract MediaType handle(URLRecord record, Input input, Context context);
    abstract List<MediaType> supportedMediaTypes();
    // 初始化时自动注册到 Router
    void afterPropertiesSet() { router.registerSpider(mediaType, this); }
}
```

| Spider 实现 | 支持的媒体类型 | 关键逻辑 |
|------------|--------------|---------|
| **TextSpider** | HTML, XHTML, JSON, XML, CSS, JavaScript, CSV, PlainText | 多阶段字符集探测；M3U8 流检测；调用 ParseService 执行用户解析脚本 |
| **ResourceSpider** | Image, Video, Audio, PDF | 二进制流下载，写入 OSS 存储 |
| **M3U8Spider** | HLS M3U8 点播视频流 | M3U8 播放列表解析，TS 分片下载合并 |

**TextSpider 字符集探测顺序：**

```
1. HTTP Response Content-Type charset 声明
2. HTML <meta> 标签 charset 声明
3. 启发式字符集检测 (前1024字节)
4. UTF-8 兜底
```

#### 5.3.3 输入/输出抽象

系统对数据读写进行了抽象，支持多种输入输出源：

| 类型 | 输入 (Input) | 输出 (Output) |
|------|-------------|--------------|
| 网络 | HTTPInput | — |
| 云存储 | OSSInput | OSSOutput |
| 磁盘 | DiskInput | DiskOutput |
| 内存 | ByteArrayInput | ByteArrayOutput |
| 流媒体 | M3U8Input | — |

### 5.4 脚本解析引擎（darwin-parser）

支持 **Groovy** 和 **JavaScript**（GraalVM）两种脚本引擎，用户通过自定义脚本实现内容解析和链接提取。

#### 5.4.1 编译与缓存策略

```
┌─────────────────────────────────────────────────────┐
│              Script 编译缓存机制                      │
│                                                     │
│  Groovy 脚本:                                       │
│    Cache Key = MD5(script_code)                     │
│    线程安全, 可跨线程共享                              │
│                                                     │
│  JavaScript 脚本:                                   │
│    Cache Key = MD5(thread_id + script_code)         │
│    GraalVM Context 非线程安全, 每线程独立缓存          │
│                                                     │
│  并发安全: Double-checked locking                    │
│  生命周期: 引用计数管理, 计数归零时清除                 │
└─────────────────────────────────────────────────────┘
```

#### 5.4.2 脚本模板机制

用户脚本被包裹在框架模板中，模板提供标准化的输入/输出接口：

```
用户脚本 → 插入模板 (%GROOVY_SCRIPT% 占位符替换)
         → 编译 (ScriptFactory.make())
         → 虚拟执行验证语法正确性
         → 缓存编译结果
```

#### 5.4.3 规则匹配与解析流程

```
URL 抓取完成
  → 遍历计划关联的 Rule 列表
  → 正则表达式匹配 URL
  → 命中规则 → 加载脚本 (缓存优先)
  → 执行解析 → 返回 ParseResponse
     ├── fieldMap: 结构化字段 (标题/正文/日期等)
     └── links[]: 子链接列表 → 入 ConcurrencyQueue 继续抓取
```

### 5.5 消息驱动架构

系统通过消息队列实现模块间解耦，支持 **Kafka**（主）和 **RocketMQ**（备选）。

#### 5.5.1 消息流拓扑

```
┌────────────┐    Kafka Topic     ┌───────────────────┐
│ PlanRunner │ ──(DISPATCH-JOB)──→│                   │
└────────────┘                    │    Allocator       │
                                  │  (URL链接分配器)    │
┌────────────┐    Redis Queue     │                   │
│ Concurr-   │ ←─(push/pop)────→ │                   │
│ encyQueue  │                    └─────────┬─────────┘
└────────────┘                              │
                                  Kafka Topic (DISPATCH-RECORD)
                                            │
                                  ┌─────────▼─────────┐
                                  │  darwin-spider-    │
                                  │      boot          │
                                  │  (爬虫消费者)       │
                                  └─────────┬─────────┘
                                            │
                                  ┌─────────▼─────────┐
                                  │ URLEventListener   │
                                  │  (结果分发)         │
                                  └─────────┬─────────┘
                                            │
                                  Kafka Topic (自定义)
                                            │
                                  ┌─────────▼─────────┐
                                  │    外部系统消费     │
                                  └───────────────────┘
```

#### 5.5.2 消息 Topic 说明

| Topic | 生产者 | 消费者 | 用途 |
|-------|--------|--------|------|
| DISPATCH-RECORD | Allocator | darwin-spider-boot | URL 抓取任务分发 |
| DISPATCH-JOB | PlanRunner | Allocator | 任务级别调度 |
| 自定义 Topic | URLEventListener | 外部系统 | 抓取结果分发（可选开启） |

#### 5.5.3 事件监听机制

- **URLEventListener：** URL 抓取完成后，根据 `allowDispatch` 配置决定是否将结果推送至下游消息队列
- **JobEventListener：** 任务完成后更新任务状态、发送任务完成通知

### 5.6 后台监控线程（darwin-runner）

| 线程 | 职责 |
|------|------|
| **Allocator** | 定期从 ConcurrencyQueue 弹出 URL，分发至消息队列 |
| **PlanRunner** | 定期检查并执行到期的周期型计划 |
| **ConcurrencyQueueMonitor** | 清理无效并发单元；清理长期未处理链接 |
| **ExpiredCleaner** | 清理过期抓取数据、过期任务和大盘统计数据 |
| **ProxyMonitor** | 定期刷新短效代理缓存 |
| **DashboardRunner** | 首页大盘统计数据采集 |

---

## 六、存储架构

| 存储类型 | 技术选型 | 用途 |
|---------|---------|------|
| 关系型存储 | MySQL 8.0 (MyBatis Plus + Druid 连接池) | App/Plan/Job/Rule/URL 等核心业务数据 |
| 分布式缓存 | Redis (Redisson + Snappy 压缩) | 并发队列、连接池、分布式锁、抓取延迟记录 |
| 对象存储 | 阿里云 OSS | 下载内容存储（HTML/图片/视频/音频/PDF 等） |
| NoSQL | 阿里云 TableStore (OTS) | 可选，大数据量场景下的 URL 记录和任务记录存储 |
| 配置中心 | etcd (Jetcd 客户端) | 动态配置管理，通过 EtcdPropertySourceFactory 加载 |

**存储选型策略：**

- URL 记录默认存储于 MySQL，当数据量较大时可切换至阿里云 OTS
- 下载的原始内容统一存储于 OSS，URLRecord 中记录 `fetchContentUrl` 指向 OSS 地址
- Redis 中仅存储热数据（队列、连接状态），使用 Snappy 压缩降低内存占用

---

## 七、核心流程时序

### 7.1 完整抓取流程

```
1. 计划执行 (手动触发 / PlanRunner 周期调度)
   │
   ├─ PlanExecutor.execute(plan)
   ├─ 生成 Job (爬虫任务)
   └─ 种子 URL 入 ConcurrencyQueue (Redis)

2. URL 调度分配 (Allocator 定期执行)
   │
   ├─ 获取出队锁
   ├─ 检查 MQ 消费端积压 (lag > 2000 则跳过)
   ├─ 遍历并发单元, 检查可用连接数
   ├─ 弹出 URL, 检查排队是否超时
   │   ├─ 超时 → OVERFLOW
   │   └─ 正常 → FETCHING → 推送至 Kafka
   └─ 释放出队锁

3. 爬虫抓取 (darwin-spider-boot 消费 Kafka)
   │
   ├─ Router.route(record)
   ├─ 并发控制检查 & 抓取延迟检查
   ├─ 选择输入源 (HTTP / OSS)
   ├─ 路由至 TextSpider / ResourceSpider / M3U8Spider
   ├─ TextSpider: 下载文本 → 执行用户脚本解析
   │   ├─ 提取结构化数据 (fieldMap)
   │   └─ 提取子链接 → 入 ConcurrencyQueue 继续抓取
   └─ ResourceSpider: 下载资源 → 存储至 OSS

4. 完成处理
   │
   ├─ 更新 URLRecord 至数据库
   ├─ ConcurrencyControl 释放连接
   ├─ URLEventListener: 结果按需分发至下游
   └─ JobEventListener: 检查任务是否全部完成
       └─ 全部完成 → 更新 Job 状态, 发送任务完成通知
```

### 7.2 周期型计划调度时序

```
PlanRunner                ConcurrencyQueue          PlanExecutor
    │                          │                        │
    ├─ acquirePushLock() ─────→│                        │
    │                          ├─ 返回锁                 │
    │                          │                        │
    ├─ 加载到期的周期型计划 ──────────────────────────────→│
    │                          │                        │
    │  对每个到期计划:           │                        │
    ├──────────────────────────────── execute(plan) ───→│
    │                          │                        ├─ 生成Job
    │                          │←── push(seedURLs) ─────┤
    │                          │                        │
    ├─ updateNextTime(plan)    │                        │
    │                          │                        │
    ├─ releasePushLock() ─────→│                        │
    │                          │                        │
```

### 7.3 URL 分配时序

```
Allocator              ConcurrencyQueue    ConcurrencyControl    MessageQueue
    │                       │                     │                   │
    ├─ acquirePopLock() ───→│                     │                   │
    │                       │                     │                   │
    │  对每个并发单元:        │                     │                   │
    ├───────────────────────────── available? ────→│                   │
    │                       │                     ├─ 返回可用连接数     │
    │                       │                     │                   │
    ├─ pop(unit, n) ───────→│                     │                   │
    │                       ├─ 返回URL列表         │                   │
    │                       │                     │                   │
    │  对每条URL:            │                     │                   │
    │  [超时] handleOverflow │                     │                   │
    │  [正常] ───────────────────── putConn() ───→│                   │
    │         ────────────────────────────────────────── push(url) ──→│
    │                       │                     │                   │
    ├─ releasePopLock() ───→│                     │                   │
    │                       │                     │                   │
```

---

## 八、技术栈总览

### 8.1 后端技术

| 类别 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 17 |
| 框架 | Spring Boot | 3.4.10 |
| REST | Jersey (JAX-RS) | 3.1.9 |
| Web 容器 | Jetty | — |
| ORM | MyBatis Plus | 3.5.14 |
| 连接池 | Druid | — |
| 数据库 | MySQL | 8.0 |
| 缓存 | Redisson (Redis) | 3.38.1 |
| 消息队列 | Apache Kafka | 4.0.0 |
| 消息队列 (备选) | Apache RocketMQ | 5.3.3 |
| 配置中心 | etcd (Jetcd) | 0.8.3 |
| 对象存储 | 阿里云 OSS | 3.18.1 |
| NoSQL (可选) | 阿里云 TableStore | 5.13.10 |
| 脚本引擎 | Groovy | 3.0.16 |
| 脚本引擎 | GraalVM JavaScript | 24.2.1 |
| HTTP 客户端 | OkHttp3 | 4.12.0 |
| HTML 解析 | jsoup | 1.15.3 |
| 任务调度 | Quartz | 2.3.2 |
| 序列化 | FastJSON | 2.0.35 |
| 压缩 | Snappy | 1.1.10.7 |
| Excel 处理 | Apache POI | 5.4.1 |
| 视频处理 | FFmpeg (JavaCV) | 5.0-1.5.7 |
| 认证 | Hylian Client | 0.0.4 |

### 8.2 前端技术

| 类别 | 技术 | 版本 |
|------|------|------|
| 框架 | Vue 3 | 3.4.37 |
| 构建 | Vite | 5.4.1 |
| 状态管理 | Pinia | 2.2.2 |
| 路由 | Vue Router | 4.4.4 |
| UI 组件库 | Element Plus | 2.8.3 |
| 图表 | ECharts / ApexCharts | 5.6.0 / 4.7.0 |
| 图可视化 | GoJS | 3.0.23 |
| 代码编辑器 | CodeMirror 6 | — |
| 视频播放 | Video.js | 8.22.0 |
| 音频播放 | Wavesurfer.js | 7.9.5 |
| PDF 预览 | Vue3-PDF-App | 1.0.3 |
| HTTP 客户端 | Axios | 1.7.7 |

---

## 九、部署架构

系统采用**管理面与数据面分离**的部署模型：

```
┌───────────────────────────────────────────────┐
│              管理面 (darwin-web)               │
│                                               │
│  ┌─────────┐ ┌─────────┐ ┌───────────────┐   │
│  │ REST API│ │ Runner  │ │   Dashboard   │   │
│  │ 接口服务 │ │ 后台线程 │ │   统计服务     │   │
│  └─────────┘ └─────────┘ └───────────────┘   │
│                                               │
│  Jetty (端口 10000/10001, SSL)                │
└──────────────────────┬────────────────────────┘
                       │ Kafka
┌──────────────────────▼────────────────────────┐
│          数据面 (darwin-spider-boot)            │
│                                               │
│  ┌──────────┐ ┌──────────┐ ┌──────────────┐  │
│  │ Text     │ │ Resource │ │   M3U8       │  │
│  │ Spider   │ │ Spider   │ │   Spider     │  │
│  └──────────┘ └──────────┘ └──────────────┘  │
│                                               │
│  可水平扩展, 多实例部署                          │
└───────────────────────────────────────────────┘
```

- **darwin-web** 负责 API 服务、后台调度线程和管理界面
- **darwin-spider-boot** 负责消费 MQ 消息执行抓取，可独立水平扩缩容
- 两者通过 Kafka/RocketMQ 解耦，spider-boot 实例数量可根据抓取负载弹性调整

---

## 十、关键设计决策

| 设计点 | 决策 | 原因 |
|--------|------|------|
| Web 容器 | Jetty (排除 Tomcat) | 轻量级，适合长连接爬虫场景 |
| REST 框架 | Jersey 3.1 (JAX-RS) | JAX-RS 标准实现，注解驱动，与 Spring Boot 集成良好 |
| 队列存储 | Redis (非内存队列) | 分布式部署支持、数据持久化、故障恢复能力 |
| 脚本引擎 | Groovy + GraalVM JS | Groovy 与 Java 生态无缝集成；GraalVM 提供高性能 JS 执行 |
| 数据压缩 | Snappy | Redis 内存优化，低延迟压缩/解压，适合高频读写 |
| 分布式锁 | Redisson 分布式锁 | 保证 Allocator/PlanRunner 单点执行，避免重复调度 |
| 部署模型 | Web + Spider-boot 分离 | 管理面与数据面独立扩缩容，资源利用最优化 |
| URL 存储 | MySQL + OTS 可选 | 小规模用 MySQL，大数据量可切换至 TableStore |
| 内容存储 | OSS | 抓取内容与元数据分离，降低数据库压力 |
| 配置管理 | etcd | 支持动态配置变更，无需重启服务 |