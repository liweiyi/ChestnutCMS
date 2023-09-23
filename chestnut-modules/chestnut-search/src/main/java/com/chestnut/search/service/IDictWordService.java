package com.chestnut.search.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.search.domain.DictWord;
import com.chestnut.search.domain.dto.DictWordDTO;

public interface IDictWordService extends IService<DictWord> {

	/**
	 * 词库变更标识
	 */
	String getLastModified(String wordType);

	/**
	 * 批量导入词库新词
	 * 
	 * @param words
	 * @return
	 */
	void batchAddDictWords(DictWordDTO dto);

	/**
	 * 删除词库新词
	 * 
	 * @param dictWordIds
	 * @return
	 */
	void deleteDictWord(List<Long> dictWordIds);
}
