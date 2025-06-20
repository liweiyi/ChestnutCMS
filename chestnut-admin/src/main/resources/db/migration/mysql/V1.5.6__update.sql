ALTER TABLE cms_custom_form MODIFY COLUMN `templates` varchar(1000);
ALTER TABLE cms_page_widget MODIFY COLUMN `publish_pipe_code` varchar(50);
ALTER TABLE cms_page_widget ADD COLUMN `templates` varchar(1000) COMMENT '模板配置';

