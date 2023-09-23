ALTER TABLE cms_content ADD COLUMN contributor_id bigint default 0;

ALTER TABLE cc_member ADD COLUMN slogan varchar(255);
ALTER TABLE cc_member ADD COLUMN description varchar(500);
ALTER TABLE cc_member ADD COLUMN cover varchar(100);

DROP TABLE cms_member_favorites;
DROP TABLE cms_member_like;

CREATE TABLE `cc_member_favorites` (
  `log_id` bigint NOT NULL,
  `member_id` bigint NOT NULL COMMENT '会员ID',
  `data_type` varchar(100) NOT NULL COMMENT '收藏数据类型',
  `data_id` bigint NOT NULL COMMENT '收藏数据ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`log_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT = Dynamic;

CREATE TABLE `cc_member_like` (
  `log_id` bigint NOT NULL,
  `member_id` bigint NOT NULL COMMENT '会员ID',
  `data_type` varchar(100) NOT NULL COMMENT '点赞数据类型',
  `data_id` bigint NOT NULL COMMENT '点赞数据ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`log_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT = Dynamic;

CREATE TABLE `cc_member_follow` (
  `log_id` bigint NOT NULL,
  `member_id` bigint NOT NULL COMMENT '会员ID',
  `follow_member_id` bigint NOT NULL COMMENT '关注的会员ID',
  PRIMARY KEY (`log_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

CREATE TABLE `cc_member_stat_data` (
  `member_id` bigint NOT NULL COMMENT '会员ID',
  `int_value1` int NOT NULL DEFAULT '0',
  `int_value2` int NOT NULL DEFAULT '0',
  `int_value3` int NOT NULL DEFAULT '0',
  `int_value4` int NOT NULL DEFAULT '0',
  `int_value5` int NOT NULL DEFAULT '0',
  `int_value6` int NOT NULL DEFAULT '0',
  `int_value7` int NOT NULL DEFAULT '0',
  `int_value8` int NOT NULL DEFAULT '0',
  `int_value9` int NOT NULL DEFAULT '0',
  `int_value10` int NOT NULL DEFAULT '0',
  `int_value11` int NOT NULL DEFAULT '0',
  `int_value12` int NOT NULL DEFAULT '0',
  `int_value13` int NOT NULL DEFAULT '0',
  `int_value14` int NOT NULL DEFAULT '0',
  `int_value15` int NOT NULL DEFAULT '0',
  `int_value16` int NOT NULL DEFAULT '0',
  `int_value17` int NOT NULL DEFAULT '0',
  `int_value18` int NOT NULL DEFAULT '0',
  `int_value19` int NOT NULL DEFAULT '0',
  `int_value20` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`member_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;