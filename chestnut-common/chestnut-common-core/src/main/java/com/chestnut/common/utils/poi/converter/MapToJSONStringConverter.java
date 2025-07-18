/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
package com.chestnut.common.utils.poi.converter;

import java.util.Map;

import cn.idev.excel.converters.Converter;
import cn.idev.excel.enums.CellDataTypeEnum;
import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.ReadCellData;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;
import com.chestnut.common.utils.JacksonUtils;

public class MapToJSONStringConverter implements Converter<Map<?, ?>> {
	
	@Override
	public CellDataTypeEnum supportExcelTypeKey() {
		return CellDataTypeEnum.STRING;
	}
	
	@Override
	public Class<?> supportJavaTypeKey() {
		return Map.class;
	}
	
	@Override
	public WriteCellData<?> convertToExcelData(Map<?, ?> value, ExcelContentProperty contentProperty,
			GlobalConfiguration globalConfiguration) throws Exception {
		return new WriteCellData<>(JacksonUtils.to(value));
	}
	
	@Override
	public Map<?, ?> convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
			GlobalConfiguration globalConfiguration) throws Exception {
		return JacksonUtils.fromMap(cellData.getStringValue());
	}
}
