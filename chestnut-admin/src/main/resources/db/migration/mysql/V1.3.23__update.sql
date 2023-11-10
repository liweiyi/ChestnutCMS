CREATE TABLE `cms_content_rela` (
  `rela_id` bigint NOT NULL COMMENT 'ID',
  `content_id` bigint NOT NULL COMMENT '内容ID',
  `rela_content_id` bigint NOT NULL COMMENT '关联内容ID',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`rela_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;