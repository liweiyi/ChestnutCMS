ALTER TABLE search_word ADD COLUMN top_flag bigint;
ALTER TABLE search_word ADD COLUMN top_date datetime;
ALTER TABLE search_word ADD COLUMN source varchar(255);

CREATE TABLE `search_word_hour_stat` (
  `stat_id` bigint NOT NULL AUTO_INCREMENT,
  `hour` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `word_id` bigint NOT NULL,
  `word` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `search_count` bigint NOT NULL,
  PRIMARY KEY (`stat_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
# 菜单名称路由调整
UPDATE sys_menu SET component = 'search/wordTab', path = 'searchWord' where menu_id = 2053;

ALTER TABLE cms_content ADD COLUMN prop1 VARCHAR(100);
ALTER TABLE cms_content ADD COLUMN prop2 VARCHAR(100);
ALTER TABLE cms_content ADD COLUMN prop3 VARCHAR(255);
ALTER TABLE cms_content ADD COLUMN prop4 VARCHAR(255);
