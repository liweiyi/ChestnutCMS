CREATE TABLE `search_word` (
  `word_id` bigint NOT NULL COMMENT '主键ID',
  `word` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '搜索词',
  `search_total` bigint NOT NULL COMMENT '历史累计搜索次数',
  `top_flag` bigint NOT NULL COMMENT '置顶标识',
  `top_date` datetime DEFAULT NULL COMMENT '置顶结束时间',
  `source` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '来源标识',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '最后修改人',
  `update_time` datetime DEFAULT NULL COMMENT '最后修改时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`word_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `search_word_hour_stat` (
  `stat_id` bigint NOT NULL AUTO_INCREMENT,
  `hour` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `word_id` bigint NOT NULL,
  `word` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `search_count` bigint NOT NULL,
  PRIMARY KEY (`stat_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
# 菜单名称路由调整
UPDATE sys_menu SET component = 'search/wordTab', path = 'searchWord' where menu_id = 2053;

ALTER TABLE cms_content ADD COLUMN prop1 VARCHAR(100);
ALTER TABLE cms_content ADD COLUMN prop2 VARCHAR(100);
ALTER TABLE cms_content ADD COLUMN prop3 VARCHAR(255);
ALTER TABLE cms_content ADD COLUMN prop4 VARCHAR(255);
