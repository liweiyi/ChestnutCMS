-- 区号与本地号码独立存储，升级时为现存数据回填中国大陆区号。
ALTER TABLE sys_user ADD COLUMN phone_calling_code varchar(4);
ALTER TABLE sys_user_role ADD COLUMN phone_calling_code varchar(4);
ALTER TABLE cc_member ADD COLUMN phone_calling_code varchar(4);
ALTER TABLE cc_member ADD COLUMN lock_end_time datetime;

UPDATE sys_user SET phone_calling_code = '+86';
UPDATE sys_user SET phone_number = concat('+86', phone_number) WHERE phone_number IS NOT NULL AND LENGTH(phone_number) > 5;
UPDATE sys_user_role SET phone_calling_code = '+86';
UPDATE sys_user_role SET phone_number = concat('+86', phone_number) WHERE phone_number IS NOT NULL AND LENGTH(phone_number) > 5;
UPDATE cc_member SET phone_calling_code = '+86';
UPDATE cc_member SET phone_number = concat('+86', phone_number) WHERE phone_number IS NOT NULL AND LENGTH(phone_number) > 5;

-- 本地号码预留国际号码长度。
ALTER TABLE cc_member MODIFY COLUMN phone_number varchar(20);

-- 会员关注与粉丝管理。
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES ('MemberFollow', '关注管理', 'MemberManager', 6, 'follow', 'member/follow', NULL, 'N', 'Y', 'C', 'Y', '0', 'member:follow:view', 'peoples', 'admin', '2026-10-15 00:00:00', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES ('MemberFollowCancel', '取消', 'MemberFollow', 1, '', NULL, NULL, 'N', 'Y', 'F', 'Y', '0', 'member:follow:cancel', '#', 'admin', '2026-10-15 00:00:00', '', NULL, '');
INSERT INTO `sys_i18n_dict` (`dict_id`, `lang_tag`, `lang_key`, `lang_value`) VALUES ('zh-CN@MENU.NAME.MemberFollow', 'zh-CN', 'MENU.NAME.MemberFollow', '关注管理');
INSERT INTO `sys_i18n_dict` (`dict_id`, `lang_tag`, `lang_key`, `lang_value`) VALUES ('en@MENU.NAME.MemberFollow', 'en', 'MENU.NAME.MemberFollow', 'Follow Management');
INSERT INTO `sys_i18n_dict` (`dict_id`, `lang_tag`, `lang_key`, `lang_value`) VALUES ('zh-TW@MENU.NAME.MemberFollow', 'zh-TW', 'MENU.NAME.MemberFollow', '關注管理');
INSERT INTO `sys_i18n_dict` (`dict_id`, `lang_tag`, `lang_key`, `lang_value`) VALUES ('zh-CN@MENU.NAME.MemberFollowCancel', 'zh-CN', 'MENU.NAME.MemberFollowCancel', '取消');
INSERT INTO `sys_i18n_dict` (`dict_id`, `lang_tag`, `lang_key`, `lang_value`) VALUES ('en@MENU.NAME.MemberFollowCancel', 'en', 'MENU.NAME.MemberFollowCancel', 'Cancel');
INSERT INTO `sys_i18n_dict` (`dict_id`, `lang_tag`, `lang_key`, `lang_value`) VALUES ('zh-TW@MENU.NAME.MemberFollowCancel', 'zh-TW', 'MENU.NAME.MemberFollowCancel', '取消');

-- 会员等级管理及经验日志查询。
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES ('MemberLevelManage', '等级管理', 'MemberManager', 7, 'level', 'member/level', NULL, 'N', 'Y', 'C', 'Y', '0', 'member:level:manage', 'guide', 'admin', '2026-10-15 00:00:00', '', NULL, '');
INSERT INTO `sys_i18n_dict` (`dict_id`, `lang_tag`, `lang_key`, `lang_value`) VALUES ('zh-CN@MENU.NAME.MemberLevelManage', 'zh-CN', 'MENU.NAME.MemberLevelManage', '等级管理');
INSERT INTO `sys_i18n_dict` (`dict_id`, `lang_tag`, `lang_key`, `lang_value`) VALUES ('en@MENU.NAME.MemberLevelManage', 'en', 'MENU.NAME.MemberLevelManage', 'Level Management');
INSERT INTO `sys_i18n_dict` (`dict_id`, `lang_tag`, `lang_key`, `lang_value`) VALUES ('zh-TW@MENU.NAME.MemberLevelManage', 'zh-TW', 'MENU.NAME.MemberLevelManage', '等級管理');
CREATE INDEX ix_member_exp_log_member_type_time ON cc_member_exp_log (member_id, level_type, log_time, log_id);

-- 会员收藏管理。
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES ('MemberFavorites', '收藏管理', 'MemberManager', 8, 'favorites', 'member/favorites', NULL, 'N', 'Y', 'C', 'Y', '0', 'member:favorites:view', 'star', 'admin', '2026-10-15 00:00:00', '', NULL, '');
INSERT INTO `sys_i18n_dict` (`dict_id`, `lang_tag`, `lang_key`, `lang_value`) VALUES ('zh-CN@MENU.NAME.MemberFavorites', 'zh-CN', 'MENU.NAME.MemberFavorites', '收藏管理');
INSERT INTO `sys_i18n_dict` (`dict_id`, `lang_tag`, `lang_key`, `lang_value`) VALUES ('en@MENU.NAME.MemberFavorites', 'en', 'MENU.NAME.MemberFavorites', 'Favorites Management');
INSERT INTO `sys_i18n_dict` (`dict_id`, `lang_tag`, `lang_key`, `lang_value`) VALUES ('zh-TW@MENU.NAME.MemberFavorites', 'zh-TW', 'MENU.NAME.MemberFavorites', '收藏管理');
CREATE INDEX ix_member_favorites_member_time ON cc_member_favorites (member_id, create_time, log_id);

-- 会员互动日志。
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES ('MemberInteractionLog', '互动日志', 'MemberManager', 9, 'interactionLog', 'member/interactionLog', NULL, 'N', 'Y', 'C', 'Y', '0', 'member:interaction:view', 'log', 'admin', '2026-10-15 00:00:00', '', NULL, '');
INSERT INTO `sys_i18n_dict` (`dict_id`, `lang_tag`, `lang_key`, `lang_value`) VALUES ('zh-CN@MENU.NAME.MemberInteractionLog', 'zh-CN', 'MENU.NAME.MemberInteractionLog', '互动日志');
INSERT INTO `sys_i18n_dict` (`dict_id`, `lang_tag`, `lang_key`, `lang_value`) VALUES ('en@MENU.NAME.MemberInteractionLog', 'en', 'MENU.NAME.MemberInteractionLog', 'Interaction Logs');
INSERT INTO `sys_i18n_dict` (`dict_id`, `lang_tag`, `lang_key`, `lang_value`) VALUES ('zh-TW@MENU.NAME.MemberInteractionLog', 'zh-TW', 'MENU.NAME.MemberInteractionLog', '互動日誌');
CREATE INDEX ix_member_like_member_time ON cc_member_like (member_id, create_time, log_id);
CREATE INDEX ix_member_like_time ON cc_member_like (create_time, log_id);
CREATE INDEX ix_member_signin_member_time ON cc_member_signin_log (member_id, log_time, log_id);
CREATE INDEX ix_member_signin_time ON cc_member_signin_log (log_time, log_id);
