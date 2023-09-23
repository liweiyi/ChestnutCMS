package com.chestnut.member.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName(MemberLike.TABLE_NAME)
public class MemberLike implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public final static String TABLE_NAME = "cc_member_like";

	@TableId(value = "log_id", type = IdType.AUTO)
	private Long logId;

	/**
	 * 会员ID
	 */
	private Long memberId;

	/**
	 * 点赞数据类型
	 */
	private String dataType;
	
	/**
	 * 点赞数据ID
	 */
	private Long dataId;

	/**
	 * 点赞时间
	 */
	private LocalDateTime createTime;
}
