ALTER TABLE cms_content ADD COLUMN seo_title varchar(255) DEFAULT '' COMMENT 'SEO标题';
ALTER TABLE cms_content ADD COLUMN seo_keywords varchar(255) DEFAULT '' COMMENT 'SEO关键词';
ALTER TABLE cms_content ADD COLUMN seo_description varchar(255) DEFAULT '' COMMENT 'SEO描述';
