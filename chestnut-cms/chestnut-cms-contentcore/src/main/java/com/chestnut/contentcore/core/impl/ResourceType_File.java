package com.chestnut.contentcore.core.impl;

import com.chestnut.contentcore.fixed.config.AllowUploadFileType;
import org.springframework.stereotype.Component;

import com.chestnut.contentcore.core.IResourceType;

import lombok.RequiredArgsConstructor;

/**
 * 资源类型：文件
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IResourceType.BEAN_NAME_PREFIX + ResourceType_File.ID)
public class ResourceType_File implements IResourceType {
	
	public final static String ID = "file";
	
	public static final  String NAME = "{CMS.CONTENTCORE.RESOURCE_TYPE." + ID + "}";
	
	private final static String[] SuffixArray = AllowUploadFileType.ALLOWED_UPLOAD_EXTENSION.toArray(String[]::new);
	
	@Override
	public String getId() {
		return ID;
	}
	
	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public String[] getUsableSuffix() {
		return SuffixArray;
	}
}
