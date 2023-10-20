ALTER TABLE x_model_field ADD COLUMN validations varchar(500) DEFAULT '' COMMENT '校验规则';
ALTER TABLE x_model_field DROP COLUMN mandatory_flag;