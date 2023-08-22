# darwin
爬虫系统：达尔文树皮蜘蛛，能结出世界上最大最牢固的网

## 特点

* 支持自定义脚本，针对特定站点定制抽链和结构化逻辑
* 支持域名/站点粒度抓取调度：具备礼貌性控制，管理站点/域名粒度单位时间并发度
* 支持文本（HTML、JSON等）、资源（图片和视频）、流媒体（M3U8流）等数据抓取
* 多层次爬虫数据管理：应用->计划->任务->抓取记录

## 模块

### 1. darwin-common 公共代码
- [x] 数据模型定义
- [x] 并发单元计算器
- [x] 公共工具定义

### 2. darwin-queue 并发控制及多级队列
- [x] 并发控制：管理并发单元（站点/域名）当前抓取连接生命周期
- [x] 多级队列：并发单元抓取调度，保证优先级、并发度及平滑度

### 3. darwin-parser 抓取内容解析
- [x] 脚本解析引擎框架：支持Groovy和JavaScript
- [x] 脚本解析服务：使用用户定义脚本对抓取内容进行解析

### 4. darwin-service 服务定义和实现
- [x] 应用服务
- [x] 计划服务
- [x] 任务服务
- [x] 规则及规则分组服务
- [x] 抓取记录服务

### 5. darwin-spider 内容爬取
- [x] 网页抓取：HTML、JSON及其他文本抓取
- [x] 资源抓取：图片、视频及PDF等资源抓取
- [x] 直播流抓取：M3U8点播资源抓取

### 6. darwin-scheduler 抓取任务调度
- [x] 周期性任务调度
- [x] URL队列调度

### 7. darwin-web 爬虫RESTFul服务
- [x] 应用服务接口
- [x] 计划服务接口
- [x] 任务服务接口
- [x] 规则及规则分组服务接口
- [x] 抓取记录服务接口

### 8. darwin-monitor 监控
- [x] 并发连接资源监控：清理过期并发连接
- [x] URL多级队列监控：清理过期任务及过期并发单元

## 架构原理

### 基本概念

* 应用：业务归属单位，所有爬虫计划、任务及链接归属于应用
* 计划：爬虫抓取计划，计划是任务的元数据定义，计划负责生成任务，一个计划归属于一个应用，包含一组规则及一系列种子URL，计划分2类
  * 一次性计划：计划根据种子列表和规则列表生成一次性爬虫任务，可重复填充种子重新生成爬虫任务
  * 周期性计划：计划根据调度周期对种子列表和规则列表反复生成爬虫任务，种子列表可复用
* 任务：数据抓取调度及归属单元，一个任务归属于计划及应用，包含一组规则及一系列种子URL，任务具备3级调度优先级：高、中、低，框架负责调度任务，抓取任务URL
* 规则：规则负责页面解析工作，包含匹配URL的正则表达式及解析页面的脚本，脚本支持Groovy和JavaScript，一个规则属于一个规则分组
* 规则分组：具备相同业务属性的规则归属同一分组
* URL记录：定义抓取URL、抓取结果及解析结果，一个URL属于一个任务及一个应用
* 并发单元：控制单位时间内的抓取并发数量，支持domain和host级别，框架通过并发单元控制对目标站点的抓取礼貌性

### darwin架构

![architecture](https://github.com/frankcl/darwin/blob/main/images/darwin%E7%B3%BB%E7%BB%9F%E6%9E%B6%E6%9E%84.png)

* 数据存储层：负责基础数据存储，例如应用、计划、任务、规则、规则分组及URL记录等
  * URL记录：元数据存储于MySQL/OTS，下载结果存储于OSS
  * 任务及URL记录：由于数据量大，支持存储类型：MySQL和OTS
* 并发管理层：负责抓取链接调度及站点抓取并发度控制
  * MultiQueue：负责抓取链接调度的多级队列，分2个粒度存储链接
    * 并发单元：以并发单元粒度调度链接抓取，控制对站点抓取礼貌性
    * 任务粒度：监控管理任务的抓取链接，感知任务是否结束
  * ConcurrentManager：负责并发单元当前抓取连接生命周期管理，获取并发单元当前抓取链接数及详情
  * 监控：针对MultiQueue和ConcurrentManager中过期数据进行监控，保证无效连接和并发单元的及时清理
* 数据服务层：对底层数据及业务操作的抽象和封装，涉及应用、计划、任务、规则、规则分组及URL记录等
* 调度抓取层：负责链接的调度和抓取
  * 周期性任务调度：负责为周期性计划定期生成任务，并将种子URL加入MultiQueue
  * URL链接调度：负责周期性地从MultiQueue中弹出链接，发往下游爬虫进行抓取
  * URL抓取：负责接收上游链接调度发送的URL，对URL进行抓取，支持3类资源抓取
    * 文本抓取：抓取HTML/JSON文本资源，并利用规则进行结构化或抽链，并将抽链结果加入MultiQueue进行调度
    * 资源抓取：抓取图片、视频资源
    * 流抓取：抓取M3U8流媒体资源
* Web接口层：提供RESTFul形式的web接口，支持应用、计划、任务、规则、规则分组及URL记录等粒度操作

### 并发控制模型

![concurrent](https://github.com/frankcl/darwin/blob/main/images/darwin%E5%B9%B6%E5%8F%91%E6%8E%A7%E5%88%B6%E9%98%9F%E5%88%97%E6%A8%A1%E5%9E%8B.png)

并发控制模型以2个维度进行：并发单元和爬虫任务

* 并发单元：负责控制单位时间内对目标站点的抓取并发度，目标站点控制粒度支持domain和host，例如对sina.com.cn这个domain控制每秒抓取数量不超过20个
  * 相同并发单元的抓取链接存储于同一队列，队列遵守FIFO原则
  * 相同并发单元将抓取链接分3个优先级队列进行存储，优先级分别为高、中、低，按照优先级进行抢占式调度，保证高优先级链接先于低优先级链接被调度
  * 抓取链接TTL：以并发单元为单位，管理相同并发单元正在抓取链接的生命周期
    * 可从TTL管理获取并发单元当前抓取链接数，正在抓取的链接加入TTL管理，抓取完成的链接从TTL管理中删除
    * TTL记录抓取链接的抓取生命周期，超过TTL的链接将被强制清理，防止异常导致的无效链接残留
* 爬虫任务：负责以任务为单位记录抓取链接信息
  * 进入调度的链接在其对应任务的队列中记录信息，抓取完成的链接在其对应任务的队列中删除信息
  * 保证先将抽链结果加入任务队列，再从任务队列中删除父链信息，通过任务队列是否为空判断爬虫任务是否结束

### 调用时序关系

* 周期性计划调度时序

![period_time](https://github.com/frankcl/darwin/blob/main/images/darwin%E5%91%A8%E6%9C%9F%E5%9E%8B%E8%AE%A1%E5%88%92%E6%97%B6%E5%BA%8F%E5%9B%BE.png)

1. 获取MultiQueue入队锁，如果失败则放弃本轮调度，否则转第2步
2. 获取当前需要进行调度的周期性计划，针对每个周期性计划进行以下步骤
   1. 根据计划构建任务，将任务添加入库
   2. 将任务种子URL加入MultiQueue
   3. 将任务种子URL添加入库
   4. 更新周期性计划下次调度时间
3. 释放MultiQueue入队锁

* URL调度时序

![url_schedule_time](https://github.com/frankcl/darwin/blob/main/images/darwin%E6%8A%93%E5%8F%96%E9%93%BE%E6%8E%A5%E8%B0%83%E5%BA%A6%E6%97%B6%E5%BA%8F%E5%9B%BE.png)

1. 获取MultiQueue出队锁，如果失败则放弃本轮调度，否则转第2步
2. 从MultiQueue获取当前并发控制单元列表，针对每个并发控制单元进行以下处理
   1. 获取并发控制单元当前可用连接数，如果可用连接数小于等于0，则放弃并发控制单元本轮调度，否则进行下一步
   2. 根据可用连接数从MultiQueue中弹出抓取URL，并更新URL状态为抓取中
   3. 将URL信息发送到消息队列，等待下游爬虫抓取
   4. 并发单元链接TTL记录中添加抓取URL信息
3. 释放MultiQueue出队锁

* URL抓取时序

![url_fetch_time](https://github.com/frankcl/darwin/blob/main/images/darwin%E9%93%BE%E6%8E%A5%E6%8A%93%E5%8F%96%E6%97%B6%E5%BA%8F%E5%9B%BE.png)

