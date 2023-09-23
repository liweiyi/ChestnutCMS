package com.chestnut.word;

public interface WordConstants {

	/**
	 * 敏感词默认替换字符串
	 */
	public String SENSITIVE_WORD_REPLACEMENT = "*";
	
	/**
	 * 热词链接替换模板字符串
	 */
	public String HOT_WORD_REPLACEMENT = "<a class=\"hot-word\" href=\"{0}\" target=\"{2}\">{1}</a>";
}
