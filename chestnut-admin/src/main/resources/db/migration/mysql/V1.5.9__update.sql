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
