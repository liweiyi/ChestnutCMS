package com.chestnut.comment.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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

	private static final long serialVersionUID = 1L;
	
	public static final String TABLE_NAME = "cc_comment";

	@TableId(value = "comment_id", type = IdType.INPUT)
    private Long commentId;
	
	/**
	 * 评论用户ID
	 */
	private Long uid;
	
	/**
	 * 父级评论ID，存在parentId表示这是一条回复
	 */
	private Long parentId;
	
	/**
	 * 如果是回复类型，且回复的也是回复类型评论，则需要设置回复的用户ID
	 */
	private Long replyUid;
	
	/**
	 * 回复数
	 */
	private int replyCount;
	
	/**
	 * 来源分类，例如：cms:site_id
	 */
	private String sourceType;
	
	/**
	 * 来源唯一标识
	 */
	private String sourceId;

	@TableField(exist = false)
	private String sourceTitle;

	@TableField(exist = false)
	private String sourceUrl;
	
	/**
	 * 评论内容
	 */
	private String content;
	
	/**
	 * 点赞数
	 */
	private Integer likeCount;
	
	/**
	 * 评论审核状态（0=未审核，1=审核通过，2=审核未通过）
	 */
	private Integer auditStatus;
	
	/**
	 * 评论时间
	 */
	private LocalDateTime commentTime;

	/**
	 * 删除标识
	 */
	private Integer delFlag;
	
	/**
	 * IP
	 */
	private String ip;
	
	/**
	 * 属地
	 */
	private String location;
	
	/**
	 * 客户端类型
	 */
	private String clientType;
	
	/**
	 * UA
	 */
	@JsonIgnore
	private String userAgent;
	
	/**
	 * 回复列表
	 */
	@TableField(exist = false)
	private List<Comment> replyList;
}
