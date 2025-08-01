/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
package com.chestnut.comment.domain;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.annotation.XComment;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.chestnut.comment.CommentConsts;

import com.chestnut.common.db.DBConstants;
import lombok.Getter;
import lombok.Setter;

/**
 * 评论表
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(value = Comment.TABLE_NAME)
public class Comment implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;
	
	public static final String TABLE_NAME = "cc_comment";

	@XComment("{CMS.COMMENT.ID}")
	@TableId(value = "comment_id", type = IdType.INPUT)
    private Long commentId;
	
	/**
	 * 评论用户ID
	 */
	@XComment("{CMS.COMMENT.UID}")
	private Long uid;
	
	/**
	 * 父级评论ID，存在parentId表示这是一条回复
	 */
	@XComment("{CMS.COMMENT.PARENT_ID}")
	private Long parentId;
	
	/**
	 * 如果是回复类型，且回复的也是回复类型评论，则需要设置回复的用户ID
	 */
	@XComment("{CMS.COMMENT.REPLY_UID}")
	private Long replyUid;
	
	/**
	 * 回复数
	 */
	@XComment("{CMS.COMMENT.REPLY_COUNT}")
	private int replyCount;
	
	/**
	 * 来源分类，例如：cms:site_id
	 */
	@XComment("{CMS.COMMENT.SOURCE_TYPE}")
	private String sourceType;
	
	/**
	 * 来源唯一标识
	 */
	@XComment("{CMS.COMMENT.SOURCE_ID}")
	private String sourceId;

	@TableField(exist = false)
	@XComment("{CMS.COMMENT.SOURCE_TITLE}")
	private String sourceTitle;

	@TableField(exist = false)
	@XComment("{CMS.COMMENT.SOURCE_URL}")
	private String sourceUrl;
	
	/**
	 * 评论内容
	 */
	@XComment("{CMS.COMMENT.CONTENT}")
	private String content;
	
	/**
	 * 点赞数
	 */
	@XComment("{CMS.COMMENT.LIKE_COUNT}")
	private Integer likeCount;
	
	/**
	 * 评论审核状态（0=未审核，1=审核通过，2=审核未通过）
	 */
	@XComment("{CMS.COMMENT.AUDIT_STATUS}")
	private Integer auditStatus;
	
	/**
	 * 评论时间
	 */
	@XComment("{CMS.COMMENT.COMMENT_TIME}")
	private LocalDateTime commentTime;

	/**
	 * 删除标识
	 */
	@XComment("{CMS.COMMENT.DEL_FLAG}")
	private Integer delFlag;
	
	/**
	 * IP
	 */
	@XComment("{CMS.COMMENT.IP}")
	private String ip;
	
	/**
	 * 属地
	 */
	@XComment("{CMS.COMMENT.REGION}")
	private String location;
	
	/**
	 * 客户端类型
	 */
	@XComment("{CMS.COMMENT.CLIENT_TYPE}")
	private String clientType;
	
	/**
	 * UA
	 */
	@JsonIgnore
	@XComment("{CMS.COMMENT.USER_AGENT}")
	private String userAgent;
	
	/**
	 * 回复列表
	 */
	@TableField(exist = false)
	@XComment("{CMS.COMMENT.REPLY_LIST}")
	private List<Comment> replyList;
}
