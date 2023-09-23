ALTER TABLE cms_video MODIFY file_size BIGINT NULL;
ALTER TABLE cms_video MODIFY format VARCHAR(20) NULL;
ALTER TABLE cms_video MODIFY duration BIGINT NULL;
ALTER TABLE cms_video MODIFY width INT NULL;
ALTER TABLE cms_video MODIFY height INT NULL;
ALTER TABLE cms_video MODIFY bit_rate INT NULL;
ALTER TABLE cms_video MODIFY frame_rate INT NULL;
ALTER TABLE cms_video MODIFY type VARCHAR(10) NULL;

ALTER TABLE cms_video_backup MODIFY file_size BIGINT NULL;
ALTER TABLE cms_video_backup MODIFY format VARCHAR(20) NULL;
ALTER TABLE cms_video_backup MODIFY duration BIGINT NULL;
ALTER TABLE cms_video_backup MODIFY width INT NULL;
ALTER TABLE cms_video_backup MODIFY height INT NULL;
ALTER TABLE cms_video_backup MODIFY bit_rate INT NULL;
ALTER TABLE cms_video_backup MODIFY frame_rate INT NULL;
ALTER TABLE cms_video_backup MODIFY type VARCHAR(10) NULL;

ALTER TABLE cms_audio MODIFY file_size BIGINT NULL;
ALTER TABLE cms_audio MODIFY duration BIGINT NULL;
ALTER TABLE cms_audio MODIFY channels INT NULL;
ALTER TABLE cms_audio MODIFY bit_rate INT NULL;
ALTER TABLE cms_audio MODIFY sampling_rate INT NULL;
ALTER TABLE cms_audio MODIFY type VARCHAR(10) NULL;
ALTER TABLE cms_audio MODIFY format VARCHAR(20) NULL;

ALTER TABLE cms_audio_backup MODIFY file_size BIGINT NULL;
ALTER TABLE cms_audio_backup MODIFY duration BIGINT NULL;
ALTER TABLE cms_audio_backup MODIFY channels INT NULL;
ALTER TABLE cms_audio_backup MODIFY bit_rate INT NULL;
ALTER TABLE cms_audio_backup MODIFY sampling_rate INT NULL;
ALTER TABLE cms_audio_backup MODIFY type VARCHAR(10) NULL;
ALTER TABLE cms_audio_backup MODIFY format VARCHAR(20) NULL;