package com.chestnut.common.utils.poi.converter;

import java.util.Map;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
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
