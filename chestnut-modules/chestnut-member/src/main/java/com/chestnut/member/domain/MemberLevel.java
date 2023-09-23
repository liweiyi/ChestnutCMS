package com.chestnut.member.domain;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Getter;
import lombok.Setter;

/***
 * 会员等级经验数据表
 */
@Getter
@Setter
@TableName(MemberLevel.TABLE_NAME)
public class MemberLevel implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String TABLE_NAME = "cc_member_level";

	@TableId(value = "data_id", type = IdType.INPUT)
    private Long dataId;
	
	/**
	 * 会员ID
	 */
	private Long memberId;
	
	/**
	 * 等级类型
	 */
	private String levelType;
	
	/**
	 * 当前等级
	 */
	private Integer level;
	
	/**
	 * 当前经验值
	 */
	private Long exp;
}
