package com.chestnut.xmodel.core;

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

	private String ShortText1;

	private String ShortText2;

	private String ShortText3;

	private String ShortText4;

	private String ShortText5;

	private String ShortText6;

	private String ShortText7;

	private String ShortText8;

	private String ShortText9;

	private String ShortText10;

	private String ShortText11;

	private String ShortText12;

	private String ShortText13;

	private String ShortText14;

	private String ShortText15;

	private String ShortText16;

	private String ShortText17;

	private String ShortText18;

	private String ShortText19;

	private String ShortText20;

	private String ShortText21;

	private String ShortText22;

	private String ShortText23;

	private String ShortText24;

	private String ShortText25;

	private String MediumText1;

	private String MediumText2;

	private String MediumText3;

	private String MediumText4;

	private String MediumText5;

	private String MediumText6;

	private String MediumText7;

	private String MediumText8;

	private String MediumText9;

	private String MediumText10;

	private String MediumText11;

	private String MediumText12;

	private String MediumText13;

	private String MediumText14;

	private String MediumText15;

	private String MediumText16;

	private String MediumText17;

	private String MediumText18;

	private String MediumText19;

	private String MediumText20;

	private String MediumText21;

	private String MediumText22;

	private String MediumText23;

	private String MediumText24;

	private String MediumText25;

	private String LargeText1;

	private String LargeText2;

	private String LargeText3;

	private String LargeText4;

	private String ClobText1;

	private Long Long1;

	private Long Long2;

	private Long Long3;

	private Long Long4;

	private Long Long5;

	private Long Long6;

	private Long Long7;

	private Long Long8;

	private Long Long9;

	private Long Long10;

	private Double Double1;

	private Double Double2;

	private Double Double3;

	private Double Double4;

	private Double Double5;

	private Double Double6;

	private Double Double7;

	private Double Double8;

	private Double Double9;

	private Double Double10;

	private LocalDateTime Date1;

	private LocalDateTime Date2;

	private LocalDateTime Date3;

	private LocalDateTime Date4;

	private LocalDateTime Date5;

	private LocalDateTime Date6;

	private LocalDateTime Date7;

	private LocalDateTime Date8;

	private LocalDateTime Date9;

	private LocalDateTime Date10;

	public void setFieldValue(String fieldName, Object fieldValue) {
		if ("short_text1".equals(fieldName)) {
			this.setShortText1(fieldValue == null ? null : fieldValue.toString());
		} else if ("short_text2".equals(fieldName)) {
			this.setShortText2(fieldValue == null ? null : fieldValue.toString());
		} else if ("short_text3".equals(fieldName)) {
			this.setShortText3(fieldValue == null ? null : fieldValue.toString());
		} else if ("short_text4".equals(fieldName)) {
			this.setShortText4(fieldValue == null ? null : fieldValue.toString());
		} else if ("short_text5".equals(fieldName)) {
			this.setShortText5(fieldValue == null ? null : fieldValue.toString());
		} else if ("short_text6".equals(fieldName)) {
			this.setShortText6(fieldValue == null ? null : fieldValue.toString());
		} else if ("short_text7".equals(fieldName)) {
			this.setShortText7(fieldValue == null ? null : fieldValue.toString());
		} else if ("short_text8".equals(fieldName)) {
			this.setShortText8(fieldValue == null ? null : fieldValue.toString());
		} else if ("short_text9".equals(fieldName)) {
			this.setShortText9(fieldValue == null ? null : fieldValue.toString());
		} else if ("short_text10".equals(fieldName)) {
			this.setShortText10(fieldValue == null ? null : fieldValue.toString());
		} else if ("short_text11".equals(fieldName)) {
			this.setShortText11(fieldValue == null ? null : fieldValue.toString());
		} else if ("short_text12".equals(fieldName)) {
			this.setShortText12(fieldValue == null ? null : fieldValue.toString());
		} else if ("short_text13".equals(fieldName)) {
			this.setShortText13(fieldValue == null ? null : fieldValue.toString());
		} else if ("short_text14".equals(fieldName)) {
			this.setShortText14(fieldValue == null ? null : fieldValue.toString());
		} else if ("short_text15".equals(fieldName)) {
			this.setShortText15(fieldValue == null ? null : fieldValue.toString());
		} else if ("short_text16".equals(fieldName)) {
			this.setShortText16(fieldValue == null ? null : fieldValue.toString());
		} else if ("short_text17".equals(fieldName)) {
			this.setShortText17(fieldValue == null ? null : fieldValue.toString());
		} else if ("short_text18".equals(fieldName)) {
			this.setShortText18(fieldValue == null ? null : fieldValue.toString());
		} else if ("short_text19".equals(fieldName)) {
			this.setShortText19(fieldValue == null ? null : fieldValue.toString());
		} else if ("short_text20".equals(fieldName)) {
			this.setShortText20(fieldValue == null ? null : fieldValue.toString());
		} else if ("short_text21".equals(fieldName)) {
			this.setShortText21(fieldValue == null ? null : fieldValue.toString());
		} else if ("short_text22".equals(fieldName)) {
			this.setShortText22(fieldValue == null ? null : fieldValue.toString());
		} else if ("short_text23".equals(fieldName)) {
			this.setShortText23(fieldValue == null ? null : fieldValue.toString());
		} else if ("short_text24".equals(fieldName)) {
			this.setShortText24(fieldValue == null ? null : fieldValue.toString());
		} else if ("short_text25".equals(fieldName)) {
			this.setShortText25(fieldValue == null ? null : fieldValue.toString());
		} else if ("short_text25".equals(fieldName)) {
			this.setShortText25(fieldValue == null ? null : fieldValue.toString());
		} else if ("medium_text1".equals(fieldName)) {
			this.setMediumText1(fieldValue == null ? null : fieldValue.toString());
		} else if ("medium_text2".equals(fieldName)) {
			this.setMediumText2(fieldValue == null ? null : fieldValue.toString());
		} else if ("medium_text3".equals(fieldName)) {
			this.setMediumText3(fieldValue == null ? null : fieldValue.toString());
		} else if ("medium_text4".equals(fieldName)) {
			this.setMediumText4(fieldValue == null ? null : fieldValue.toString());
		} else if ("medium_text5".equals(fieldName)) {
			this.setMediumText5(fieldValue == null ? null : fieldValue.toString());
		} else if ("medium_text6".equals(fieldName)) {
			this.setMediumText6(fieldValue == null ? null : fieldValue.toString());
		} else if ("medium_text7".equals(fieldName)) {
			this.setMediumText7(fieldValue == null ? null : fieldValue.toString());
		} else if ("medium_text8".equals(fieldName)) {
			this.setMediumText8(fieldValue == null ? null : fieldValue.toString());
		} else if ("medium_text9".equals(fieldName)) {
			this.setMediumText9(fieldValue == null ? null : fieldValue.toString());
		} else if ("medium_text10".equals(fieldName)) {
			this.setMediumText10(fieldValue == null ? null : fieldValue.toString());
		} else if ("medium_text11".equals(fieldName)) {
			this.setMediumText11(fieldValue == null ? null : fieldValue.toString());
		} else if ("medium_text12".equals(fieldName)) {
			this.setMediumText12(fieldValue == null ? null : fieldValue.toString());
		} else if ("medium_text13".equals(fieldName)) {
			this.setMediumText13(fieldValue == null ? null : fieldValue.toString());
		} else if ("medium_text14".equals(fieldName)) {
			this.setMediumText14(fieldValue == null ? null : fieldValue.toString());
		} else if ("medium_text15".equals(fieldName)) {
			this.setMediumText15(fieldValue == null ? null : fieldValue.toString());
		} else if ("medium_text16".equals(fieldName)) {
			this.setMediumText16(fieldValue == null ? null : fieldValue.toString());
		} else if ("medium_text17".equals(fieldName)) {
			this.setMediumText17(fieldValue == null ? null : fieldValue.toString());
		} else if ("medium_text18".equals(fieldName)) {
			this.setMediumText18(fieldValue == null ? null : fieldValue.toString());
		} else if ("medium_text19".equals(fieldName)) {
			this.setMediumText19(fieldValue == null ? null : fieldValue.toString());
		} else if ("medium_text20".equals(fieldName)) {
			this.setMediumText20(fieldValue == null ? null : fieldValue.toString());
		} else if ("medium_text21".equals(fieldName)) {
			this.setMediumText21(fieldValue == null ? null : fieldValue.toString());
		} else if ("medium_text22".equals(fieldName)) {
			this.setMediumText22(fieldValue == null ? null : fieldValue.toString());
		} else if ("medium_text23".equals(fieldName)) {
			this.setMediumText23(fieldValue == null ? null : fieldValue.toString());
		} else if ("medium_text24".equals(fieldName)) {
			this.setMediumText24(fieldValue == null ? null : fieldValue.toString());
		} else if ("medium_text25".equals(fieldName)) {
			this.setMediumText25(fieldValue == null ? null : fieldValue.toString());
		} else if ("large_text1".equals(fieldName)) {
			this.setLargeText1(fieldValue == null ? null : fieldValue.toString());
		} else if ("large_text2".equals(fieldName)) {
			this.setLargeText2(fieldValue == null ? null : fieldValue.toString());
		} else if ("large_text3".equals(fieldName)) {
			this.setLargeText3(fieldValue == null ? null : fieldValue.toString());
		} else if ("large_text4".equals(fieldName)) {
			this.setLargeText4(fieldValue == null ? null : fieldValue.toString());
		} else if ("clob_text1".equals(fieldName)) {
			this.setClobText1(fieldValue == null ? null : fieldValue.toString());
		} else if ("long1".equals(fieldName)) {
			this.setLong1(fieldValue == null ? null : Long.valueOf(fieldValue.toString()));
		} else if ("long2".equals(fieldName)) {
			this.setLong2(fieldValue == null ? null : Long.valueOf(fieldValue.toString()));
		} else if ("long3".equals(fieldName)) {
			this.setLong3(fieldValue == null ? null : Long.valueOf(fieldValue.toString()));
		} else if ("long4".equals(fieldName)) {
			this.setLong4(fieldValue == null ? null : Long.valueOf(fieldValue.toString()));
		} else if ("long5".equals(fieldName)) {
			this.setLong5(fieldValue == null ? null : Long.valueOf(fieldValue.toString()));
		} else if ("long6".equals(fieldName)) {
			this.setLong6(fieldValue == null ? null : Long.valueOf(fieldValue.toString()));
		} else if ("long7".equals(fieldName)) {
			this.setLong7(fieldValue == null ? null : Long.valueOf(fieldValue.toString()));
		} else if ("long8".equals(fieldName)) {
			this.setLong8(fieldValue == null ? null : Long.valueOf(fieldValue.toString()));
		} else if ("long9".equals(fieldName)) {
			this.setLong9(fieldValue == null ? null : Long.valueOf(fieldValue.toString()));
		} else if ("long10".equals(fieldName)) {
			this.setLong10(fieldValue == null ? null : Long.valueOf(fieldValue.toString()));
		} else if ("double1".equals(fieldName)) {
			this.setDouble1(fieldValue == null ? null : Double.valueOf(fieldValue.toString()));
		} else if ("double2".equals(fieldName)) {
			this.setDouble2(fieldValue == null ? null : Double.valueOf(fieldValue.toString()));
		} else if ("double3".equals(fieldName)) {
			this.setDouble3(fieldValue == null ? null : Double.valueOf(fieldValue.toString()));
		} else if ("double4".equals(fieldName)) {
			this.setDouble4(fieldValue == null ? null : Double.valueOf(fieldValue.toString()));
		} else if ("double5".equals(fieldName)) {
			this.setDouble5(fieldValue == null ? null : Double.valueOf(fieldValue.toString()));
		} else if ("double6".equals(fieldName)) {
			this.setDouble6(fieldValue == null ? null : Double.valueOf(fieldValue.toString()));
		} else if ("double7".equals(fieldName)) {
			this.setDouble7(fieldValue == null ? null : Double.valueOf(fieldValue.toString()));
		} else if ("double8".equals(fieldName)) {
			this.setDouble8(fieldValue == null ? null : Double.valueOf(fieldValue.toString()));
		} else if ("double9".equals(fieldName)) {
			this.setDouble9(fieldValue == null ? null : Double.valueOf(fieldValue.toString()));
		} else if ("double10".equals(fieldName)) {
			this.setDouble10(fieldValue == null ? null : Double.valueOf(fieldValue.toString()));
		} else if ("date1".equals(fieldName)) {
			this.setDate1(fieldValue == null ? null : LocalDateTime.parse(fieldValue.toString()));
		} else if ("date2".equals(fieldName)) {
			this.setDate2(fieldValue == null ? null : LocalDateTime.parse(fieldValue.toString()));
		} else if ("date3".equals(fieldName)) {
			this.setDate3(fieldValue == null ? null : LocalDateTime.parse(fieldValue.toString()));
		} else if ("date4".equals(fieldName)) {
			this.setDate4(fieldValue == null ? null : LocalDateTime.parse(fieldValue.toString()));
		} else if ("date5".equals(fieldName)) {
			this.setDate5(fieldValue == null ? null : LocalDateTime.parse(fieldValue.toString()));
		} else if ("date6".equals(fieldName)) {
			this.setDate6(fieldValue == null ? null : LocalDateTime.parse(fieldValue.toString()));
		} else if ("date7".equals(fieldName)) {
			this.setDate7(fieldValue == null ? null : LocalDateTime.parse(fieldValue.toString()));
		} else if ("date8".equals(fieldName)) {
			this.setDate8(fieldValue == null ? null : LocalDateTime.parse(fieldValue.toString()));
		} else if ("date9".equals(fieldName)) {
			this.setDate9(fieldValue == null ? null : LocalDateTime.parse(fieldValue.toString()));
		} else if ("date10".equals(fieldName)) {
			this.setDate10(fieldValue == null ? null : LocalDateTime.parse(fieldValue.toString()));
		}
	}
}
