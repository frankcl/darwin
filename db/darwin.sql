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

 Date: 18/05/2025 13:28:24
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for app
-- ----------------------------
DROP TABLE IF EXISTS `app`;
CREATE TABLE `app` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `create_time` bigint NOT NULL,
  `update_time` bigint NOT NULL,
  `creator` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  `modifier` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `INDEX_NAME` (`name`) USING BTREE,
  KEY `INDEX_CREATE_TIME` (`create_time`) USING BTREE,
  KEY `INDEX_UPDATE_TIME` (`update_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for job
-- ----------------------------
DROP TABLE IF EXISTS `job`;
CREATE TABLE `job` (
  `priority` int DEFAULT '1',
  `create_time` bigint NOT NULL,
  `update_time` bigint NOT NULL,
  `job_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `plan_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `status` tinyint DEFAULT '1',
  `allow_repeat` tinyint DEFAULT NULL,
  `app_id` int NOT NULL,
  `fetch_method` int DEFAULT NULL,
  `executor` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
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
-- Table structure for message
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
  `id` int NOT NULL AUTO_INCREMENT,
  `source_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `exception` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  `create_time` bigint NOT NULL,
  `update_time` bigint NOT NULL,
  `source_type` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `INDEX_CREATE_TIME` (`create_time`) USING BTREE,
  KEY `INDEX_UPDATE_TIME` (`update_time`) USING BTREE,
  KEY `INDEX_SOURCE_KEY` (`source_key`) USING BTREE,
  KEY `INDEX_SOURCE_TYPE` (`source_type`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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
  `plan_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `category` int DEFAULT NULL,
  `crontab_expression` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `status` tinyint DEFAULT '0',
  `next_time` bigint DEFAULT NULL,
  `allow_repeat` tinyint DEFAULT NULL,
  `fetch_method` int DEFAULT NULL,
  `creator` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `modifier` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
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
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for rule
-- ----------------------------
DROP TABLE IF EXISTS `rule`;
CREATE TABLE `rule` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `regex` text COLLATE utf8mb4_general_ci,
  `script` mediumtext COLLATE utf8mb4_general_ci,
  `script_type` int DEFAULT NULL,
  `create_time` bigint NOT NULL,
  `update_time` bigint NOT NULL,
  `plan_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `creator` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `modifier` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `change_log` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  `version` int unsigned DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `INDEX_NAME` (`name`) USING BTREE,
  KEY `INDEX_SCRIPT_TYPE` (`script_type`) USING BTREE,
  KEY `INDEX_CREATE_TIME` (`create_time`) USING BTREE,
  KEY `INDEX_UPDATE_TIME` (`update_time`) USING BTREE,
  KEY `INDEX_PLAN_ID` (`plan_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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
  `modifier` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `change_log` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  `version` int unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `INDEX_CREATE_TIME` (`create_time`) USING BTREE,
  KEY `INDEX_UPDATE_TIME` (`update_time`) USING BTREE,
  KEY `INDEX_RULE_ID` (`rule_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for seed
-- ----------------------------
DROP TABLE IF EXISTS `seed`;
CREATE TABLE `seed` (
  `key` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `timeout` int DEFAULT NULL,
  `priority` int DEFAULT '1',
  `create_time` bigint NOT NULL,
  `update_time` bigint NOT NULL,
  `headers` json DEFAULT NULL,
  `custom_map` json DEFAULT NULL,
  `fetch_method` int DEFAULT NULL,
  `plan_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `link_scope` int DEFAULT NULL,
  `hash` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `allow_dispatch` tinyint DEFAULT '1',
  `host` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `domain` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `normalize` tinyint DEFAULT NULL,
  `http_request` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `request_body` json DEFAULT NULL,
  `request_hash` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `post_media_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`key`),
  KEY `INDEX_HASH` (`hash`) USING BTREE,
  KEY `INDEX_HTTP_REQUEST` (`http_request`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for trend
-- ----------------------------
DROP TABLE IF EXISTS `trend`;
CREATE TABLE `trend` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `key` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `category` int NOT NULL,
  `values` json DEFAULT NULL,
  `create_time` bigint NOT NULL,
  `update_time` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `INDEX_CATEGORY` (`category`) USING BTREE,
  KEY `INDEX_KEY` (`key`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for url
-- ----------------------------
DROP TABLE IF EXISTS `url`;
CREATE TABLE `url` (
  `key` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `job_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `url` text COLLATE utf8mb4_general_ci NOT NULL,
  `timeout` int DEFAULT NULL,
  `priority` int DEFAULT '1',
  `create_time` bigint NOT NULL,
  `update_time` bigint NOT NULL,
  `push_time` bigint DEFAULT NULL,
  `pop_time` bigint DEFAULT NULL,
  `content_type` int DEFAULT NULL,
  `depth` int DEFAULT NULL,
  `concurrency_level` int DEFAULT NULL,
  `headers` json DEFAULT NULL,
  `fetch_time` bigint DEFAULT NULL,
  `parent_url` text COLLATE utf8mb4_general_ci,
  `fetch_content_url` text COLLATE utf8mb4_general_ci,
  `status` int NOT NULL,
  `custom_map` json DEFAULT NULL,
  `field_map` json DEFAULT NULL,
  `hash` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `fetch_method` int DEFAULT NULL,
  `app_id` int NOT NULL,
  `redirect_url` text COLLATE utf8mb4_general_ci,
  `plan_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `link_scope` int DEFAULT NULL,
  `http_code` int DEFAULT NULL,
  `content_length` bigint DEFAULT NULL,
  `allow_dispatch` tinyint DEFAULT '1',
  `concurrency_unit` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `host` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `domain` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `fetched` tinyint DEFAULT NULL,
  `media_type` json DEFAULT NULL,
  `charset` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `allow_repeat` tinyint DEFAULT NULL,
  `html_charset` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `normalize` tinyint DEFAULT NULL,
  `down_time` bigint DEFAULT NULL,
  `request_body` json DEFAULT NULL,
  `http_request` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `request_hash` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `post_media_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`key`),
  KEY `INDEX_JOB_ID` (`job_id`) USING BTREE,
  KEY `INDEX_HASH` (`hash`),
  KEY `INDEX_PRIORITY` (`priority`) USING BTREE,
  KEY `INDEX_CREATE_TIME` (`create_time`) USING BTREE,
  KEY `INDEX_UPDATE_TIME` (`update_time`) USING BTREE,
  KEY `INDEX_STATUS` (`status`) USING BTREE,
  KEY `INDEX_PLAN_ID` (`plan_id`) USING BTREE,
  KEY `INDEX_HTTP_CODE` (`http_code`) USING BTREE,
  KEY `INDEX_FETCH_TIME` (`fetch_time`) USING BTREE,
  KEY `INDEX_FETCH_METHOD` (`fetch_method`) USING BTREE,
  KEY `INDEX_APP_ID` (`app_id`) USING BTREE,
  KEY `INDEX_LINK_SCOPE` (`link_scope`) USING BTREE,
  KEY `INDEX_CONTENT_LENGTH` (`content_length`) USING BTREE,
  KEY `INDEX_CONCURRENCY_UNIT` (`concurrency_unit`) USING BTREE,
  KEY `INDEX_HOST` (`host`) USING BTREE,
  KEY `INDEX_DOMAIN` (`domain`) USING BTREE,
  KEY `INDEX_FETCHED` (`fetched`) USING BTREE,
  KEY `INDEX_DOWN_TIME` (`down_time`) USING BTREE,
  KEY `INDEX_CONTENT_TYPE` (`content_type`) USING BTREE,
  KEY `INDEX_HTTP_REQUEST` (`http_request`),
  KEY `INDEX_REQUEST_HASH` (`request_hash`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

SET FOREIGN_KEY_CHECKS = 1;
