package com.chestnut.member.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/***
 * 会员关注数据表
 */
@Getter
@Setter
@TableName(MemberFollow.TABLE_NAME)
public class MemberFollow implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String TABLE_NAME = "cc_member_follow";

	@TableId(value = "log_id", type = IdType.INPUT)
    private Long logId;
	
	/**
	 * 会员ID
	 */
	private Long memberId;
	
	/**
	 * 关注会员ID
	 */
	private Long followMemberId;
}
