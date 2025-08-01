ALTER TABLE b_cms_content ADD COLUMN `images` varchar(500);
UPDATE b_cms_content SET images = CASE WHEN logo IS NOT NULL AND logo != '' THEN CONCAT('["', logo, '"]') ELSE '[]' END;
ALTER TABLE b_cms_content DROP COLUMN logo;

ALTER TABLE b_cms_article_detail DROP COLUMN content_json;
ALTER TABLE b_cms_article_detail ADD COLUMN `format` varchar(10);
UPDATE b_cms_article_detail SET format = 'RichText';

ALTER TABLE cms_content_op_log ADD COLUMN `catalog_id` bigint;
ALTER TABLE cms_content_op_log ADD COLUMN `catalog_ancestors` varchar(500);

ALTER TABLE cms_exd_default MODIFY COLUMN data_id varchar(100);
ALTER TABLE cms_cfd_default MODIFY COLUMN data_id varchar(100);
ALTER TABLE cms_resource MODIFY COLUMN status varchar(1);


-- 以下SQL为统一兼容各数据库的字段名称和类型
ALTER TABLE sys_notice MODIFY COLUMN notice_content longtext;
ALTER TABLE cms_template MODIFY COLUMN content longtext;
ALTER TABLE sys_scheduled_task_log CHANGE COLUMN percent task_percent int;
ALTER TABLE cms_ad_hour_stat CHANGE COLUMN view view_total bigint;