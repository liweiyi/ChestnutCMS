ALTER TABLE cms_content ADD COLUMN like_count bigint default 0;
ALTER TABLE cms_content ADD COLUMN comment_count bigint default 0;
ALTER TABLE cms_content ADD COLUMN favorite_count bigint default 0;
ALTER TABLE cms_content ADD COLUMN view_count bigint default 0;

CREATE TABLE `cms_member_favorites` (
  `log_id` bigint NOT NULL,
  `site_id` bigint NOT NULL COMMENT '站点ID',
  `content_id` bigint NOT NULL COMMENT '内容ID',
  `member_id` bigint NOT NULL COMMENT '会员ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`log_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

CREATE TABLE `cms_member_like` (
  `log_id` bigint NOT NULL,
  `site_id` bigint NOT NULL COMMENT '站点ID',
  `content_id` bigint NOT NULL COMMENT '内容ID',
  `member_id` bigint NOT NULL COMMENT '会员ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`log_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;
