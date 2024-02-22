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
package com.chestnut.system.config.converter;

import java.util.Objects;
import java.util.Optional;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.utils.SpringUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.annotation.ExcelDictField;
import com.chestnut.system.domain.SysDictData;
import com.chestnut.system.service.ISysDictTypeService;

public class DictConverter implements Converter<Object> {

	private ISysDictTypeService dictService = SpringUtils.getBean(ISysDictTypeService.class);

	@Override
	public Class<?> supportJavaTypeKey() {
		return Object.class;
	}

	@Override
	public CellDataTypeEnum supportExcelTypeKey() {
		return null;
	}

	@Override
	public WriteCellData<String> convertToExcelData(Object value, ExcelContentProperty contentProperty,
			GlobalConfiguration cfg) throws Exception {
		if (Objects.nonNull(value)) {
			ExcelDictField excelDictField = contentProperty.getField().getAnnotation(ExcelDictField.class);
			if (Objects.nonNull(excelDictField) && StringUtils.isNotEmpty(excelDictField.value())) {
				Optional<SysDictData> opt = dictService.optDictData(excelDictField.value(), value.toString());
				if (opt.isPresent()) {
					return new WriteCellData<>(I18nUtils.get(opt.get().getDictLabel(), cfg.getLocale()));
				}
			}
		}
		return new WriteCellData<>(Objects.isNull(value) ? StringUtils.EMPTY : value.toString());
	}

	@Override
	public Object convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
			GlobalConfiguration cfg) throws Exception {
		String value = cellData.getStringValue();
		ExcelDictField excelDictField = contentProperty.getField().getAnnotation(ExcelDictField.class);
		if (Objects.nonNull(excelDictField) && StringUtils.isNotEmpty(excelDictField.value())) {
			Optional<SysDictData> opt = dictService.selectDictDatasByType(excelDictField.value()).stream().filter(d -> {
				return StringUtils.equals(cellData.getStringValue(), I18nUtils.get(d.getDictLabel(), cfg.getLocale()));
			}).findFirst();
			if (opt.isPresent()) {
				return opt.get().getDictValue();
			}
		}
		return value;
	}
}