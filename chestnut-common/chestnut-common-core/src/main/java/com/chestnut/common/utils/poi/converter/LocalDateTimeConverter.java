package com.chestnut.common.utils.poi.converter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.chestnut.common.utils.DateUtils;

public class LocalDateTimeConverter implements Converter<LocalDateTime> {
	
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DateUtils.YYYY_MM_DD_HH_MM_SS);

	@Override
	public CellDataTypeEnum supportExcelTypeKey() {
		return CellDataTypeEnum.STRING;
	}
	
	@Override
	public Class<?> supportJavaTypeKey() {
		return LocalDateTime.class;
	}
	
	@Override
	public WriteCellData<?> convertToExcelData(LocalDateTime value, ExcelContentProperty contentProperty,
			GlobalConfiguration globalConfiguration) throws Exception {
		return new WriteCellData<>(FORMATTER.format(value));
	}
	
	@Override
	public LocalDateTime convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
			GlobalConfiguration globalConfiguration) throws Exception {
		return LocalDateTime.parse(cellData.getStringValue(), FORMATTER);
	}
}
