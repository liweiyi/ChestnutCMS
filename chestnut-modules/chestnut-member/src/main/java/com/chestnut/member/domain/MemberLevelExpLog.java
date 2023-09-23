package com.chestnut.member.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Getter;
import lombok.Setter;

/**
 * 会员经验日志表
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(MemberLevelExpLog.TABLE_NAME)
public class MemberLevelExpLog implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String TABLE_NAME = "cc_member_exp_log";

	@TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;
	
	/**
	 * 会员ID
	 */
	private Long memberId;
	
	/**
	 * 操作项类型
	 */
	private String opType;
	
	/**
	 * 操作项关联等级类型
	 */
	private String levelType;
	
	/**
	 * 获得/减少经验值
	 */
	private Integer changeExp;
	
	/**
	 * 变更后等级
	 */
	private Integer level;
	
	/**
	 * 变更后经验值
	 */
	private Long exp;
	
	/**
	 * 日志时间
	 */
	private LocalDateTime logTime;
}
