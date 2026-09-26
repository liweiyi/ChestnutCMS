

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, `query`, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('LogCdnRefresh', 'CDN刷新日志', 'Logs', 5, '/monitor/logs/cdnRefresh', 'monitor/logs/cdnRefresh', NULL, 'N', 'Y', 'C', 'N', '0', 'monitor:logs:view', 'log', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, '');



-- Role department ownership
-- Historical role assignments remain effective until an administrator changes them.
ALTER TABLE sys_dept MODIFY COLUMN ancestors varchar(500);
ALTER TABLE sys_user ADD COLUMN dept_ancestors varchar(500);
ALTER TABLE sys_role ADD COLUMN dept_id bigint NULL;
ALTER TABLE sys_role ADD COLUMN dept_ancestors varchar(500);
-- 支持按所属机构筛选角色及检查机构是否关联角色。
CREATE INDEX idx_sys_role_dept_id ON sys_role (dept_id);

TRUNCATE TABLE sys_user_role;
ALTER TABLE sys_user_role ADD COLUMN user_name varchar(30);
ALTER TABLE sys_user_role ADD COLUMN nick_name varchar(30);
ALTER TABLE sys_user_role ADD COLUMN real_name varchar(50);
ALTER TABLE sys_user_role ADD COLUMN phone_number varchar(20);
ALTER TABLE sys_user_role ADD COLUMN email varchar(50);
UPDATE sys_user_role SET user_name = (select user_name from sys_user where sys_user.user_id = sys_user_role.user_id);
UPDATE sys_user_role SET nick_name = (select nick_name from sys_user where sys_user.user_id = sys_user_role.user_id);
UPDATE sys_user_role SET real_name = (select real_name from sys_user where sys_user.user_id = sys_user_role.user_id);
UPDATE sys_user_role SET phone_number = (select phone_number from sys_user where sys_user.user_id = sys_user_role.user_id);
UPDATE sys_user_role SET email = (select email from sys_user where sys_user.user_id = sys_user_role.user_id);
-- End role department ownership

-- Successful Java update patches; failed attempts are only logged and retried on startup.
CREATE TABLE sys_update_patcher (
    patcher_id varchar(32) NOT NULL COMMENT '补丁版本号',
    start_time datetime(3) NOT NULL COMMENT '执行开始时间',
    cost_seconds int NOT NULL COMMENT '执行耗时，单位：秒',
    PRIMARY KEY (patcher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='更新补丁成功记录';

ALTER TABLE sys_user_binding DROP COLUMN access_token;
ALTER TABLE sys_user_binding DROP COLUMN refresh_token;
ALTER TABLE sys_user_binding DROP COLUMN token_expire_time;
ALTER TABLE sys_user_binding ADD COLUMN properties longtext;