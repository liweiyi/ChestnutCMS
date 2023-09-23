-- ----------------------------
-- Table structure for sys_scheduled_task
-- ----------------------------
DROP TABLE IF EXISTS `sys_scheduled_task`;
CREATE TABLE `sys_scheduled_task` (
  `task_id` bigint NOT NULL,
  `task_type` varchar(50) NOT NULL,
  `status` varchar(1) NOT NULL,
  `task_trigger` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `trigger_args` varchar(255) NOT NULL,
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`task_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_scheduled_task_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_scheduled_task_log`;
CREATE TABLE `sys_scheduled_task_log` (
  `log_id` bigint NOT NULL,
  `task_id` bigint NOT NULL,
  `task_type` varchar(50) NOT NULL,
  `ready_time` datetime NOT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `interrupt_time` datetime DEFAULT NULL,
  `percent` int DEFAULT NULL,
  `result` varchar(1) NOT NULL,
  `message` varchar(2000) DEFAULT NULL,
  `log_time` datetime NOT NULL,
  PRIMARY KEY (`log_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;


INSERT INTO `sys_menu` VALUES (2081, '定时任务', 2, 2, 'task', 'monitor/task/index', NULL, 'N', 'Y', 'C', 'Y', '0', NULL, 'job', 'admin', '2023-05-06 19:11:08', '', NULL, '');

INSERT INTO `sys_i18n_dict` VALUES (239, 'zh-CN', 'MENU.NAME.2081', '定时任务');
INSERT INTO `sys_i18n_dict` VALUES (240, 'en', 'MENU.NAME.2081', 'Scheduled Task');

