alter table cms_content_rela add column site_id bigint;
update cms_content_rela a set a.site_id = (select b.site_id from cms_content b where b.content_id=a.content_id);

ALTER TABLE cc_tag_word_group ADD COLUMN owner VARCHAR(100);
ALTER TABLE cc_tag_word_group ADD COLUMN word_total bigint DEFAULT 0;
ALTER TABLE cc_tag_word ADD COLUMN owner VARCHAR(100);
UPDATE cc_tag_word_group SET word_total = (SELECT count(*) FROM cc_tag_word WHERE cc_tag_word.group_id=cc_tag_word_group.group_id);

ALTER TABLE cc_hot_word_group ADD COLUMN owner VARCHAR(100);
ALTER TABLE cc_hot_word_group ADD COLUMN word_total bigint DEFAULT 0;
ALTER TABLE cc_hot_word ADD COLUMN owner VARCHAR(100);
UPDATE cc_hot_word_group SET word_total = (SELECT count(*) FROM cc_hot_word WHERE cc_hot_word.group_id=cc_hot_word_group.group_id);

UPDATE sys_menu set component = 'cms/word/word' WHERE menu_id = '2039';