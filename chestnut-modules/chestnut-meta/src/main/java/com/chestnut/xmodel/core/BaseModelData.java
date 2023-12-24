package com.chestnut.xmodel.core;

import com.chestnut.common.utils.ConvertUtils;
import com.chestnut.common.utils.StringUtils;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 元数据模型数据表基类，提供多种常见类型字段作为扩展字段
 * 
 * 25xShortText = varchar(50)
 * 25xMediumText = varchar(200)
 * 4xLargeText = varchar(2000)
 * 1xClobText1 = clob
 * 10xLong = bigint
 * 10xDouble = double
 * 10xDate = datetime
 */
@Getter
@Setter
public class BaseModelData {

	private String shortText1;

	private String shortText2;

	private String shortText3;

	private String shortText4;

	private String shortText5;

	private String shortText6;

	private String shortText7;

	private String shortText8;

	private String shortText9;

	private String shortText10;

	private String shortText11;

	private String shortText12;

	private String shortText13;

	private String shortText14;

	private String shortText15;

	private String shortText16;

	private String shortText17;

	private String shortText18;

	private String shortText19;

	private String shortText20;

	private String shortText21;

	private String shortText22;

	private String shortText23;

	private String shortText24;

	private String shortText25;

	private String mediumText1;

	private String mediumText2;

	private String mediumText3;

	private String mediumText4;

	private String mediumText5;

	private String mediumText6;

	private String mediumText7;

	private String mediumText8;

	private String mediumText9;

	private String mediumText10;

	private String mediumText11;

	private String mediumText12;

	private String mediumText13;

	private String mediumText14;

	private String mediumText15;

	private String mediumText16;

	private String mediumText17;

	private String mediumText18;

	private String mediumText19;

	private String mediumText20;

	private String mediumText21;

	private String mediumText22;

	private String mediumText23;

	private String mediumText24;

	private String mediumText25;

	private String largeText1;

	private String largeText2;

	private String largeText3;

	private String largeText4;

	private String clobText1;

	private Long long1;

	private Long long2;

	private Long long3;

	private Long long4;

	private Long long5;

	private Long long6;

	private Long long7;

	private Long long8;

	private Long long9;

	private Long long10;

	private Double double1;

	private Double double2;

	private Double double3;

	private Double double4;

	private Double double5;

	private Double double6;

	private Double double7;

	private Double double8;

	private Double double9;

	private Double double10;

	private LocalDateTime date1;

	private LocalDateTime date2;

	private LocalDateTime date3;

	private LocalDateTime date4;

	private LocalDateTime date5;

	private LocalDateTime date6;

	private LocalDateTime date7;

	private LocalDateTime date8;

	private LocalDateTime date9;

	private LocalDateTime date10;

	public void setFieldValue(@NotNull String fieldName, Object fieldValue) {
		switch(fieldName) {
			case "short_text1", "short_text2", "short_text3", "short_text4", "short_text5", "short_text6", "short_text7", "short_text8", "short_text9", "short_text10", "short_text11", "short_text12", "short_text13", "short_text14", "short_text15", "short_text16", "short_text17", "short_text18", "short_text19", "short_text20", "short_text21", "short_text22", "short_text23", "short_text24", "short_text25", "medium_text1", "medium_text2", "medium_text3", "medium_text4", "medium_text5", "medium_text6", "medium_text7", "medium_text8", "medium_text9", "medium_text10", "medium_text11", "medium_text12", "medium_text13", "medium_text14", "medium_text15", "medium_text16", "medium_text17", "medium_text18", "medium_text19", "medium_text20", "medium_text21", "medium_text22", "medium_text23", "medium_text24", "medium_text25", "large_text1", "large_text2", "large_text3", "large_text4", "clob_text1" -> ConvertUtils.toStr(fieldValue);
			case "long1", "long2", "long3", "long4", "long5", "long6", "long7", "long8", "long9", "long10" -> ConvertUtils.toLong(fieldValue);
			case "double1", "double2", "double3", "double4", "double5", "double6", "double7", "double8", "double9", "double10" -> ConvertUtils.toDouble(fieldValue);
			case "date1", "date2", "date3", "date4", "date5", "date6", "date7", "date8", "date9", "date10" -> ConvertUtils.toLocalDateTime(fieldValue);
			default -> throw new RuntimeException("Unknown field name: " + fieldName);
		}
	}

	public static void main(String[] args) {
		List<String> stringFields = new ArrayList<>();
		List<String> longFields = new ArrayList<>();
		List<String> doubleFields = new ArrayList<>();
		List<String> dateFields = new ArrayList<>();
		Field[] declaredFields = BaseModelData.class.getDeclaredFields();
		for (Field field : declaredFields) {
			String fieldName = StringUtils.toUnderScoreCase(field.getName());
			if (field.getType() == String.class) {
				stringFields.add(fieldName);
			} else if (field.getType() == Long.class) {
				longFields.add(fieldName);
			} else if (field.getType() == Double.class) {
				doubleFields.add(fieldName);
			} else if (field.getType() == LocalDateTime.class) {
				dateFields.add(fieldName);
			}
		}
		StringBuilder sb = new StringBuilder();
		sb.append("switch(fieldName) {\n");

		String stringFieldsStr = stringFields.stream().map(fn -> "\"" + fn + "\"").collect(Collectors.joining(", "));
		sb.append("\tcase ").append(stringFieldsStr).append(" -> ConvertUtils.toStr(fieldValue);\n");

		String longFieldsStr = longFields.stream().map(fn -> "\"" + fn + "\"").collect(Collectors.joining(", "));
		sb.append("\tcase ").append(longFieldsStr).append(" -> ConvertUtils.toLong(fieldValue);\n");

		String doubleFieldStr = doubleFields.stream().map(fn -> "\"" + fn + "\"").collect(Collectors.joining(", "));
		sb.append("\tcase ").append(doubleFieldStr).append(" -> ConvertUtils.toDouble(fieldValue);\n");

		String dateFieldStr = dateFields.stream().map(fn -> "\"" + fn + "\"").collect(Collectors.joining(", "));
		sb.append("\tcase ").append(dateFieldStr).append(" -> ConvertUtils.toLocalDateTime(fieldValue);\n");

		sb.append("\tdefault -> throw new RuntimeException(\"Unknown field name: \" + fieldName);\n");
		sb.append("}");
		System.out.println(sb);
	}
}
