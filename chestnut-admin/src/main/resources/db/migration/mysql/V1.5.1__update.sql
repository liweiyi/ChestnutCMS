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
