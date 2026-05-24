ALTER TABLE sys_security_config ADD COLUMN name varchar(100);
ALTER TABLE sys_security_config ADD COLUMN configs longtext;

ALTER TABLE sys_menu MODIFY COLUMN menu_id varchar(100);
ALTER TABLE sys_menu MODIFY COLUMN parent_id varchar(100);
