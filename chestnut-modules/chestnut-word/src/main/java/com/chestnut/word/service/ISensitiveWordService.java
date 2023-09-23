package com.chestnut.word.service;

import java.util.List;
import java.util.Set;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.word.domain.SensitiveWord;

public interface ISensitiveWordService extends IService<SensitiveWord> {
	
	/**
	 * 替换敏感词
	 * 
	 * @param text
	 * @param replaceStr
	 * @return
	 */
	String replaceSensitiveWords(String text, String replacement);

	/**
	 * 添加敏感词
	 * 
	 * @param word
	 * @return
	 */
	void addWord(SensitiveWord word);

	/**
	 * 编辑敏感词
	 * 
	 * @param sensitiveWord
	 * @return
	 */
	void editWord(SensitiveWord sensitiveWord);

	/**
	 * 删除敏感词
	 * 
	 * @param wordIds
	 * @return
	 */
	void deleteWord(List<Long> wordIds);

	/**
	 * 查找指定内容中的敏感词
	 *
	 * @param text
	 * @return
	 */
	Set<String> check(String text);
}
