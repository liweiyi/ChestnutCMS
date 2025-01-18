ALTER TABLE cms_content DROP COLUMN deleted;
ALTER TABLE cms_article_detail DROP COLUMN deleted;
ALTER TABLE cms_image DROP COLUMN deleted;
ALTER TABLE cms_audio DROP COLUMN deleted;
ALTER TABLE cms_video DROP COLUMN deleted;

CREATE TABLE `b_cms_content` (
  `content_id` bigint NOT NULL COMMENT '主键ID',
  `site_id` bigint NOT NULL COMMENT '所属站点ID',
  `catalog_id` bigint NOT NULL COMMENT '所属栏目ID',
  `catalog_ancestors` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '所属栏目祖级IDs',
  `top_catalog` bigint NOT NULL COMMENT '所属顶级栏目ID',
  `dept_id` bigint DEFAULT NULL COMMENT '部门ID',
  `dept_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '部门编码',
  `content_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '内容类型',
  `title` varchar(360) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标题',
  `sub_title` varchar(180) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '副标题',
  `short_title` varchar(180) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '短标题',
  `title_style` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '标题样式',
  `logo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '引导图',
  `source` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '来源',
  `source_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '来源URL',
  `original` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '0' COMMENT '是否原创（Y=是，N=否）',
  `author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '作者',
  `editor` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '编辑',
  `summary` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '摘要',
  `static_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '自定义内容静态化文件路径',
  `status` char(3) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '0' COMMENT '内容状态',
  `attributes` int DEFAULT '0' COMMENT '内容属性',
  `top_flag` bigint NOT NULL DEFAULT '0' COMMENT '置顶标识',
  `top_date` datetime DEFAULT NULL COMMENT '置顶时间',
  `sort_flag` bigint NOT NULL COMMENT '排序字段',
  `keywords` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '关键词',
  `tags` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'TAGs',
  `copy_type` tinyint NOT NULL DEFAULT '0' COMMENT '复制类型',
  `copy_id` bigint DEFAULT '0' COMMENT '复制源ID',
  `publish_date` datetime DEFAULT NULL COMMENT '发布时间',
  `offline_date` datetime DEFAULT NULL COMMENT '下线时间',
  `publish_pipe` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '发布通道',
  `publish_pipe_props` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '发布通道属性，配置独立模板用',
  `link_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '0' COMMENT '是否链接内容（Y=是，N=否）',
  `redirect_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '跳转链接',
  `is_lock` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '0' COMMENT '是否已锁定（Y=是，N=否）',
  `lock_user` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '锁定用户名',
  `config_props` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '扩展属性',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '最后修改人',
  `update_time` datetime DEFAULT NULL COMMENT '最后修改时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `like_count` bigint DEFAULT '0',
  `comment_count` bigint DEFAULT '0',
  `favorite_count` bigint DEFAULT '0',
  `view_count` bigint DEFAULT '0',
  `contributor_id` bigint DEFAULT '0',
  `seo_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT 'SEO标题',
  `seo_keywords` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT 'SEO关键词',
  `seo_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT 'SEO描述',
  `prop1` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `prop2` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `prop3` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `prop4` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `backup_id` bigint NOT NULL,
  `backup_by` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `backup_time` datetime NOT NULL,
  `backup_remark` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`backup_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

CREATE TABLE `b_cms_article_detail` (
  `content_id` bigint NOT NULL COMMENT 'ID',
  `site_id` bigint NOT NULL,
  `content_html` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文章正文（html格式）',
  `content_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '文章正文（json格式）',
  `page_titles` varchar(1500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '分页标题',
  `download_remote_image` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '0' COMMENT '是否下载远程图片',
  `backup_id` bigint NOT NULL,
  `backup_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `backup_time` datetime DEFAULT NULL,
  `backup_remark` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`backup_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

CREATE TABLE `b_cms_image` (
  `image_id` bigint NOT NULL COMMENT '主键ID',
  `site_id` bigint DEFAULT NULL,
  `content_id` bigint NOT NULL COMMENT '所属内容ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '图片标题',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '图片摘要',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '图片原文件名',
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '图片路径',
  `image_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '图片类型',
  `file_size` bigint NOT NULL COMMENT '图片文件大小',
  `width` int NOT NULL DEFAULT '0' COMMENT '图片宽度',
  `height` int NOT NULL DEFAULT '0' COMMENT '图片高度',
  `redirect_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '跳转链接',
  `hit_count` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '点击量',
  `sort_flag` bigint NOT NULL COMMENT '排序字段',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '最近修改人',
  `update_time` datetime DEFAULT NULL COMMENT '最近修改时间',
  `remark` varchar(400) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `backup_id` bigint NOT NULL,
  `backup_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `backup_time` datetime DEFAULT NULL,
  `backup_remark` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`backup_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

CREATE TABLE `b_cms_audio` (
  `audio_id` bigint NOT NULL COMMENT 'ID',
  `content_id` bigint NOT NULL COMMENT '所属内容ID',
  `site_id` bigint NOT NULL COMMENT '所属站点ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '音频标题',
  `author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '作者',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '简介',
  `type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '音频文件路径',
  `file_size` bigint DEFAULT NULL,
  `format` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `duration` bigint DEFAULT NULL,
  `decoder` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '编码方式',
  `channels` int DEFAULT NULL,
  `bit_rate` int DEFAULT NULL,
  `sampling_rate` int DEFAULT NULL,
  `sort_flag` int NOT NULL COMMENT '排序字段',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '最后修改人',
  `update_time` datetime DEFAULT NULL COMMENT '最后修改时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `backup_id` bigint NOT NULL,
  `backup_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `backup_time` datetime DEFAULT NULL,
  `backup_remark` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`backup_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

CREATE TABLE `b_cms_video` (
  `video_id` bigint NOT NULL,
  `content_id` bigint NOT NULL COMMENT '所属内容ID',
  `site_id` bigint NOT NULL COMMENT '所属站点ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '视频标题',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '简介',
  `type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '视频文件路径',
  `file_size` bigint DEFAULT NULL,
  `format` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `duration` bigint DEFAULT NULL,
  `decoder` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '视频编码方式',
  `width` int DEFAULT NULL,
  `height` int DEFAULT NULL,
  `bit_rate` int DEFAULT NULL,
  `frame_rate` int DEFAULT NULL,
  `sort_flag` int NOT NULL COMMENT '排序字段',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '最后修改人',
  `update_time` datetime DEFAULT NULL COMMENT '最后修改时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `backup_id` bigint NOT NULL,
  `backup_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `backup_time` datetime DEFAULT NULL,
  `backup_remark` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`backup_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

-- __CC_IGNORE__
CREATE TABLE `cms_book` (
  `content_id` bigint NOT NULL COMMENT 'ID',
  `site_id` bigint NOT NULL COMMENT '所属站点ID',
  `isbn` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '国际标准书号',
  `original_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '原作名',
  `intro` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '图书介绍',
  `author_id` bigint DEFAULT NULL COMMENT '关联作者内容ID',
  `publisher` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '出版社',
  `producer` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '出品方',
  `publication_date` datetime DEFAULT NULL COMMENT '出版时间',
  `translators` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '译者',
  `number_of_pages` int DEFAULT NULL COMMENT '页数',
  `number_of_words` int DEFAULT NULL COMMENT '字数',
  `price` bigint DEFAULT NULL COMMENT '价格',
  `completed` varchar(1) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否完结',
  PRIMARY KEY (`content_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `b_cms_book` (
  `content_id` bigint NOT NULL COMMENT 'ID',
  `site_id` bigint NOT NULL COMMENT '所属站点ID',
  `isbn` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '国际标准书号',
  `original_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '原作名',
  `intro` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '图书介绍',
  `author_id` bigint DEFAULT NULL COMMENT '关联作者内容ID',
  `publisher` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '出版社',
  `producer` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '出品方',
  `publication_date` datetime DEFAULT NULL COMMENT '出版时间',
  `translators` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '译者',
  `number_of_pages` int DEFAULT NULL COMMENT '页数',
  `number_of_words` int DEFAULT NULL COMMENT '字数',
  `price` bigint DEFAULT NULL COMMENT '价格',
  `completed` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否完结',
  `backup_id` bigint NOT NULL,
  `backup_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `backup_time` datetime DEFAULT NULL,
  `backup_remark` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`backup_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `cms_book_chapter` (
  `chapter_id` bigint NOT NULL COMMENT 'ID',
  `site_id` bigint NOT NULL COMMENT '所属站点ID',
  `content_id` bigint NOT NULL COMMENT '所属内容ID',
  `title` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT '章节标题',
  `content` varchar(2000) COLLATE utf8mb4_general_ci NOT NULL COMMENT '章节内容',
  `number_of_words` int NOT NULL COMMENT '字数',
  `publish_date` datetime DEFAULT NULL COMMENT '发布时间',
  `sort_flag` bigint NOT NULL COMMENT '排序字段',
  `status` char(3) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '状态',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '最后修改人',
  `update_time` datetime DEFAULT NULL COMMENT '最后修改时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`chapter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `b_cms_book_chapter` (
  `chapter_id` bigint NOT NULL COMMENT 'ID',
  `site_id` bigint NOT NULL COMMENT '所属站点ID',
  `content_id` bigint NOT NULL COMMENT '所属内容ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '章节标题',
  `content` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '章节内容',
  `number_of_words` int NOT NULL COMMENT '字数',
  `publish_date` datetime DEFAULT NULL COMMENT '发布时间',
  `sort_flag` bigint NOT NULL COMMENT '排序字段',
  `status` char(3) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '状态',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '最后修改人',
  `update_time` datetime DEFAULT NULL COMMENT '最后修改时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `backup_id` bigint NOT NULL,
  `backup_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `backup_time` datetime DEFAULT NULL,
  `backup_remark` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`backup_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
-- __CC_IGNORE_END__

ALTER TABLE cms_catalog MODIFY COLUMN `static_flag` varchar(1);
ALTER TABLE cms_catalog MODIFY COLUMN `visible_flag` varchar(1);
ALTER TABLE cms_catalog MODIFY COLUMN `status` varchar(1);
ALTER TABLE cms_content MODIFY COLUMN `original` varchar(1);
ALTER TABLE cms_content MODIFY COLUMN `status` varchar(3);
ALTER TABLE cms_content MODIFY COLUMN `link_flag` varchar(1);
ALTER TABLE cms_content MODIFY COLUMN `is_lock` varchar(1);
ALTER TABLE cms_publishpipe MODIFY COLUMN `state` varchar(1);
ALTER TABLE gen_table MODIFY COLUMN `gen_type` varchar(1);
ALTER TABLE gen_table_column MODIFY COLUMN `is_pk` varchar(1);
ALTER TABLE gen_table_column MODIFY COLUMN `is_increment` varchar(1);
ALTER TABLE gen_table_column MODIFY COLUMN `is_required` varchar(1);
ALTER TABLE gen_table_column MODIFY COLUMN `is_insert` varchar(1);
ALTER TABLE gen_table_column MODIFY COLUMN `is_edit` varchar(1);
ALTER TABLE gen_table_column MODIFY COLUMN `is_list` varchar(1);
ALTER TABLE gen_table_column MODIFY COLUMN `is_query` varchar(1);
ALTER TABLE sys_config MODIFY COLUMN `config_type` varchar(1);
ALTER TABLE sys_dict_data MODIFY COLUMN `is_default` varchar(1);
ALTER TABLE sys_logininfor MODIFY COLUMN `status` varchar(1);
ALTER TABLE sys_menu MODIFY COLUMN `is_frame` varchar(1);
ALTER TABLE sys_menu MODIFY COLUMN `is_cache` varchar(1);
ALTER TABLE sys_menu MODIFY COLUMN `menu_type` varchar(1);
ALTER TABLE sys_menu MODIFY COLUMN `visible` varchar(1);
ALTER TABLE sys_menu MODIFY COLUMN `status` varchar(1);
ALTER TABLE sys_notice MODIFY COLUMN `notice_type` varchar(1);
ALTER TABLE sys_notice MODIFY COLUMN `status` varchar(1);
ALTER TABLE sys_post MODIFY COLUMN `status` varchar(1);
ALTER TABLE sys_role MODIFY COLUMN `status` varchar(1);
ALTER TABLE sys_security_config MODIFY COLUMN `status` varchar(1);
ALTER TABLE sys_security_config MODIFY COLUMN `force_modify_pwd_after_add` varchar(1);
ALTER TABLE sys_security_config MODIFY COLUMN `force_modify_pwd_after_reset` varchar(1);
ALTER TABLE sys_storage_config MODIFY COLUMN `status` varchar(1);
ALTER TABLE sys_storage_config MODIFY COLUMN `pipeline` varchar(1);
ALTER TABLE sys_storage_config MODIFY COLUMN `domain` varchar(1);
ALTER TABLE sys_user MODIFY COLUMN `sex` varchar(1);
ALTER TABLE sys_user MODIFY COLUMN `status` varchar(1);
ALTER TABLE sys_user MODIFY COLUMN `force_modify_password` varchar(1);

ALTER TABLE cms_catalog add column tag_ignore varchar(1);
UPDATE cms_catalog SET tag_ignore = 'N';