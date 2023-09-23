package com.chestnut.common.utils.poi.converter;

import java.lang.reflect.Array;
import java.util.Objects;
import java.util.Set;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.chestnut.common.utils.StringUtils;

public class StringToSetConverter implements Converter<Set<String>> {
	
	@Override
	public CellDataTypeEnum supportExcelTypeKey() {
		return CellDataTypeEnum.STRING;
	}
	
	@Override
	public Class<?> supportJavaTypeKey() {
		return Array.class;
	}
	
	@Override
	public WriteCellData<?> convertToExcelData(Set<String> value, ExcelContentProperty contentProperty,
			GlobalConfiguration globalConfiguration) throws Exception {
		return new WriteCellData<>(Objects.isNull(value) ? StringUtils.EMPTY : String.join(";", value));
	}
	
	@Override
	public Set<String> convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
			GlobalConfiguration globalConfiguration) throws Exception {
		return Set.of(StringUtils.split(cellData.getStringValue(), ";"));
	}
}
