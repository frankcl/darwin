/*
 Navicat Premium Data Transfer

 Source Server         : frankcl的MySQL
 Source Server Type    : MySQL
 Source Server Version : 80030 (8.0.30)
 Source Host           : localhost:3306
 Source Schema         : darwin

 Target Server Type    : MySQL
 Target Server Version : 80030 (8.0.30)
 File Encoding         : 65001

 Date: 14/03/2025 16:24:38
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for app
-- ----------------------------
DROP TABLE IF EXISTS `app`;
CREATE TABLE `app` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `create_time` bigint NOT NULL,
  `update_time` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `INDEX_NAME` (`name`) USING BTREE,
  KEY `INDEX_CREATE_TIME` (`create_time`) USING BTREE,
  KEY `INDEX_UPDATE_TIME` (`update_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of app
-- ----------------------------
BEGIN;
INSERT INTO `app` (`id`, `name`, `create_time`, `update_time`) VALUES (16, '测试应用', 1699265148581, 1699265148581);
COMMIT;

-- ----------------------------
-- Table structure for app_user
-- ----------------------------
DROP TABLE IF EXISTS `app_user`;
CREATE TABLE `app_user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `app_id` int NOT NULL,
  `user_id` varchar(32) COLLATE utf8mb4_general_ci NOT NULL,
  `nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `create_time` bigint NOT NULL,
  `update_time` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `INDEX_APP_ID` (`app_id`) USING BTREE,
  KEY `INDEX_USER_ID` (`user_id`) USING BTREE,
  KEY `INDEX_CREATE_TIME` (`create_time`) USING BTREE,
  KEY `INDEX_UPDATE_TIME` (`update_time`) USING BTREE,
  KEY `INDEX_NICK_NAME` (`nick_name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of app_user
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for executor
-- ----------------------------
DROP TABLE IF EXISTS `executor`;
CREATE TABLE `executor` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `status` int NOT NULL DEFAULT '0',
  `cause` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  `create_time` bigint NOT NULL,
  `update_time` bigint NOT NULL,
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  PRIMARY KEY (`id`),
  UNIQUE KEY `INDEX_NAME` (`name`) USING BTREE,
  KEY `INDEX_CREATE_TIME` (`create_time`) USING BTREE,
  KEY `INDEX_UPDATE_TIME` (`update_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of executor
-- ----------------------------
BEGIN;
INSERT INTO `executor` (`id`, `name`, `status`, `cause`, `create_time`, `update_time`, `comment`) VALUES (2, 'URLScheduler', 0, NULL, 1741845491522, 1741845491522, '负责从多级队列中调度URL进行抓取');
INSERT INTO `executor` (`id`, `name`, `status`, `cause`, `create_time`, `update_time`, `comment`) VALUES (3, 'PlanScheduler', 0, NULL, 1741845491522, 1741845491522, '负责调度周期性计划，生成抓取任务');
INSERT INTO `executor` (`id`, `name`, `status`, `cause`, `create_time`, `update_time`, `comment`) VALUES (4, 'ProxyRefresher', 0, NULL, 1741845491522, 1741845491522, '负责保持代理IP活性，移除过期代理IP');
INSERT INTO `executor` (`id`, `name`, `status`, `cause`, `create_time`, `update_time`, `comment`) VALUES (5, 'MultiQueueMonitor', 0, NULL, 1741845491522, 1741845491522, '负责移除多级队列和数据库中过期URL');
INSERT INTO `executor` (`id`, `name`, `status`, `cause`, `create_time`, `update_time`, `comment`) VALUES (6, 'ConcurrentConnectionMonitor', 0, NULL, 1741845491522, 1741845491522, '负责移除并发控制单元中过期URL，以缓解并发控制压力');
INSERT INTO `executor` (`id`, `name`, `status`, `cause`, `create_time`, `update_time`, `comment`) VALUES (7, 'ProxyMonitor', 0, NULL, 1741845491522, 1741845491522, '负责定期获取有效短期代理IP');
COMMIT;

-- ----------------------------
-- Table structure for job
-- ----------------------------
DROP TABLE IF EXISTS `job`;
CREATE TABLE `job` (
  `priority` int DEFAULT '1',
  `create_time` bigint NOT NULL,
  `update_time` bigint NOT NULL,
  `job_id` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `plan_id` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `rule_ids` json DEFAULT NULL,
  `seed_urls` json DEFAULT NULL,
  `status` int DEFAULT NULL,
  `avoid_repeated_fetch` tinyint DEFAULT NULL,
  `app_id` int NOT NULL,
  `fetch_method` int DEFAULT NULL,
  PRIMARY KEY (`job_id`),
  KEY `INDEX_PLAN_ID` (`plan_id`) USING BTREE,
  KEY `INDEX_PRIORITY` (`priority`) USING BTREE,
  KEY `INDEX_NAME` (`name`) USING BTREE,
  KEY `INDEX_CREATE_TIME` (`create_time`) USING BTREE,
  KEY `INDEX_UPDATE_TIME` (`update_time`) USING BTREE,
  KEY `INDEX_STATUS` (`status`) USING BTREE,
  KEY `INDEX_FETCH_METHOD` (`fetch_method`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for plan
-- ----------------------------
DROP TABLE IF EXISTS `plan`;
CREATE TABLE `plan` (
  `priority` int DEFAULT '1',
  `app_id` int NOT NULL,
  `create_time` bigint NOT NULL,
  `update_time` bigint NOT NULL,
  `app_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `plan_id` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `category` int DEFAULT NULL,
  `crontab_expression` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `rule_ids` json DEFAULT NULL,
  `seed_urls` json DEFAULT NULL,
  `status` int DEFAULT NULL,
  `next_time` bigint DEFAULT NULL,
  `avoid_repeated_fetch` tinyint DEFAULT NULL,
  `fetch_method` int DEFAULT NULL,
  PRIMARY KEY (`plan_id`),
  KEY `INDEX_APP_ID` (`app_id`) USING BTREE,
  KEY `INDEX_PRIORITY` (`priority`) USING BTREE,
  KEY `INDEX_CREATE_TIME` (`create_time`) USING BTREE,
  KEY `INDEX_UPDATE_TIME` (`update_time`) USING BTREE,
  KEY `INDEX_CATEGORY` (`category`) USING BTREE,
  KEY `INDEX_NAME` (`name`) USING BTREE,
  KEY `INDEX_STATUS` (`status`) USING BTREE,
  KEY `INDEX_FETCH_METHOD` (`fetch_method`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for proxy
-- ----------------------------
DROP TABLE IF EXISTS `proxy`;
CREATE TABLE `proxy` (
  `id` int NOT NULL AUTO_INCREMENT,
  `address` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `port` int NOT NULL,
  `create_time` bigint NOT NULL,
  `update_time` bigint NOT NULL,
  `category` int NOT NULL,
  `username` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `password` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `expired_time` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `INDEX_ADDRESS` (`address`) USING BTREE,
  KEY `INDEX_PORT` (`port`) USING BTREE,
  KEY `INDEX_CATEGORY` (`category`) USING BTREE,
  KEY `INDEX_EXPIRED_TIME` (`expired_time`) USING BTREE,
  KEY `INDEX_CREATE_TIME` (`create_time`) USING BTREE,
  KEY `INDEX_UPDATE_TIME` (`update_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of proxy
-- ----------------------------
BEGIN;
INSERT INTO `proxy` (`id`, `address`, `port`, `create_time`, `update_time`, `category`, `username`, `password`, `expired_time`) VALUES (2, '121.224.73.47', 888, 1702373019892, 1702373019892, 1, 'cj430n', 'cj430n', NULL);
INSERT INTO `proxy` (`id`, `address`, `port`, `create_time`, `update_time`, `category`, `username`, `password`, `expired_time`) VALUES (3, '121.224.78.14', 888, 1702373089892, 1702373089892, 1, 'cj430n', 'cj430n', NULL);
INSERT INTO `proxy` (`id`, `address`, `port`, `create_time`, `update_time`, `category`, `username`, `password`, `expired_time`) VALUES (11, '121.224.77.195', 888, 1704943994000, 1704943994000, 1, 'cj430n', 'cj430n', NULL);
INSERT INTO `proxy` (`id`, `address`, `port`, `create_time`, `update_time`, `category`, `username`, `password`, `expired_time`) VALUES (12, '121.224.5.139', 888, 1704943994000, 1704943994000, 1, 'cj430n', 'cj430n', NULL);
INSERT INTO `proxy` (`id`, `address`, `port`, `create_time`, `update_time`, `category`, `username`, `password`, `expired_time`) VALUES (13, '121.224.75.204', 888, 1704943994000, 1704943994000, 1, 'cj430n', 'cj430n', NULL);
INSERT INTO `proxy` (`id`, `address`, `port`, `create_time`, `update_time`, `category`, `username`, `password`, `expired_time`) VALUES (14, '121.224.7.239', 888, 1704943994000, 1704943994000, 1, 'cj430n', 'cj430n', NULL);
INSERT INTO `proxy` (`id`, `address`, `port`, `create_time`, `update_time`, `category`, `username`, `password`, `expired_time`) VALUES (15, '121.224.4.124', 888, 1704943994000, 1704943994000, 1, 'cj430n', 'cj430n', NULL);
INSERT INTO `proxy` (`id`, `address`, `port`, `create_time`, `update_time`, `category`, `username`, `password`, `expired_time`) VALUES (16, '121.224.6.120', 888, 1704943994000, 1704943994000, 1, 'cj430n', 'cj430n', NULL);
INSERT INTO `proxy` (`id`, `address`, `port`, `create_time`, `update_time`, `category`, `username`, `password`, `expired_time`) VALUES (17, '121.224.4.17', 888, 1704943994000, 1704943994000, 1, 'cj430n', 'cj430n', NULL);
INSERT INTO `proxy` (`id`, `address`, `port`, `create_time`, `update_time`, `category`, `username`, `password`, `expired_time`) VALUES (18, '121.224.7.162', 888, 1704943994000, 1704943994000, 1, 'cj430n', 'cj430n', NULL);
COMMIT;

-- ----------------------------
-- Table structure for rule
-- ----------------------------
DROP TABLE IF EXISTS `rule`;
CREATE TABLE `rule` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `domain` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `regex` text COLLATE utf8mb4_general_ci,
  `script` mediumtext COLLATE utf8mb4_general_ci,
  `script_type` int DEFAULT NULL,
  `create_time` bigint NOT NULL,
  `update_time` bigint NOT NULL,
  `app_id` int NOT NULL,
  `plan_id` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `INDEX_NAME` (`name`) USING BTREE,
  KEY `INDEX_DOMAIN` (`domain`),
  KEY `INDEX_SCRIPT_TYPE` (`script_type`) USING BTREE,
  KEY `INDEX_CREATE_TIME` (`create_time`) USING BTREE,
  KEY `INDEX_UPDATE_TIME` (`update_time`) USING BTREE,
  KEY `INDEX_APP_ID` (`app_id`) USING BTREE,
  KEY `INDEX_PLAN_ID` (`plan_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=220 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of rule
-- ----------------------------
BEGIN;
INSERT INTO `rule` (`id`, `name`, `domain`, `regex`, `script`, `script_type`, `create_time`, `update_time`, `app_id`, `plan_id`) VALUES (139, '人民网结构化规则', 'people.com.cn', '^http(s)?://\\w+.people.com.cn/n1/\\w+/\\w+/\\w+-\\w+.html$', 'import org.apache.commons.lang3.StringUtils;\nimport org.jsoup.Jsoup;\nimport org.jsoup.nodes.Document;\nimport org.jsoup.nodes.Element;\nimport org.jsoup.select.Elements;\nimport xin.manong.darwin.parser.sdk.HTMLParser;\nimport xin.manong.darwin.parser.sdk.ParseRequest;\nimport xin.manong.darwin.parser.sdk.ParseResponse;\n\nimport java.text.SimpleDateFormat;\nimport java.util.HashMap;\nimport java.util.Map;\n\npublic class GroovyParser extends HTMLParser {\n\n    @Override\n    public ParseResponse parse(ParseRequest request) {\n        Map<String, Object> fieldMap = new HashMap<>();\n        Document document = Jsoup.parse(request.html, StringUtils.isEmpty(request.redirectURL) ?\n                request.url : request.redirectURL);\n        String title = request.userDefinedMap == null ? null : (String) request.userDefinedMap.get(\"title\");\n        if (StringUtils.isEmpty(title)) title = parseTitle(document);\n        if (!StringUtils.isEmpty(title)) fieldMap.put(\"title\", title);\n        Long publishTime = parsePublishTime(document);\n        if (publishTime != null) fieldMap.put(\"publish_timestamp\", publishTime);\n        String source = parseSource(document);\n        if (!StringUtils.isEmpty(source)) fieldMap.put(\"source\", source);\n        Elements mainElements = document.selectXpath(\"//*[@class=\\\"rm_txt_con cf\\\"]\");\n        if (!mainElements.isEmpty()) {\n            String mainHTML = mainElements.html();\n            String mainText = mainElements.text();\n            if (!StringUtils.isEmpty(mainHTML)) fieldMap.put(\"html\", mainHTML);\n            if (!StringUtils.isEmpty(mainText)) fieldMap.put(\"content_text\", mainText);\n        }\n        return ParseResponse.buildOK(fieldMap, null, request.userDefinedMap);\n    }\n\n    /**\n     * 解析标题\n     *\n     * @param document HTML文档\n     * @return 成功返回标题，否则返回null\n     */\n    private String parseTitle(Element document) {\n        Elements elements = document.selectXpath(\"//div[@class=\\\"col col-1 fl\\\"]/h1\");\n        if (elements.isEmpty()) return null;\n        return elements.get(0).text().trim();\n    }\n\n    /**\n     * 解析发布时间，毫秒时间戳\n     *\n     * @param document HTML文档\n     * @return 成功返回发布时间，否则返回null\n     */\n    private Long parsePublishTime(Element document) {\n        Elements elements = document.selectXpath(\"//div[@class=\\\"col-1-1 fl\\\"]\");\n        if (elements.isEmpty()) return null;\n        String text = elements.get(0).text().trim();\n        if (text.contains(\"|\")) text = text.split(\"\\\\|\")[0];\n        if (StringUtils.isEmpty(text)) return null;\n        try {\n            SimpleDateFormat format = new SimpleDateFormat(\"yyyy年MM月dd日HH:mm\");\n            return format.parse(text).getTime();\n        } catch (Exception e) {\n            return null;\n        }\n    }\n\n    /**\n     * 解析来源\n     *\n     * @param document HTML文档\n     * @return 成功返回来源，否则返回null\n     */\n    private String parseSource(Element document) {\n        Elements elements = document.selectXpath(\"//div[@class=\\\"col-1-1 fl\\\"]/a\");\n        if (elements.isEmpty()) return null;\n        return elements.get(0).text().trim();\n    }\n}\n', 1, 1700041063662, 1700041063662, 16, '370803c276408930aa66e4133c1da9bc');
INSERT INTO `rule` (`id`, `name`, `domain`, `regex`, `script`, `script_type`, `create_time`, `update_time`, `app_id`, `plan_id`) VALUES (140, '人民网抽链规则', 'people.com.cn', '^http(s)?://\\w+.people.com.cn/GB/\\d+/index\\.html$', 'import org.apache.commons.lang3.StringUtils;\nimport org.jsoup.Jsoup;\nimport org.jsoup.nodes.Document;\nimport org.jsoup.nodes.Element;\nimport org.jsoup.select.Elements;\nimport xin.manong.darwin.common.model.URLRecord;\nimport xin.manong.darwin.parser.sdk.HTMLParser;\nimport xin.manong.darwin.parser.sdk.ParseRequest;\nimport xin.manong.darwin.parser.sdk.ParseResponse;\n\nimport java.util.ArrayList;\nimport java.util.List;\n\npublic class GroovyParser extends HTMLParser {\n\n    @Override\n    public ParseResponse parse(ParseRequest request) {\n        Document document = Jsoup.parse(request.html, StringUtils.isEmpty(request.redirectURL) ?\n                request.url : request.redirectURL);\n        Elements elements = document.selectXpath(\"//div[@class=\\\"ej_list_box clear\\\"]/ul/li\");\n        List<URLRecord> childURLs = new ArrayList<>();\n        for (Element element : elements) {\n            URLRecord childURL = parseChildURL(element);\n            if (childURL == null) continue;\n            childURLs.add(childURL);\n        }\n        return ParseResponse.buildOK(null, childURLs, request.userDefinedMap);\n    }\n\n    /**\n     * 解析抽链结果\n     *\n     * @param element 链接元素\n     * @return 抽链结果\n     */\n    private URLRecord parseChildURL(Element element) {\n        Elements elements = element.select(\"a\");\n        if (elements.isEmpty()) return null;\n        Element e = elements.get(0);\n        if (!e.hasAttr(\"href\")) return null;\n        URLRecord childURL = new URLRecord(e.absUrl(\"href\"));\n        String title = e.text().trim();\n        if (!StringUtils.isEmpty(title)) childURL.userDefinedMap.put(\"title\", title);\n        return childURL;\n    }\n}\n', 1, 1700041477942, 1700041477942, 16, '370803c276408930aa66e4133c1da9bc');
COMMIT;

-- ----------------------------
-- Table structure for rule_history
-- ----------------------------
DROP TABLE IF EXISTS `rule_history`;
CREATE TABLE `rule_history` (
  `id` int NOT NULL AUTO_INCREMENT,
  `rule_id` int NOT NULL,
  `script` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  `script_type` int DEFAULT NULL,
  `regex` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  `create_time` bigint NOT NULL,
  `update_time` bigint NOT NULL,
  `domain` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `INDEX_CREATE_TIME` (`create_time`) USING BTREE,
  KEY `INDEX_UPDATE_TIME` (`update_time`) USING BTREE,
  KEY `INDEX_RULE_ID` (`rule_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of rule_history
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for url
-- ----------------------------
DROP TABLE IF EXISTS `url`;
CREATE TABLE `url` (
  `key` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `job_id` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `url` text COLLATE utf8mb4_general_ci NOT NULL,
  `timeout` int DEFAULT NULL,
  `priority` int DEFAULT '1',
  `create_time` bigint NOT NULL,
  `update_time` bigint NOT NULL,
  `in_queue_time` bigint DEFAULT NULL,
  `out_queue_time` bigint DEFAULT NULL,
  `category` int DEFAULT NULL,
  `depth` int DEFAULT NULL,
  `concurrent_level` int DEFAULT NULL,
  `headers` json DEFAULT NULL,
  `fetch_time` bigint DEFAULT NULL,
  `parent_url` text COLLATE utf8mb4_general_ci,
  `fetch_content_url` text COLLATE utf8mb4_general_ci,
  `status` int NOT NULL,
  `user_defined_map` json DEFAULT NULL,
  `field_map` json DEFAULT NULL,
  `hash` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `fetch_method` int DEFAULT NULL,
  `app_id` int NOT NULL,
  `redirect_url` text COLLATE utf8mb4_general_ci,
  `mime_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `sub_mime_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `plan_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `scope` int DEFAULT NULL,
  `http_code` int DEFAULT NULL,
  PRIMARY KEY (`key`),
  KEY `INDEX_JOB_ID` (`job_id`) USING BTREE,
  KEY `INDEX_HASH` (`hash`),
  KEY `INDEX_PRIORITY` (`priority`) USING BTREE,
  KEY `INDEX_CREATE_TIME` (`create_time`) USING BTREE,
  KEY `INDEX_UPDATE_TIME` (`update_time`) USING BTREE,
  KEY `INDEX_CATEGORY` (`category`) USING BTREE,
  KEY `INDEX_STATUS` (`status`) USING BTREE,
  KEY `INDEX_PLAN_ID` (`plan_id`) USING BTREE,
  KEY `INDEX_HTTP_CODE` (`http_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

SET FOREIGN_KEY_CHECKS = 1;
