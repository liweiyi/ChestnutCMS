package com.chestnut.word.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.word.domain.TagWord;

public interface ITagWordService extends IService<TagWord> {

	/**
	 * 添加TAG词
	 * 
	 * @param tagWord
	 * @return
	 */
	void addTagWord(TagWord tagWord);

	/**
	 * 编辑TAG词
	 * 
	 * @param tagWord
	 * @return
	 */
	void editTagWord(TagWord tagWord);

	/**
	 * 删除TAG词
	 * 
	 * @param tagWordIds
	 * @return
	 */
	void deleteTagWords(List<Long> tagWordIds);
}
