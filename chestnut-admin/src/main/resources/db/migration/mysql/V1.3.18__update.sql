ALTER TABLE cms_exd_default DROP PRIMARY KEY, ADD PRIMARY KEY(data_id, data_type, model_id);

ALTER TABLE cc_comment CHANGE COLUMN deleted del_flag int;

ALTER TABLE cc_member CHANGE COLUMN phonenumber phone_number varchar(20);

ALTER TABLE sys_user CHANGE COLUMN phonenumber phone_number varchar(20);

ALTER TABLE x_model_field ADD COLUMN sort_flag bigint DEFAULT '0' COMMENT '排序字段';

CREATE TABLE `cms_catalog_content_stat` (
  `catalog_id` bigint NOT NULL COMMENT '栏目ID',
  `site_id` bigint NOT NULL COMMENT '站点ID',
  `draft_total` int NOT NULL DEFAULT '0' COMMENT '初稿内容数',
  `to_publish_total` int NOT NULL DEFAULT '0' COMMENT '待发布内容数',
  `published_total` int NOT NULL DEFAULT '0' COMMENT '已发布内容数',
  `offline_total` int NOT NULL DEFAULT '0' COMMENT '已下线内容数',
  `editing_total` int NOT NULL DEFAULT '0' COMMENT '重新编辑内容数',
  PRIMARY KEY (`catalog_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

CREATE TABLE `cms_user_content_stat` (
  `id` varchar(100) NOT NULL COMMENT 'ID',
  `site_id` bigint NOT NULL COMMENT '站点ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `user_name` varchar(100) NOT NULL COMMENT '用户名',
  `draft_total` int NOT NULL DEFAULT '0' COMMENT '初稿内容数',
  `to_publish_total` int NOT NULL DEFAULT '0' COMMENT '待发布内容数',
  `published_total` int NOT NULL DEFAULT '0' COMMENT '已发布内容数',
  `offline_total` int NOT NULL DEFAULT '0' COMMENT '已下线内容数',
  `editing_total` int NOT NULL DEFAULT '0' COMMENT '重新编辑内容数',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;