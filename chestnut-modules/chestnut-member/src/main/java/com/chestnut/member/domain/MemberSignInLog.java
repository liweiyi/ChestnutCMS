package com.chestnut.member.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Getter;
import lombok.Setter;

/**
 * 会员签到日志表
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(MemberSignInLog.TABLE_NAME)
public class MemberSignInLog implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String TABLE_NAME = "cc_member_signin_log";

	@TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;
	
	/**
	 * 会员ID
	 */
	private Long memberId;
	
	/**
	 * 签到日期唯一标识，格式：yyyyMMdd
	 */
	private Integer signInKey;
	
	/**
	 * 签到时间
	 */
	private LocalDateTime logTime;
}
