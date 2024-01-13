package com.chestnut.word.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.word.domain.HotWord;
import org.springframework.transaction.annotation.Transactional;

public interface IHotWordService extends IService<HotWord> {

	@Transactional(rollbackFor = Exception.class)
	void addHotWord(HotWord hotWord);

	void editHotWord(HotWord hotWord);

	@Transactional(rollbackFor = Exception.class)
    void deleteHotWords(List<Long> hotWordIds);

    /**
	 * 缓存热词对象
	 */
	public record HotWordCache(String word, String url, String target) {
		
	}

	/**
	 * 获取指定分组热词集合
	 * 
	 * @param groupCode
	 * @return
	 */
	Map<String, HotWordCache> getHotWords(String groupCode);

	/**
	 * 按指定热词分组处理内容中的热词
	 * 
	 * @param text
	 * @param groupCodes
	 * @param target
	 * @param replacementTemplate
	 * @return
	 */
	String replaceHotWords(String text, String[] groupCodes, String target, String replacementTemplate);
}
