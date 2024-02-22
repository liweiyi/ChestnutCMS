/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
