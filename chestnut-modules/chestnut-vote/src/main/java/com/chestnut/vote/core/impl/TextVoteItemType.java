package com.chestnut.vote.core.impl;

import org.springframework.stereotype.Component;

import com.chestnut.vote.core.IVoteItemType;

@Component(IVoteItemType.BEAN_PREFIX + TextVoteItemType.ID)
public class TextVoteItemType implements IVoteItemType {

	public static final String ID = "Text";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "{VOTE.ITEM_TYPE.TEXT}";
	}
}
