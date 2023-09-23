package com.chestnut.word.service;

import java.util.Map;
import java.util.Set;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.word.domain.ErrorProneWord;

public interface IErrorProneWordService extends IService<ErrorProneWord> {

	/**
	 * 查找置顶文本中的易错词
	 * 
	 * @param text
	 * @return
	 */
	Map<String, String> findErrorProneWords(String text);

	/**
	 * 易错词替换
	 * 
	 * @param str
	 * @return
	 */
	String replaceErrorProneWords(String text);

	/**
	 * 易错词集合
	 * 
	 * @return
	 */
	Map<String, String> getErrorProneWords();

	/**
	 * 添加易错词
	 * 
	 * @param errorProneWord
	 */
	void addErrorProneWord(ErrorProneWord errorProneWord);

	/**
	 * 修改易错词
	 * 
	 * @param errorProneWord
	 */
	void updateErrorProneWord(ErrorProneWord errorProneWord);

	/**
	 * 查找指定文本内容中的易错词
	 *
	 * @param text
	 * @return
	 */
    Map<String, String> check(String text);
}