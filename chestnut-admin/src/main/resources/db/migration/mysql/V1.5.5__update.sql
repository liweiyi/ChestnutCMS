ALTER TABLE cms_cfd_default ADD COLUMN `uid` bigint;
ALTER TABLE cms_custom_form MODIFY COLUMN `rule_limit` VARCHAR(20);

