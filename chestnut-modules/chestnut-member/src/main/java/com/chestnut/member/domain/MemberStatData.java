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
package com.chestnut.member.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/***
 * 会员相关数据聚合统计表
 */
@Getter
@Setter
@TableName(MemberStatData.TABLE_NAME)
public class MemberStatData implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String TABLE_NAME = "cc_member_stat_data";

	@TableId(value = "member_id", type = IdType.INPUT)
    private Long memberId;

	private Integer intValue1;

	private Integer intValue2;

	private Integer intValue3;

	private Integer intValue4;

	private Integer intValue5;

	private Integer intValue6;

	private Integer intValue7;

	private Integer intValue8;

	private Integer intValue9;

	private Integer intValue10;

	private Integer intValue11;

	private Integer intValue12;

	private Integer intValue13;

	private Integer intValue14;

	private Integer intValue15;

	private Integer intValue16;

	private Integer intValue17;

	private Integer intValue18;

	private Integer intValue19;

	private Integer intValue20;

	public void setValue(String field, Integer value) {
		switch (field) {
			case "intValue1" -> this.intValue1 = value;
			case "intValue2" -> this.intValue2 = value;
			case "intValue3" -> this.intValue3 = value;
			case "intValue4" -> this.intValue4 = value;
			case "intValue5" -> this.intValue5 = value;
			case "intValue6" -> this.intValue6 = value;
			case "intValue7" -> this.intValue7 = value;
			case "intValue8" -> this.intValue8 = value;
			case "intValue9" -> this.intValue9 = value;
			case "intValue10" -> this.intValue10 = value;
			case "intValue11" -> this.intValue11 = value;
			case "intValue12" -> this.intValue12 = value;
			case "intValue13" -> this.intValue13 = value;
			case "intValue14" -> this.intValue14 = value;
			case "intValue15" -> this.intValue15 = value;
			case "intValue16" -> this.intValue16 = value;
			case "intValue17" -> this.intValue17 = value;
			case "intValue18" -> this.intValue18 = value;
			case "intValue19" -> this.intValue19 = value;
			case "intValue20" -> this.intValue20 = value;
			default -> throw new IllegalStateException("Unexpected value: " + field);
		}
	}

	public Integer getValue(String field) {
		return switch (field) {
			case "intValue1" -> this.intValue1;
			case "intValue2" -> this.intValue2;
			case "intValue3" -> this.intValue3;
			case "intValue4" -> this.intValue4;
			case "intValue5" -> this.intValue5;
			case "intValue6" -> this.intValue6;
			case "intValue7" -> this.intValue7;
			case "intValue8" -> this.intValue8;
			case "intValue9" -> this.intValue9;
			case "intValue10" -> this.intValue10;
			case "intValue11" -> this.intValue11;
			case "intValue12" -> this.intValue12;
			case "intValue13" -> this.intValue13;
			case "intValue14" -> this.intValue14;
			case "intValue15" -> this.intValue15;
			case "intValue16" -> this.intValue16;
			case "intValue17" -> this.intValue17;
			case "intValue18" -> this.intValue18;
			case "intValue19" -> this.intValue19;
			case "intValue20" -> this.intValue20;
			default -> throw new IllegalStateException("Unexpected value: " + field);
		};
	}
}
