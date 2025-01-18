UPDATE cms_content SET `link_flag` = 'N' WHERE link_flag is null;
ALTER TABLE cms_content MODIFY COLUMN `link_flag` varchar(1) DEFAULT 'N';
UPDATE cms_content SET `is_lock` = 'N' WHERE is_lock is null;
ALTER TABLE cms_content MODIFY COLUMN `is_lock` varchar(1) DEFAULT 'N';

ALTER TABLE cms_video MODIFY COLUMN `path` varchar(1000);
ALTER TABLE b_cms_video MODIFY COLUMN `path` varchar(1000);

ALTER TABLE cms_content ADD COLUMN `images` varchar(500);
UPDATE cms_content SET images = CASE WHEN logo IS NOT NULL AND logo != '' THEN CONCAT('["', logo, '"]') ELSE '[]' END;
ALTER TABLE cms_content DROP COLUMN logo;

ALTER TABLE cms_article_detail DROP COLUMN content_json;
ALTER TABLE cms_article_detail ADD COLUMN `format` varchar(10);
UPDATE cms_article_detail SET format = 'RichText';

CREATE TABLE `cms_content_op_log` (
  `log_id` bigint NOT NULL COMMENT 'ID',
  `site_id` bigint NOT NULL COMMENT '站点ID',
  `content_id` bigint NOT NULL COMMENT '内容ID',
  `type` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '操作方式',
  `details` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '操作明细',
  `operator_type` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '操作人类型',
  `operator` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '操作人',
  `log_time` datetime NOT NULL COMMENT '日志时间',
  PRIMARY KEY (`log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

/* __CC_IGNORE__ */
CREATE TABLE `cc_check_latest_version_log` (
  `log_id` bigint NOT NULL COMMENT 'ID',
  `from` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '来源',
  `referer` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Referer',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`log_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

CREATE TABLE `cc_certificate` (
  `cert_id` bigint NOT NULL COMMENT 'ID',
  `issuer` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '签发者类型',
  `domain` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '域名',
  `acme_account_id` bigint DEFAULT NULL COMMENT '关联ACME账号ID',
  `issue_time` bigint DEFAULT NULL COMMENT '证书签发时间',
  `expire_time` bigint DEFAULT NULL COMMENT '证书过期时间',
  `key_path` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '证书Key路径',
  `crt_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '证书路径',
  `crt_country` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '证书信息：国家',
  `crt_state` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '证书信息：省',
  `crt_locality` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '证书信息：地区',
  `crt_organization` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '证书信息：组织机构',
  `status` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '状态',
  `config_props` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '配置参数',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '最后修改人',
  `update_time` datetime DEFAULT NULL COMMENT '最后修改时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`cert_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='域名证书表';

CREATE TABLE `cc_acme_account` (
  `account_id` bigint NOT NULL,
  `issuer` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `key_pair_path` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `location_url` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `domain_num` int NOT NULL,
  `contact` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '最后修改人',
  `update_time` datetime DEFAULT NULL COMMENT '最后修改时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='ACME账号表';
INSERT INTO `sys_menu` VALUES (2082, '域名证书', 3, 5, 'deploy/certificate', 'deploy/certificate/index', '', 'N', 'Y', 'C', 'Y', '0', 'deploy:cert:view', 'skill', 'admin', '2025-01-14 00:00:00', '', NULL, '');
INSERT INTO `sys_i18n_dict` VALUES (632978182307909, 'zh-CN', 'MENU.NAME.2082', '证书管理');
INSERT INTO `sys_i18n_dict` VALUES (633640198516805, 'en', 'MENU.NAME.2082', 'Domain Cert');
INSERT INTO `sys_i18n_dict` VALUES (633640198524997, 'zh-TW', 'MENU.NAME.2082', '證書管理');
/* __CC_IGNORE_END__ */