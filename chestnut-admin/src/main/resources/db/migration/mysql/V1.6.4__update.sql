-- 会员订阅基础服务不创建非主键唯一约束和外键约束。
-- V1.6.4：会员订阅基础服务。

-- 会员订阅基础服务。
-- 基础能力仅管理订阅定义、服务项、会员结束时间及调整事实，不依赖交易。
-- 订阅结束时间及审计前后结束时间使用UTC纪元秒，创建及操作时间使用UTC纪元毫秒。
CREATE TABLE cc_member_subscribe_service (
    service_id varchar(100) NOT NULL COMMENT '服务项ID，区分大小写，创建后不可修改',
    name varchar(100) NOT NULL COMMENT '服务项名称',
    create_by varchar(64) NOT NULL COMMENT '创建人',
    update_by varchar(64) NOT NULL COMMENT '更新人',
    create_time datetime(3) NOT NULL COMMENT '创建时间',
    update_time datetime(3) NOT NULL COMMENT '更新时间',
    remark varchar(400) NULL DEFAULT '' COMMENT '备注',
    PRIMARY KEY (service_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='会员订阅服务项';

CREATE TABLE cc_member_subscribe_definition (
    definition_id varchar(64) NOT NULL COMMENT '订阅定义标识',
    subscription_name varchar(128) NOT NULL COMMENT '订阅名称',
    description varchar(1000) NOT NULL DEFAULT '' COMMENT '订阅说明',
    service_ids json NOT NULL COMMENT '已创建的订阅服务项ID数组，包含系统内置项和自定义项',
    status char(1) NOT NULL DEFAULT '0' COMMENT '状态：0启用、1禁用',
    create_by varchar(64) NOT NULL COMMENT '创建人',
    update_by varchar(64) NOT NULL COMMENT '更新人',
    create_time datetime(3) NOT NULL COMMENT '创建时间',
    update_time datetime(3) NOT NULL COMMENT '更新时间',
    remark varchar(500) NULL DEFAULT '' COMMENT '备注',
    PRIMARY KEY (definition_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='会员订阅定义';

CREATE TABLE cc_member_subscribe (
    record_id varchar(64) NOT NULL COMMENT '会员订阅记录标识',
    member_id bigint NOT NULL COMMENT '会员标识',
    subscription_id varchar(64) NOT NULL COMMENT '订阅定义标识',
    expire_time bigint NOT NULL COMMENT '结束时间，UTC纪元秒，允许调整到过去',
    create_time bigint NOT NULL COMMENT '创建时间，UTC纪元毫秒',
    PRIMARY KEY (record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='会员订阅结束时间';

CREATE TABLE cc_member_subscribe_log (
    log_id varchar(64) NOT NULL COMMENT '调整日志标识',
    record_id varchar(64) NOT NULL COMMENT '会员订阅记录标识',
    member_id bigint NOT NULL COMMENT '会员标识',
    subscription_id varchar(64) NOT NULL COMMENT '订阅定义标识',
    delta_seconds bigint NOT NULL COMMENT '带符号的调整秒数，允许负数及零',
    before_expire_time bigint NOT NULL COMMENT '调整前结束时间，UTC纪元秒，首次关联为0',
    after_expire_time bigint NOT NULL COMMENT '调整后结束时间，UTC纪元秒',
    operation_time bigint NOT NULL COMMENT '操作时间，UTC纪元毫秒',
    operator_type varchar(32) NOT NULL COMMENT '可信操作人类型',
    operator_id varchar(64) NOT NULL COMMENT '可信操作人标识',
    source_type varchar(64) NOT NULL COMMENT '调整来源类型',
    source_id varchar(128) NOT NULL COMMENT '来源业务单据标识，与来源类型共同保证幂等',
    reason varchar(500) NOT NULL COMMENT '调整原因',
    PRIMARY KEY (log_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='只追加的会员订阅时长调整审计';

-- 订阅配置菜单及服务项操作权限。
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES ('MemberSubscriptionConfig', '订阅配置', 'MemberManager', 11, 'subscriptionConfig', 'member/subscription/config/index', NULL, 'N', 'Y', 'C', 'Y', '0', 'member:subscription:config:view', 'list', 'admin', '2026-10-27 00:00:00', '', NULL, '');
INSERT INTO sys_i18n_dict (dict_id, lang_tag, lang_key, lang_value) VALUES ('zh-CN@MENU.NAME.MemberSubscriptionConfig', 'zh-CN', 'MENU.NAME.MemberSubscriptionConfig', '订阅配置');
INSERT INTO sys_i18n_dict (dict_id, lang_tag, lang_key, lang_value) VALUES ('en@MENU.NAME.MemberSubscriptionConfig', 'en', 'MENU.NAME.MemberSubscriptionConfig', 'Subscription Settings');
INSERT INTO sys_i18n_dict (dict_id, lang_tag, lang_key, lang_value) VALUES ('zh-TW@MENU.NAME.MemberSubscriptionConfig', 'zh-TW', 'MENU.NAME.MemberSubscriptionConfig', '訂閱配置');

-- 会员订阅基础菜单与权限。

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES ('MemberSubscriptionDefinitionAdd', '新增订阅定义', 'MemberSubscriptionConfig', 1, '', NULL, NULL, 'N', 'Y', 'F', 'Y', '0', 'member:subscription:definition:add', '#', 'admin', '2026-10-21 00:00:00', '', NULL, '');
INSERT INTO sys_i18n_dict (dict_id, lang_tag, lang_key, lang_value) VALUES ('zh-CN@MENU.NAME.MemberSubscriptionDefinitionAdd', 'zh-CN', 'MENU.NAME.MemberSubscriptionDefinitionAdd', '新增订阅定义');
INSERT INTO sys_i18n_dict (dict_id, lang_tag, lang_key, lang_value) VALUES ('en@MENU.NAME.MemberSubscriptionDefinitionAdd', 'en', 'MENU.NAME.MemberSubscriptionDefinitionAdd', 'Create Subscription Definition');
INSERT INTO sys_i18n_dict (dict_id, lang_tag, lang_key, lang_value) VALUES ('zh-TW@MENU.NAME.MemberSubscriptionDefinitionAdd', 'zh-TW', 'MENU.NAME.MemberSubscriptionDefinitionAdd', '新增訂閱定義');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES ('MemberSubscriptionDefinitionEdit', '编辑订阅定义', 'MemberSubscriptionConfig', 2, '', NULL, NULL, 'N', 'Y', 'F', 'Y', '0', 'member:subscription:definition:edit', '#', 'admin', '2026-10-21 00:00:00', '', NULL, '');
INSERT INTO sys_i18n_dict (dict_id, lang_tag, lang_key, lang_value) VALUES ('zh-CN@MENU.NAME.MemberSubscriptionDefinitionEdit', 'zh-CN', 'MENU.NAME.MemberSubscriptionDefinitionEdit', '编辑订阅定义');
INSERT INTO sys_i18n_dict (dict_id, lang_tag, lang_key, lang_value) VALUES ('en@MENU.NAME.MemberSubscriptionDefinitionEdit', 'en', 'MENU.NAME.MemberSubscriptionDefinitionEdit', 'Edit Subscription Definition');
INSERT INTO sys_i18n_dict (dict_id, lang_tag, lang_key, lang_value) VALUES ('zh-TW@MENU.NAME.MemberSubscriptionDefinitionEdit', 'zh-TW', 'MENU.NAME.MemberSubscriptionDefinitionEdit', '編輯訂閱定義');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES ('MemberSubscriptionDefinitionRemove', '删除未使用定义', 'MemberSubscriptionConfig', 3, '', NULL, NULL, 'N', 'Y', 'F', 'Y', '0', 'member:subscription:definition:remove', '#', 'admin', '2026-10-21 00:00:00', '', NULL, '');
INSERT INTO sys_i18n_dict (dict_id, lang_tag, lang_key, lang_value) VALUES ('zh-CN@MENU.NAME.MemberSubscriptionDefinitionRemove', 'zh-CN', 'MENU.NAME.MemberSubscriptionDefinitionRemove', '删除未使用定义');
INSERT INTO sys_i18n_dict (dict_id, lang_tag, lang_key, lang_value) VALUES ('en@MENU.NAME.MemberSubscriptionDefinitionRemove', 'en', 'MENU.NAME.MemberSubscriptionDefinitionRemove', 'Delete Unused Definition');
INSERT INTO sys_i18n_dict (dict_id, lang_tag, lang_key, lang_value) VALUES ('zh-TW@MENU.NAME.MemberSubscriptionDefinitionRemove', 'zh-TW', 'MENU.NAME.MemberSubscriptionDefinitionRemove', '刪除未使用定義');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES ('MemberSubscriptionRecord', '会员订阅', 'MemberManager', 12, 'subscriptions', 'member/subscription/record/index', NULL, 'N', 'Y', 'C', 'Y', '0', 'member:subscription:view', 'peoples', 'admin', '2026-10-21 00:00:00', '', NULL, '');
INSERT INTO sys_i18n_dict (dict_id, lang_tag, lang_key, lang_value) VALUES ('zh-CN@MENU.NAME.MemberSubscriptionRecord', 'zh-CN', 'MENU.NAME.MemberSubscriptionRecord', '会员订阅');
INSERT INTO sys_i18n_dict (dict_id, lang_tag, lang_key, lang_value) VALUES ('en@MENU.NAME.MemberSubscriptionRecord', 'en', 'MENU.NAME.MemberSubscriptionRecord', 'Member Subscriptions');
INSERT INTO sys_i18n_dict (dict_id, lang_tag, lang_key, lang_value) VALUES ('zh-TW@MENU.NAME.MemberSubscriptionRecord', 'zh-TW', 'MENU.NAME.MemberSubscriptionRecord', '會員訂閱');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES ('MemberSubscriptionAdjust', '调整或撤销有效期', 'MemberSubscriptionRecord', 1, '', NULL, NULL, 'N', 'Y', 'F', 'Y', '0', 'member:subscription:adjust', '#', 'admin', '2026-10-21 00:00:00', '', NULL, '');
INSERT INTO sys_i18n_dict (dict_id, lang_tag, lang_key, lang_value) VALUES ('zh-CN@MENU.NAME.MemberSubscriptionAdjust', 'zh-CN', 'MENU.NAME.MemberSubscriptionAdjust', '调整或撤销有效期');
INSERT INTO sys_i18n_dict (dict_id, lang_tag, lang_key, lang_value) VALUES ('en@MENU.NAME.MemberSubscriptionAdjust', 'en', 'MENU.NAME.MemberSubscriptionAdjust', 'Adjust or Revoke Validity');
INSERT INTO sys_i18n_dict (dict_id, lang_tag, lang_key, lang_value) VALUES ('zh-TW@MENU.NAME.MemberSubscriptionAdjust', 'zh-TW', 'MENU.NAME.MemberSubscriptionAdjust', '調整或撤銷有效期');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES ('MemberSubscriptionLog', '时长调整审计', 'MemberSubscriptionRecord', 2, '', NULL, NULL, 'N', 'Y', 'F', 'Y', '0', 'member:subscription:log:view', '#', 'admin', '2026-10-21 00:00:00', '', NULL, '');
INSERT INTO sys_i18n_dict (dict_id, lang_tag, lang_key, lang_value) VALUES ('zh-CN@MENU.NAME.MemberSubscriptionLog', 'zh-CN', 'MENU.NAME.MemberSubscriptionLog', '时长调整审计');
INSERT INTO sys_i18n_dict (dict_id, lang_tag, lang_key, lang_value) VALUES ('en@MENU.NAME.MemberSubscriptionLog', 'en', 'MENU.NAME.MemberSubscriptionLog', 'Duration Adjustment Audit');
INSERT INTO sys_i18n_dict (dict_id, lang_tag, lang_key, lang_value) VALUES ('zh-TW@MENU.NAME.MemberSubscriptionLog', 'zh-TW', 'MENU.NAME.MemberSubscriptionLog', '時長調整稽核');


-- 站点置顶及树形结构。
ALTER TABLE cms_site ADD COLUMN top_flag bigint NOT NULL DEFAULT 0 COMMENT '置顶标识';
ALTER TABLE cms_site ADD COLUMN top_date datetime(0) NULL COMMENT '置顶结束时间';

ALTER TABLE cms_site ADD COLUMN ancestors varchar(500) NOT NULL DEFAULT '0' COMMENT '站点祖先路径';
ALTER TABLE cms_site ADD COLUMN tree_level int NOT NULL DEFAULT 1 COMMENT '站点层级';
ALTER TABLE cms_site ADD COLUMN child_count int NOT NULL DEFAULT 0 COMMENT '直接子站点数量';
UPDATE cms_site SET ancestors = CONCAT('', site_id) WHERE parent_id = 0;
CREATE INDEX idx_cms_site_parent_id ON cms_site (parent_id);


-- 补齐菜单名称的数据库翻译。只插入缺失记录，保留已有翻译。
-- 菜单记录存在时才补齐对应译文，避免恢复已删除菜单的翻译。
-- 开源版简体中文、繁体中文和英文。
INSERT INTO sys_i18n_dict (dict_id, lang_tag, lang_key, lang_value) SELECT 'zh-CN@MENU.NAME.LogCdnRefresh', 'zh-CN', 'MENU.NAME.LogCdnRefresh', 'CDN刷新日志' WHERE NOT EXISTS (SELECT 1 FROM sys_i18n_dict WHERE lang_tag = 'zh-CN' AND lang_key = 'MENU.NAME.LogCdnRefresh') AND EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 'LogCdnRefresh');
INSERT INTO sys_i18n_dict (dict_id, lang_tag, lang_key, lang_value) SELECT 'en@MENU.NAME.LogCdnRefresh', 'en', 'MENU.NAME.LogCdnRefresh', 'CDN Refresh Logs' WHERE NOT EXISTS (SELECT 1 FROM sys_i18n_dict WHERE lang_tag = 'en' AND lang_key = 'MENU.NAME.LogCdnRefresh') AND EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 'LogCdnRefresh');
INSERT INTO sys_i18n_dict (dict_id, lang_tag, lang_key, lang_value) SELECT 'zh-TW@MENU.NAME.LogCdnRefresh', 'zh-TW', 'MENU.NAME.LogCdnRefresh', 'CDN刷新日誌' WHERE NOT EXISTS (SELECT 1 FROM sys_i18n_dict WHERE lang_tag = 'zh-TW' AND lang_key = 'MENU.NAME.LogCdnRefresh') AND EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 'LogCdnRefresh');
