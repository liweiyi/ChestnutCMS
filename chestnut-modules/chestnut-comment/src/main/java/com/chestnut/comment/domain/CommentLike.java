package com.chestnut.comment.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Getter;
import lombok.Setter;

/**
 * 评论点赞记录表
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(value = CommentLike.TABLE_NAME)
public class CommentLike implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String TABLE_NAME = "cc_comment_like";

	@TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;
	
	/**
	 * 评论ID
	 */
	private Long commentId;
	
	/**
	 * 点赞用户ID
	 */
	private Long uid;
	
	/**
	 * 点赞时间
	 */
	private LocalDateTime likeTime;
}
