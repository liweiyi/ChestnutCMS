package com.chestnut.vote.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

import lombok.Getter;
import lombok.Setter;

/**
 * 问卷调查日志表
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(value = VoteLog.TABLE_NAME, autoResultMap = true)
public class VoteLog implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "cc_vote_log";

	@TableId(value = "log_id", type = IdType.AUTO)
	private Long logId;

	/**
	 * 关联问卷调查表ID
	 */
	private Long voteId;
	
	/**
	 * 参与人类型
	 */
	private String userType;
	
	/**
	 * 参与人ID
	 */
	private String userId;

	/**
	 * 投票结果
	 * 
	 * 格式：<subjectId, itemId|inputText>
	 */
	@TableField(typeHandler = JacksonTypeHandler.class)
	private Map<Long, String> result;
	
	/**
	 * 日志记录时间
	 */
	private LocalDateTime logTime;
	
	/**
	 * IP
	 */
	private String ip;
	
	/**
	 * UserAgent
	 */
	private String userAgent;
}
