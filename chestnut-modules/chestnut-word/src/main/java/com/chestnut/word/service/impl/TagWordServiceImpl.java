package com.chestnut.word.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.SortUtils;
import com.chestnut.word.domain.TagWord;
import com.chestnut.word.mapper.TagWordMapper;
import com.chestnut.word.service.ITagWordService;

@Service
public class TagWordServiceImpl extends ServiceImpl<TagWordMapper, TagWord> implements ITagWordService {

	@Override
	public void addTagWord(TagWord tagWord) {
		boolean checkUnique = checkUnique(tagWord.getGroupId(), null, tagWord.getWord());
		Assert.isTrue(checkUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("word"));
		
    	tagWord.setWordId(IdUtils.getSnowflakeId());
    	tagWord.setSortFlag(SortUtils.getDefaultSortValue());
    	this.save(tagWord);
	}

	@Override
	public void editTagWord(TagWord tagWord) {
		TagWord dbTagWord = this.getById(tagWord.getWordId());
		Assert.notNull(dbTagWord, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("wordId", tagWord.getWordId()));

		boolean checkUnique = checkUnique(tagWord.getGroupId(), tagWord.getWordId(), tagWord.getWord());
		Assert.isTrue(checkUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("word"));

		dbTagWord.setGroupId(tagWord.getGroupId());
		dbTagWord.setWord(tagWord.getWord());
		dbTagWord.setLogo(tagWord.getLogo());
		dbTagWord.setRemark(tagWord.getRemark());
		dbTagWord.updateBy(tagWord.getUpdateBy());
		this.updateById(tagWord);
    }
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteTagWords(List<Long> tagWordIds) {
		this.removeByIds(tagWordIds);
	}
	
	private boolean checkUnique(Long groupId, Long wordId, String word) {
		return this.lambdaQuery().eq(TagWord::getGroupId, groupId)
    			.ne(wordId != null && wordId > 0, TagWord::getWordId, wordId)
    			.eq(TagWord::getWord, word)
    			.count() == 0;
	}
}
