package com.chestnut.contentcore.domain.dto;

import org.springframework.web.multipart.MultipartFile;

import com.chestnut.common.security.domain.BaseDTO;
import com.chestnut.contentcore.domain.CmsSite;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ResourceUploadDTO extends BaseDTO {

	private CmsSite site;

	private MultipartFile file;
	
	private String name;
	
	private String remark;
	
}
