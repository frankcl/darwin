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

### 并发控制模型

![concurrent](https://github.com/frankcl/darwin/blob/main/images/darwin%E5%B9%B6%E5%8F%91%E6%8E%A7%E5%88%B6%E9%98%9F%E5%88%97%E6%A8%A1%E5%9E%8B.png)

### 调用时序关系

* 周期型计划调度时序

![period_time](https://github.com/frankcl/darwin/blob/main/images/darwin%E5%91%A8%E6%9C%9F%E5%9E%8B%E8%AE%A1%E5%88%92%E6%97%B6%E5%BA%8F%E5%9B%BE.png)

* URL调度时序

![url_schedule_time](https://github.com/frankcl/darwin/blob/main/images/darwin%E6%8A%93%E5%8F%96%E9%93%BE%E6%8E%A5%E8%B0%83%E5%BA%A6%E6%97%B6%E5%BA%8F%E5%9B%BE.png)

* URL抓取时序

![url_fetch_time](https://github.com/frankcl/darwin/blob/main/images/darwin%E9%93%BE%E6%8E%A5%E6%8A%93%E5%8F%96%E6%97%B6%E5%BA%8F%E5%9B%BE.png)

