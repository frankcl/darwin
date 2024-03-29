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

 Date: 29/12/2023 17:06:02
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
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for app_user
-- ----------------------------
DROP TABLE IF EXISTS `app_user`;
CREATE TABLE `app_user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `app_id` int NOT NULL,
  `user_id` varchar(32) COLLATE utf8mb4_general_ci NOT NULL,
  `user_real_name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `create_time` bigint NOT NULL,
  `update_time` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `INDEX_APP_ID` (`app_id`) USING BTREE,
  KEY `INDEX_USER_ID` (`user_id`) USING BTREE,
  KEY `INDEX_USER_REAL_NAME` (`user_real_name`) USING BTREE,
  KEY `INDEX_CREATE_TIME` (`create_time`) USING BTREE,
  KEY `INDEX_UPDATE_TIME` (`update_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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
  `rule_group` int NOT NULL,
  `create_time` bigint NOT NULL,
  `update_time` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `INDEX_NAME` (`name`) USING BTREE,
  KEY `INDEX_DOMAIN` (`domain`),
  KEY `INDEX_SCRIPT_TYPE` (`script_type`) USING BTREE,
  KEY `INDEX_RULE_GROUP` (`rule_group`) USING BTREE,
  KEY `INDEX_CREATE_TIME` (`create_time`) USING BTREE,
  KEY `INDEX_UPDATE_TIME` (`update_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=173 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for rule_group
-- ----------------------------
DROP TABLE IF EXISTS `rule_group`;
CREATE TABLE `rule_group` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `create_time` bigint NOT NULL,
  `update_time` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `INDEX_NAME` (`name`) USING BTREE,
  KEY `INDEX_CREATE_TIME` (`create_time`) USING BTREE,
  KEY `INDEX_UPDATE_TIME` (`update_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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
