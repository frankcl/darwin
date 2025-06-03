# darwin

 * 爬虫系统：达尔文树皮蜘蛛，能结出世界上最大最牢固的网
 * 产品地址：https://darwin.manong.xin

## 特点

* 支持自定义脚本，针对特定站点定制抽链和结构化逻辑
* 支持域名/站点粒度抓取调度：具备礼貌性控制，管理站点/域名粒度单位时间并发度和抓取间隔
* 支持多类型数据抓取：文本（HTML、JSON等）、资源（图片、视频、音频、PDF等）
* 多层次爬虫数据管理：应用->计划->任务->抓取记录

## 模块

### 1. darwin-common 公共代码
- [x] 数据模型定义
- [x] 工具定义

### 2. darwin-queue 并发控制及并发队列
- [x] 并发控制：管理并发单元（站点/域名）抓取连接生命周期、最大并发数及抓取间隔
- [x] 并发队列：并发单元抓取调度，保证优先级及平滑度

### 3. darwin-parser 抓取内容解析
- [x] 脚本解析引擎框架：支持Groovy和JavaScript
- [x] 脚本解析服务：使用用户定义脚本对抓取内容进行解析

### 4. darwin-service 服务定义和实现
- [x] 应用
- [x] 应用用户管理
- [x] 计划
- [x] 任务
- [x] 代理管理
- [x] 规则及规则历史
- [x] 抓取记录：数据和种子
- [x] OSS服务

### 5. darwin-spider 爬虫
- [x] 爬虫路由：根据数据类型路由不通爬虫实现
- [x] 爬虫：根据数据类型抓取数据

### 6. darwin-spider-boot 爬虫应用启动
- [x] 接收爬取数据，启动爬虫应用

### 7. darwin-runner 各类后台线程
- [x] 抓取数据分配
- [x] 周期型计划执行
- [x] 首页大盘统计
- [x] 并发队列监控
- [x] 过期数据清理

### 8. darwin-web 爬虫RESTFul服务
- [x] 应用服务接口
- [x] 应用用户管理接口
- [x] 计划服务接口
- [x] 任务服务接口
- [x] 代理服务接口
- [x] 规则及规则分组服务接口
- [x] 抓取记录服务接口

### 9. darwin-log 切面日志
- [x] 切面日志管理：涉及URL、并发单元、任务和计划

### 10. darwin-vue 前端应用

## 架构原理

### 基本概念

* 应用：业务归属单位，所有爬虫计划、任务及链接归属于应用
* 计划：爬虫抓取计划，计划是任务的元数据定义，计划负责生成任务，一个计划归属于一个应用，包含一组规则、一系列种子URL和爬虫任务，计划分2类
  * 一次型计划：计划根据种子列表和规则列表生成一次性爬虫任务
  * 周期型计划：计划根据调度周期对种子列表反复生成爬虫任务
* 任务：数据抓取调度及归属单元，一个任务由归属计划生成，通过手动执行计划或系统调度计划生成
* 规则：规则负责页面解析工作，包含匹配URL的正则表达式及解析页面的脚本，脚本支持Groovy和JavaScript
* 规则历史：为保证规则每一次修改可追溯、可回滚，每次修改规则将产生一个规则历史版本
* 代理：代理IP，区分长效代理和短效代理
  * 长效代理：代理IP长期有效
  * 短效代理：代理IP具有有效时间
* URL记录：包含抓取信息、抓取内容及解析结果
* 并发单元：控制单位时间内的抓取并发数量，支持domain和host级别，框架通过并发单元控制达到对目标站点的抓取礼貌性

### darwin架构

![architecture](https://github.com/frankcl/darwin/blob/main/images/darwin%E7%B3%BB%E7%BB%9F%E6%9E%B6%E6%9E%84.png)

* 数据存储层：负责基础数据存储，例如应用、计划、任务、规则及URL记录等
  * URL记录：存储于MySQL或OTS（可根据数据量选择），下载内容存储于OSS
  * 任务记录：存储于MySQL或OTS（可根据数据量选择）
* 并发管理层：负责站点和域名抓取并发度控制及抓取间隔
  * ConcurrencyQueue：以并发单元为粒度存储待抓取链接
    * 并发单元：可以是domain或host(默认host)，相同并发单元链接存储在同一队列，控制抓取礼貌性
    * 关于锁：为了保证只有一个线程调度(入队和出队)，ConcurrencyQueue提供了全局入队锁和出队锁，只有获得锁的线程能够进行相关操作
  * ConcurrentControl：负责管理并发单元抓取连接池
    * 通过ConcurrentControl可获取并发单元当前正在抓取连接数，进而控制并发单元在同一时刻的抓取数量
    * ConcurrentControl中记录抓取连接的生命周期，连接过期将被移除(ConcurrencyQueueMonitor负责)，防止抓取异常造成并发单元连接池耗尽问题
* 数据服务层
  * 基础服务：对底层数据及业务操作的抽象和封装，涉及应用、计划、任务、规则及URL记录等
  * 事件监听
    * URLEventListener：链接抓取完成通知监听器
    * JobEventListener：爬虫任务完成通知监听器
* 调度抓取层：负责链接的调度和抓取
  * 周期型任务调度：负责为周期型计划定期生成任务，并将种子URL加入ConcurrencyQueue
  * URL链接分配器：负责定期从ConcurrencyQueue中弹出链接，发往下游爬虫进行抓取
    * 抓取溢出：URL在调度队列中滞留过长时间（默认2小时），不抓取并将状态设置为OVERFLOW
  * URL抓取：负责接收上游链接分配发送的URL，对URL进行抓取
    * 抓取路由：负责根据数据类型路由至不同的爬虫进行抓取
    * 爬虫：支持多类型数据抓取，如文本和资源等
* Web接口层：提供RESTFul形式的web接口，支持应用、计划、任务、规则及URL记录等粒度操作
* 后台监控线程：负责保障系统数据完备性和一致性
  * ConcurrencyQueueMonitor
    * 负责清理无效并发单元
    * 负责清理长期未处理链接
  * ExpiredCleaner：负责清理过期抓取数据、任务和大盘统计数据
  * ProxyMonitor：负责定期刷新短效代理

### 并发控制模型

![concurrent](https://github.com/frankcl/darwin/blob/main/images/darwin%E5%B9%B6%E5%8F%91%E6%8E%A7%E5%88%B6%E9%98%9F%E5%88%97%E6%A8%A1%E5%9E%8B.png)

并发单元：负责控制单位时间内对目标站点的抓取并发度，目标站点控制粒度支持domain和host，例如对sina.com.cn(domain)控制每秒抓取数量不超过20个
* 相同并发单元的抓取链接存储于同一队列，队列遵守FIFO原则
* 相同并发单元将抓取链接分3个优先级队列进行调度，分别为高、中、低，按照优先级进行抢占式调度，保证高优先级链接先于低优先级链接被调度

ConcurrencyControl负责抓取连接TTL管理：以并发单元为单位，管理并发单元正在抓取链接的生命周期
* 获取并发单元当前抓取连接数，可用连接数，以及总连接数
* 抓取数据时，并发单元可用连接数减一
* 抓取完成后，并发单元可用连接数加一
* ConcurrencyQueueMonitor：超过TTL的连接将被强制清理，防止异常导致并发单元连接池耗尽

### 系统模块关系流程及调用时序关系

* 系统模块关系及流程

![module_flow](https://github.com/frankcl/darwin/blob/main/images/darwin%E7%B3%BB%E7%BB%9F%E6%A8%A1%E5%9D%97%E4%BA%A4%E4%BA%92.png)

1. 计划执行：通过计划生成爬虫任务，种子URL加入ConcurrencyQueue
   1. 周期型计划：由周期型计划调度器定期调度执行，生成爬虫任务，每次调度完成后更新计划下次调度时间
   2. 手动执行计划：人工触发计划（周期型和单次型计划）执行，生成爬虫任务
2. URL调度和抓取
   1. 根据并发控制获取并发单元可用连接数，根据可用连接数从ConcurrencyQueue中获取URL
   2. 抓取URL并更新抓取结果
   3. 如果解析结果中存在子链接，将子链接添加到ConcurrencyQueue和数据库
   4. 判断爬虫任务是否结束，如结束更新爬虫任务信息

* 周期型计划调度时序

![period_time](https://github.com/frankcl/darwin/blob/main/images/darwin%E5%91%A8%E6%9C%9F%E5%9E%8B%E8%AE%A1%E5%88%92%E6%97%B6%E5%BA%8F%E5%9B%BE.png)

1. 获取ConcurrencyQueue入队锁，如果失败则放弃本轮调度，否则转第2步
2. 获取当前需要进行调度的周期型计划，每个周期型计划进行以下步骤
   1. 根据计划构建爬虫任务
   2. 将种子URL加入ConcurrencyQueue和数据库
   3. 更新周期型计划下次调度时间
3. 释放ConcurrencyQueue入队锁

* URL分配时序

![url_schedule_time](https://github.com/frankcl/darwin/blob/main/images/darwin%E6%8A%93%E5%8F%96%E9%93%BE%E6%8E%A5%E8%B0%83%E5%BA%A6%E6%97%B6%E5%BA%8F%E5%9B%BE.png)

1. 获取ConcurrencyQueue出队锁，如果失败则放弃本轮调度，否则转第2步
2. 从ConcurrencyQueue获取当前并发控制单元列表，针对每个并发控制单元进行以下处理
   1. 获取并发控制单元当前可用连接数，如果可用连接数小于等于0，则放弃并发控制单元本轮调度，否则进行下一步
   2. 根据可用连接数从ConcurrencyQueue中弹出抓取URL，针对每条URL，如果URL排队时间过长，转a），否则转b）
      1. 溢出处理：更新URL状态为OVERFLOW，发送URL完成通知，如果爬虫任务结束发送任务完成通知
      2. 正常处理：更新URL状态为抓取中，将URL信息发送到消息队列，等待下游爬虫抓取；并发单元链接TTL记录中添加抓取URL信息
3. 释放ConcurrencyQueue出队锁

* URL抓取时序

![url_fetch_time](https://github.com/frankcl/darwin/blob/main/images/darwin%E9%93%BE%E6%8E%A5%E6%8A%93%E5%8F%96%E6%97%B6%E5%BA%8F%E5%9B%BE.png)

1. 从消息队列获取待抓取URL，进行以下处理
2. 抓取URL，分析response header，根据ContentType路由至不通spider进行抓取
   1. TextSpider：负责文本数据抓取和解析，抓取结果调用用户脚本进行解析和抽链
   2. ResourceSpider：负责图片、视频、音频、PDF等资源抓取
   3. M3U8Spider：负责点播型视频流抓取
3. URL完成分发处理
   1. 将抓取结果和URL信息更新入库 
   2. 从ConcurrencyControl中移除连接信息
   3. 通过消息队列发送URL抓取结果
4. 判断URL所属爬虫任务是否结束，如果结束，执行以下步骤
   1. 更新关联爬虫任务状态为结束
   2. 通过消息队列发送结束任务信息