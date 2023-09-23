package com.chestnut.contentcore.domain.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileVO {
	
	private String filePath;

	private String fileName;
	
	private Boolean isDirectory;
	
	private Boolean canEdit = false;
	
	private Long fileSize;
	
	private LocalDateTime modifyTime;
}
