ALTER TABLE sys_security_config ADD COLUMN captcha_enable varchar(1) DEFAULT 'N' COMMENT '是否启用验证码';
ALTER TABLE sys_security_config ADD COLUMN captcha_type varchar(50) DEFAULT 'normal' COMMENT '验证码类型';
ALTER TABLE sys_security_config ADD COLUMN captcha_expires int DEFAULT 600 COMMENT '验证码过期时间，单位：秒';
ALTER TABLE sys_security_config ADD COLUMN captcha_duration int DEFAULT 0 COMMENT '验证码刷新间隔，单位：秒';
ALTER TABLE sys_security_config ADD COLUMN login_type_config_ids varchar(255) COMMENT '第三方登录配置ID';

ALTER TABLE cms_audio ADD COLUMN cover varchar(255) COMMENT '音频封面图';

CREATE TABLE sys_login_config (
    config_id bigint NOT NULL COMMENT 'ID',
    type varchar(50) NOT NULL COMMENT '类型',
    config_name varchar(100) NOT NULL COMMENT '配置名称',
    config_desc varchar(100) DEFAULT NULL COMMENT '配置描述',
    status varchar(1) NOT NULL COMMENT '状态',
    config_props varchar(2000) NOT NULL COMMENT '配置详情',
    create_by varchar(64) NOT NULL DEFAULT '' COMMENT '创建者',
    create_time datetime NOT NULL COMMENT '创建时间',
    update_by varchar(64) DEFAULT '' COMMENT '更新者',
    update_time datetime DEFAULT NULL COMMENT '更新时间',
    remark varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (config_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE sys_user_binding (
    binding_id bigint NOT NULL COMMENT 'ID',
    user_id bigint NOT NULL COMMENT '系统用户ID',
    binding_type varchar(50) NOT NULL COMMENT '类型',
    open_id varchar(255) NOT NULL COMMENT '第三方用户唯一标识',
    union_id varchar(255) DEFAULT NULL COMMENT '第三方同开放平台多应用唯一标识',
    access_token varchar(255) DEFAULT NULL,
    refresh_token varchar(255) DEFAULT NULL,
    token_expire_time bigint DEFAULT NULL COMMENT 'token过期时间',
    create_by varchar(64) NOT NULL DEFAULT '' COMMENT '创建者',
    create_time datetime NOT NULL COMMENT '创建时间',
    update_by varchar(64) DEFAULT '' COMMENT '更新者',
    update_time datetime DEFAULT NULL COMMENT '更新时间',
    remark varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (binding_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE cc_cloud_config (
                                 config_id bigint NOT NULL COMMENT 'ID',
                                 type varchar(50)  NOT NULL COMMENT '类型',
                                 config_name varchar(100)  NOT NULL COMMENT '配置名称',
                                 config_desc varchar(100)  DEFAULT NULL COMMENT '配置描述',
                                 status varchar(1)  NOT NULL COMMENT '状态',
                                 config_props varchar(2000)  NOT NULL COMMENT '配置详情',
                                 create_by varchar(64)  NOT NULL DEFAULT '' COMMENT '创建者',
                                 create_time datetime NOT NULL COMMENT '创建时间',
                                 update_by varchar(64)  DEFAULT '' COMMENT '更新者',
                                 update_time datetime DEFAULT NULL COMMENT '更新时间',
                                 remark varchar(500)  DEFAULT NULL COMMENT '备注',
                                 PRIMARY KEY (config_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- __CC_IGNORE__
CREATE TABLE cms_file (
    content_id bigint NOT NULL COMMENT 'ID',
    site_id bigint NOT NULL COMMENT '站点ID',
    path varchar(255) DEFAULT NULL COMMENT '文件路径',
    type varchar(20) DEFAULT NULL COMMENT '文件类型',
    file_size bigint DEFAULT NULL COMMENT '文件大小',
    intro longtext COMMENT '文件介绍',
    content longtext COMMENT '文件内容',
    PRIMARY KEY (content_id) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE b_cms_file (
    content_id bigint NOT NULL COMMENT '关联内容ID',
    site_id bigint NOT NULL COMMENT '站点ID',
    path varchar(255) DEFAULT NULL COMMENT '文件路径',
    type varchar(20) DEFAULT NULL COMMENT '文件类型',
    file_size bigint DEFAULT NULL COMMENT '文件大小',
    intro longtext '文件介绍',
    content longtext '文件内容',
    backup_id bigint NOT NULL COMMENT '备份ID',
    backup_by varchar(50) NOT NULL COMMENT '备份操作人',
    backup_time datetime DEFAULT NULL COMMENT '备份时间',
    backup_remark varchar(100) DEFAULT NULL COMMENT '备份备注',
    PRIMARY KEY (backup_id) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE cms_article_version (
    version_id bigint NOT NULL COMMENT 'ID',
    content_id bigint NOT NULL COMMENT '关联内容ID',
    body_text longtext NOT NULL COMMENT '版本文章详情',
    create_by varchar(50) NOT NULL COMMENT '创建人',
    create_time datetime NOT NULL COMMENT '创建时间',
    update_by varchar(50) DEFAULT NULL COMMENT '最后修改人',
    update_time datetime DEFAULT NULL COMMENT '最后修改时间',
    remark varchar(255) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (version_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE cc_message_config (
     config_id bigint NOT NULL COMMENT 'ID',
     type varchar(50) NOT NULL COMMENT '类型',
     name varchar(100) NOT NULL COMMENT '名称',
     config_props varchar(2000) NOT NULL COMMENT '配置信息',
     create_by varchar(64) NOT NULL DEFAULT '' COMMENT '创建者',
     create_time datetime NOT NULL COMMENT '创建时间',
     update_by varchar(64) DEFAULT '' COMMENT '更新者',
     update_time datetime DEFAULT NULL COMMENT '更新时间',
     remark varchar(500) DEFAULT NULL COMMENT '备注',
     PRIMARY KEY (config_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE cc_message_event (
    event_id bigint NOT NULL COMMENT 'ID',
    name varchar(100) NOT NULL COMMENT '事件名称',
    status varchar(1) NOT NULL COMMENT '状态',
    notifies varchar(2000) NOT NULL COMMENT '通知配置',
    trigger_total bigint NOT NULL COMMENT '触发次数',
    create_by varchar(64) NOT NULL DEFAULT '' COMMENT '创建者',
    create_time datetime NOT NULL COMMENT '创建时间',
    update_by varchar(64) DEFAULT '' COMMENT '更新者',
    update_time datetime DEFAULT NULL COMMENT '更新时间',
    remark varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE cc_message_template (
   template_id bigint NOT NULL COMMENT 'ID',
   type varchar(50) NOT NULL COMMENT '类型',
   name varchar(100) NOT NULL COMMENT '名称',
   title varchar(255) DEFAULT NULL COMMENT '标题模板',
   content longtext NOT NULL COMMENT '内容模板',
   create_by varchar(64) NOT NULL DEFAULT '' COMMENT '创建者',
   create_time datetime NOT NULL COMMENT '创建时间',
   update_by varchar(64) DEFAULT '' COMMENT '更新者',
   update_time datetime DEFAULT NULL COMMENT '更新时间',
   remark varchar(500) DEFAULT NULL COMMENT '备注',
   PRIMARY KEY (template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE cc_message_send_log (
   log_id bigint NOT NULL COMMENT '日志ID',
   config_id bigint NOT NULL COMMENT '消息配置ID',
   status varchar(1) NOT NULL COMMENT '发送状态',
   result_message varchar(500) DEFAULT NULL COMMENT '结果信息',
   receiver varchar(100) DEFAULT NULL COMMENT '消息接收人',
   send_time bigint NOT NULL COMMENT '发送时间',
   PRIMARY KEY (log_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO sys_menu VALUES (742635012284485, '消息配置', 3, 10, 'message/config', 'message/config/index', '', 'N', 'Y', 'C', 'Y', '0', 'message:config:view', 'email', 'admin', '2025-12-01 00:00:00', '', NULL, '');
INSERT INTO sys_menu VALUES (751443708399685, '消息模板', 3, 11, 'message/template', 'message/template/index', '', 'N', 'Y', 'C', 'Y', '0', 'message:template:view', 'code', 'admin', '2025-12-01 00:00:00', '', NULL, '');
INSERT INTO sys_menu VALUES (752212121038917, '消息事件', 3, 12, 'message/event', 'message/event/index', '', 'N', 'Y', 'C', 'Y', '0', 'message:event:view', 'guide', 'admin', '2025-12-01 00:00:00', '', NULL, '');
INSERT INTO sys_i18n_dict VALUES (742635012350021, 'zh-CN', 'MENU.NAME.742635012284485', '消息配置');
INSERT INTO sys_i18n_dict VALUES (751443469946949, 'en', 'MENU.NAME.742635012284485', 'Message Config');
INSERT INTO sys_i18n_dict VALUES (751443469951045, 'zh-TW', 'MENU.NAME.742635012284485', '消息配置');
INSERT INTO sys_i18n_dict VALUES (751443708424261, 'zh-CN', 'MENU.NAME.751443708399685', '消息配置');
INSERT INTO sys_i18n_dict VALUES (753275796111429, 'en', 'MENU.NAME.751443708399685', 'Message Template');
INSERT INTO sys_i18n_dict VALUES (753275796127813, 'zh-TW', 'MENU.NAME.751443708399685', '消息模板');
INSERT INTO sys_i18n_dict VALUES (752212121124933, 'zh-CN', 'MENU.NAME.752212121038917', '消息事件');
INSERT INTO sys_i18n_dict VALUES (753275891617861, 'en', 'MENU.NAME.752212121038917', 'Message Event');
INSERT INTO sys_i18n_dict VALUES (753275891630149, 'zh-TW', 'MENU.NAME.752212121038917', '消息事件');
-- __CC_IGNORE_END__