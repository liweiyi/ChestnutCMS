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
package com.chestnut.contentcore.properties;

import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.IProperty;
import com.chestnut.contentcore.enums.WatermarkerPosition;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * 图片水印参数配置
 */
@RequiredArgsConstructor
@Component(IProperty.BEAN_NAME_PREFIX + ImageWatermarkArgsProperty.ID)
public class ImageWatermarkArgsProperty implements IProperty {

	public final static String ID = "ImageWatermarkArgs";
	
	static UseType[] UseTypes = new UseType[] { UseType.Site };
	
	@Override
	public UseType[] getUseTypes() {
		return UseTypes;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "图片水印参数";
	}
	
	@Override
	public ImageWatermarkArgs defaultValue() {
		return new ImageWatermarkArgs();
	}
	
	@Override
	public ImageWatermarkArgs getPropValue(Map<String, String> configProps) {
		String v = MapUtils.getString(configProps, ID);
		if (StringUtils.isNotEmpty(v)) {
			return JacksonUtils.from(v, ImageWatermarkArgs.class);
		}
		return defaultValue();
	}
	
	public static ImageWatermarkArgs getValue(Map<String, String> configProps) {
		String v = MapUtils.getString(configProps, ID);
		if (StringUtils.isNotEmpty(v)) {
			return JacksonUtils.from(v, ImageWatermarkArgs.class);
		}
		return new ImageWatermarkArgs();
	}
	
	@Getter
	@Setter
	public static class ImageWatermarkArgs {
		
		private String image = StringUtils.EMPTY;
		
		private String position = WatermarkerPosition.TOP_RIGHT.name();
		
		private float opacity = 1f;

		private float ratio = 30f;
	}
}
