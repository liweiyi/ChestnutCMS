package com.chestnut.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Getter;
import lombok.Setter;

/**
 * 用户和岗位关联 sys_user_post
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(SysUserPost.TABLE_NAME)
public class SysUserPost {

	public static final String TABLE_NAME = "sys_user_post";

	/** 用户ID */
	private Long userId;

	/** 岗位ID */
	private Long postId;
}
