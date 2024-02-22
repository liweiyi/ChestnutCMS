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
package com.chestnut.xmodel.core;

import com.chestnut.common.utils.ConvertUtils;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

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

	private LocalDateTime datetime1;

	private LocalDateTime datetime2;

	private LocalDateTime datetime3;

	private LocalDateTime datetime4;

	private LocalDateTime datetime5;

	private LocalDateTime datetime6;

	private LocalDateTime datetime7;

	private LocalDateTime datetime8;

	private LocalDateTime datetime9;

	private LocalDateTime datetime10;

	public boolean isStringField(@NotNull String fieldName) {
		return switch(fieldName) {
			case "short_text1", "short_text2", "short_text3", "short_text4", "short_text5", "short_text6", "short_text7", "short_text8", "short_text9", "short_text10", "short_text11", "short_text12", "short_text13", "short_text14", "short_text15", "short_text16", "short_text17", "short_text18", "short_text19", "short_text20", "short_text21", "short_text22", "short_text23", "short_text24", "short_text25", "medium_text1", "medium_text2", "medium_text3", "medium_text4", "medium_text5", "medium_text6", "medium_text7", "medium_text8", "medium_text9", "medium_text10", "medium_text11", "medium_text12", "medium_text13", "medium_text14", "medium_text15", "medium_text16", "medium_text17", "medium_text18", "medium_text19", "medium_text20", "medium_text21", "medium_text22", "medium_text23", "medium_text24", "medium_text25", "large_text1", "large_text2", "large_text3", "large_text4", "clob_text1" -> true;		default -> false;
		};
	}

	public String getStringFieldValue(@NotNull String fieldName) {
		return switch(fieldName) {
			case "short_text1" -> getShortText1();
			case "short_text2" -> getShortText2();
			case "short_text3" -> getShortText3();
			case "short_text4" -> getShortText4();
			case "short_text5" -> getShortText5();
			case "short_text6" -> getShortText6();
			case "short_text7" -> getShortText7();
			case "short_text8" -> getShortText8();
			case "short_text9" -> getShortText9();
			case "short_text10" -> getShortText10();
			case "short_text11" -> getShortText11();
			case "short_text12" -> getShortText12();
			case "short_text13" -> getShortText13();
			case "short_text14" -> getShortText14();
			case "short_text15" -> getShortText15();
			case "short_text16" -> getShortText16();
			case "short_text17" -> getShortText17();
			case "short_text18" -> getShortText18();
			case "short_text19" -> getShortText19();
			case "short_text20" -> getShortText20();
			case "short_text21" -> getShortText21();
			case "short_text22" -> getShortText22();
			case "short_text23" -> getShortText23();
			case "short_text24" -> getShortText24();
			case "short_text25" -> getShortText25();
			case "medium_text1" -> getMediumText1();
			case "medium_text2" -> getMediumText2();
			case "medium_text3" -> getMediumText3();
			case "medium_text4" -> getMediumText4();
			case "medium_text5" -> getMediumText5();
			case "medium_text6" -> getMediumText6();
			case "medium_text7" -> getMediumText7();
			case "medium_text8" -> getMediumText8();
			case "medium_text9" -> getMediumText9();
			case "medium_text10" -> getMediumText10();
			case "medium_text11" -> getMediumText11();
			case "medium_text12" -> getMediumText12();
			case "medium_text13" -> getMediumText13();
			case "medium_text14" -> getMediumText14();
			case "medium_text15" -> getMediumText15();
			case "medium_text16" -> getMediumText16();
			case "medium_text17" -> getMediumText17();
			case "medium_text18" -> getMediumText18();
			case "medium_text19" -> getMediumText19();
			case "medium_text20" -> getMediumText20();
			case "medium_text21" -> getMediumText21();
			case "medium_text22" -> getMediumText22();
			case "medium_text23" -> getMediumText23();
			case "medium_text24" -> getMediumText24();
			case "medium_text25" -> getMediumText25();
			case "large_text1" -> getLargeText1();
			case "large_text2" -> getLargeText2();
			case "large_text3" -> getLargeText3();
			case "large_text4" -> getLargeText4();
			case "clob_text1" -> getClobText1();
			default -> throw new RuntimeException("Unknown field name: " + fieldName);
		};
	}

	public Long getLongFieldValue(@NotNull String fieldName) {
		return switch(fieldName) {
			case "long1" -> getLong1();
			case "long2" -> getLong2();
			case "long3" -> getLong3();
			case "long4" -> getLong4();
			case "long5" -> getLong5();
			case "long6" -> getLong6();
			case "long7" -> getLong7();
			case "long8" -> getLong8();
			case "long9" -> getLong9();
			case "long10" -> getLong10();
			default -> throw new RuntimeException("Unknown field name: " + fieldName);
		};
	}

	public Double getDoubleFieldValue(@NotNull String fieldName) {
		return switch(fieldName) {
			case "double1" -> getDouble1();
			case "double2" -> getDouble2();
			case "double3" -> getDouble3();
			case "double4" -> getDouble4();
			case "double5" -> getDouble5();
			case "double6" -> getDouble6();
			case "double7" -> getDouble7();
			case "double8" -> getDouble8();
			case "double9" -> getDouble9();
			case "double10" -> getDouble10();
			default -> throw new RuntimeException("Unknown field name: " + fieldName);
		};
	}

	public LocalDateTime getDateFieldValue(@NotNull String fieldName) {
		return switch(fieldName) {
			case "datetime1" -> getDatetime1();
			case "datetime2" -> getDatetime2();
			case "datetime3" -> getDatetime3();
			case "datetime4" -> getDatetime4();
			case "datetime5" -> getDatetime5();
			case "datetime6" -> getDatetime6();
			case "datetime7" -> getDatetime7();
			case "datetime8" -> getDatetime8();
			case "datetime9" -> getDatetime9();
			case "datetime10" -> getDatetime10();
			default -> throw new RuntimeException("Unknown field name: " + fieldName);
		};
	}

	public Object getFieldValue(@NotNull String fieldName) {
		return switch(fieldName) {
			case "short_text1" -> getShortText1();
			case "short_text2" -> getShortText2();
			case "short_text3" -> getShortText3();
			case "short_text4" -> getShortText4();
			case "short_text5" -> getShortText5();
			case "short_text6" -> getShortText6();
			case "short_text7" -> getShortText7();
			case "short_text8" -> getShortText8();
			case "short_text9" -> getShortText9();
			case "short_text10" -> getShortText10();
			case "short_text11" -> getShortText11();
			case "short_text12" -> getShortText12();
			case "short_text13" -> getShortText13();
			case "short_text14" -> getShortText14();
			case "short_text15" -> getShortText15();
			case "short_text16" -> getShortText16();
			case "short_text17" -> getShortText17();
			case "short_text18" -> getShortText18();
			case "short_text19" -> getShortText19();
			case "short_text20" -> getShortText20();
			case "short_text21" -> getShortText21();
			case "short_text22" -> getShortText22();
			case "short_text23" -> getShortText23();
			case "short_text24" -> getShortText24();
			case "short_text25" -> getShortText25();
			case "medium_text1" -> getMediumText1();
			case "medium_text2" -> getMediumText2();
			case "medium_text3" -> getMediumText3();
			case "medium_text4" -> getMediumText4();
			case "medium_text5" -> getMediumText5();
			case "medium_text6" -> getMediumText6();
			case "medium_text7" -> getMediumText7();
			case "medium_text8" -> getMediumText8();
			case "medium_text9" -> getMediumText9();
			case "medium_text10" -> getMediumText10();
			case "medium_text11" -> getMediumText11();
			case "medium_text12" -> getMediumText12();
			case "medium_text13" -> getMediumText13();
			case "medium_text14" -> getMediumText14();
			case "medium_text15" -> getMediumText15();
			case "medium_text16" -> getMediumText16();
			case "medium_text17" -> getMediumText17();
			case "medium_text18" -> getMediumText18();
			case "medium_text19" -> getMediumText19();
			case "medium_text20" -> getMediumText20();
			case "medium_text21" -> getMediumText21();
			case "medium_text22" -> getMediumText22();
			case "medium_text23" -> getMediumText23();
			case "medium_text24" -> getMediumText24();
			case "medium_text25" -> getMediumText25();
			case "large_text1" -> getLargeText1();
			case "large_text2" -> getLargeText2();
			case "large_text3" -> getLargeText3();
			case "large_text4" -> getLargeText4();
			case "clob_text1" -> getClobText1();
			case "long1" -> getLong1();
			case "long2" -> getLong2();
			case "long3" -> getLong3();
			case "long4" -> getLong4();
			case "long5" -> getLong5();
			case "long6" -> getLong6();
			case "long7" -> getLong7();
			case "long8" -> getLong8();
			case "long9" -> getLong9();
			case "long10" -> getLong10();
			case "double1" -> getDouble1();
			case "double2" -> getDouble2();
			case "double3" -> getDouble3();
			case "double4" -> getDouble4();
			case "double5" -> getDouble5();
			case "double6" -> getDouble6();
			case "double7" -> getDouble7();
			case "double8" -> getDouble8();
			case "double9" -> getDouble9();
			case "double10" -> getDouble10();
			case "datetime1" -> getDatetime1();
			case "datetime2" -> getDatetime2();
			case "datetime3" -> getDatetime3();
			case "datetime4" -> getDatetime4();
			case "datetime5" -> getDatetime5();
			case "datetime6" -> getDatetime6();
			case "datetime7" -> getDatetime7();
			case "datetime8" -> getDatetime8();
			case "datetime9" -> getDatetime9();
			case "datetime10" -> getDatetime10();
			default -> throw new RuntimeException("Unknown field name: " + fieldName);
		};
	}

	public void setFieldValue(@NotNull String fieldName, Object fieldValue) {
		switch(fieldName) {
			case "short_text1" -> setShortText1(ConvertUtils.toStr(fieldValue));
			case "short_text2" -> setShortText2(ConvertUtils.toStr(fieldValue));
			case "short_text3" -> setShortText3(ConvertUtils.toStr(fieldValue));
			case "short_text4" -> setShortText4(ConvertUtils.toStr(fieldValue));
			case "short_text5" -> setShortText5(ConvertUtils.toStr(fieldValue));
			case "short_text6" -> setShortText6(ConvertUtils.toStr(fieldValue));
			case "short_text7" -> setShortText7(ConvertUtils.toStr(fieldValue));
			case "short_text8" -> setShortText8(ConvertUtils.toStr(fieldValue));
			case "short_text9" -> setShortText9(ConvertUtils.toStr(fieldValue));
			case "short_text10" -> setShortText10(ConvertUtils.toStr(fieldValue));
			case "short_text11" -> setShortText11(ConvertUtils.toStr(fieldValue));
			case "short_text12" -> setShortText12(ConvertUtils.toStr(fieldValue));
			case "short_text13" -> setShortText13(ConvertUtils.toStr(fieldValue));
			case "short_text14" -> setShortText14(ConvertUtils.toStr(fieldValue));
			case "short_text15" -> setShortText15(ConvertUtils.toStr(fieldValue));
			case "short_text16" -> setShortText16(ConvertUtils.toStr(fieldValue));
			case "short_text17" -> setShortText17(ConvertUtils.toStr(fieldValue));
			case "short_text18" -> setShortText18(ConvertUtils.toStr(fieldValue));
			case "short_text19" -> setShortText19(ConvertUtils.toStr(fieldValue));
			case "short_text20" -> setShortText20(ConvertUtils.toStr(fieldValue));
			case "short_text21" -> setShortText21(ConvertUtils.toStr(fieldValue));
			case "short_text22" -> setShortText22(ConvertUtils.toStr(fieldValue));
			case "short_text23" -> setShortText23(ConvertUtils.toStr(fieldValue));
			case "short_text24" -> setShortText24(ConvertUtils.toStr(fieldValue));
			case "short_text25" -> setShortText25(ConvertUtils.toStr(fieldValue));
			case "medium_text1" -> setMediumText1(ConvertUtils.toStr(fieldValue));
			case "medium_text2" -> setMediumText2(ConvertUtils.toStr(fieldValue));
			case "medium_text3" -> setMediumText3(ConvertUtils.toStr(fieldValue));
			case "medium_text4" -> setMediumText4(ConvertUtils.toStr(fieldValue));
			case "medium_text5" -> setMediumText5(ConvertUtils.toStr(fieldValue));
			case "medium_text6" -> setMediumText6(ConvertUtils.toStr(fieldValue));
			case "medium_text7" -> setMediumText7(ConvertUtils.toStr(fieldValue));
			case "medium_text8" -> setMediumText8(ConvertUtils.toStr(fieldValue));
			case "medium_text9" -> setMediumText9(ConvertUtils.toStr(fieldValue));
			case "medium_text10" -> setMediumText10(ConvertUtils.toStr(fieldValue));
			case "medium_text11" -> setMediumText11(ConvertUtils.toStr(fieldValue));
			case "medium_text12" -> setMediumText12(ConvertUtils.toStr(fieldValue));
			case "medium_text13" -> setMediumText13(ConvertUtils.toStr(fieldValue));
			case "medium_text14" -> setMediumText14(ConvertUtils.toStr(fieldValue));
			case "medium_text15" -> setMediumText15(ConvertUtils.toStr(fieldValue));
			case "medium_text16" -> setMediumText16(ConvertUtils.toStr(fieldValue));
			case "medium_text17" -> setMediumText17(ConvertUtils.toStr(fieldValue));
			case "medium_text18" -> setMediumText18(ConvertUtils.toStr(fieldValue));
			case "medium_text19" -> setMediumText19(ConvertUtils.toStr(fieldValue));
			case "medium_text20" -> setMediumText20(ConvertUtils.toStr(fieldValue));
			case "medium_text21" -> setMediumText21(ConvertUtils.toStr(fieldValue));
			case "medium_text22" -> setMediumText22(ConvertUtils.toStr(fieldValue));
			case "medium_text23" -> setMediumText23(ConvertUtils.toStr(fieldValue));
			case "medium_text24" -> setMediumText24(ConvertUtils.toStr(fieldValue));
			case "medium_text25" -> setMediumText25(ConvertUtils.toStr(fieldValue));
			case "large_text1" -> setLargeText1(ConvertUtils.toStr(fieldValue));
			case "large_text2" -> setLargeText2(ConvertUtils.toStr(fieldValue));
			case "large_text3" -> setLargeText3(ConvertUtils.toStr(fieldValue));
			case "large_text4" -> setLargeText4(ConvertUtils.toStr(fieldValue));
			case "clob_text1" -> setClobText1(ConvertUtils.toStr(fieldValue));
			case "long1" -> setLong1(ConvertUtils.toLong(fieldValue));
			case "long2" -> setLong2(ConvertUtils.toLong(fieldValue));
			case "long3" -> setLong3(ConvertUtils.toLong(fieldValue));
			case "long4" -> setLong4(ConvertUtils.toLong(fieldValue));
			case "long5" -> setLong5(ConvertUtils.toLong(fieldValue));
			case "long6" -> setLong6(ConvertUtils.toLong(fieldValue));
			case "long7" -> setLong7(ConvertUtils.toLong(fieldValue));
			case "long8" -> setLong8(ConvertUtils.toLong(fieldValue));
			case "long9" -> setLong9(ConvertUtils.toLong(fieldValue));
			case "long10" -> setLong10(ConvertUtils.toLong(fieldValue));
			case "double1" -> setDouble1(ConvertUtils.toDouble(fieldValue));
			case "double2" -> setDouble2(ConvertUtils.toDouble(fieldValue));
			case "double3" -> setDouble3(ConvertUtils.toDouble(fieldValue));
			case "double4" -> setDouble4(ConvertUtils.toDouble(fieldValue));
			case "double5" -> setDouble5(ConvertUtils.toDouble(fieldValue));
			case "double6" -> setDouble6(ConvertUtils.toDouble(fieldValue));
			case "double7" -> setDouble7(ConvertUtils.toDouble(fieldValue));
			case "double8" -> setDouble8(ConvertUtils.toDouble(fieldValue));
			case "double9" -> setDouble9(ConvertUtils.toDouble(fieldValue));
			case "double10" -> setDouble10(ConvertUtils.toDouble(fieldValue));
			case "datetime1" -> setDatetime1(ConvertUtils.toLocalDateTime(fieldValue));
			case "datetime2" -> setDatetime2(ConvertUtils.toLocalDateTime(fieldValue));
			case "datetime3" -> setDatetime3(ConvertUtils.toLocalDateTime(fieldValue));
			case "datetime4" -> setDatetime4(ConvertUtils.toLocalDateTime(fieldValue));
			case "datetime5" -> setDatetime5(ConvertUtils.toLocalDateTime(fieldValue));
			case "datetime6" -> setDatetime6(ConvertUtils.toLocalDateTime(fieldValue));
			case "datetime7" -> setDatetime7(ConvertUtils.toLocalDateTime(fieldValue));
			case "datetime8" -> setDatetime8(ConvertUtils.toLocalDateTime(fieldValue));
			case "datetime9" -> setDatetime9(ConvertUtils.toLocalDateTime(fieldValue));
			case "datetime10" -> setDatetime10(ConvertUtils.toLocalDateTime(fieldValue));
			default -> throw new RuntimeException("Unknown field name: " + fieldName);
		}
	}
}
