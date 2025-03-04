ALTER TABLE cms_ad_click_log ADD COLUMN `ad_name` varchar(255);
ALTER TABLE cms_ad_click_log MODIFY COLUMN `ip` varchar(40);
ALTER TABLE cms_ad_click_log MODIFY COLUMN `address` varchar(100);
ALTER TABLE cms_ad_click_log MODIFY COLUMN `browser` varchar(50);
ALTER TABLE cms_ad_click_log MODIFY COLUMN `os` varchar(50);
ALTER TABLE cms_ad_click_log MODIFY COLUMN `device_type` varchar(50);
ALTER TABLE cms_ad_click_log MODIFY COLUMN `locale` varchar(20);

ALTER TABLE cms_ad_view_log ADD COLUMN `ad_name` varchar(255);
ALTER TABLE cms_ad_view_log MODIFY COLUMN `ip` varchar(40);
ALTER TABLE cms_ad_view_log MODIFY COLUMN `address` varchar(100);
ALTER TABLE cms_ad_view_log MODIFY COLUMN `browser` varchar(50);
ALTER TABLE cms_ad_view_log MODIFY COLUMN `os` varchar(50);
ALTER TABLE cms_ad_view_log MODIFY COLUMN `device_type` varchar(50);
ALTER TABLE cms_ad_view_log MODIFY COLUMN `locale` varchar(20);
