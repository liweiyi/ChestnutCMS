package com.chestnut.comment.member;

import com.chestnut.member.level.IExpOperation;
import org.springframework.stereotype.Component;

/**
 * 等级经验操作项：评论
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IExpOperation.BEAN_PREFIX + CommentExpOperation.ID)
public class CommentExpOperation implements IExpOperation {
	
	public static final String ID = "Comment";
	
	private static final String NAME = "{MEMBER.EXP_OPERATION." + ID + "}";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return NAME;
	}
}
