/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chestnut.word.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.word.WordConstants;
import com.chestnut.word.cache.HotWordMonitoredCache;
import com.chestnut.word.domain.HotWord;
import com.chestnut.word.domain.HotWordGroup;
import com.chestnut.word.mapper.HotWordGroupMapper;
import com.chestnut.word.mapper.HotWordMapper;
import com.chestnut.word.service.IHotWordService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotWordServiceImpl extends ServiceImpl<HotWordMapper, HotWord> implements IHotWordService {

	private final HotWordMonitoredCache hotWordCache;

	private final RedissonClient redissonClient;

	private final HotWordGroupMapper hotWordGroupMapper;

	@Override
	public Map<String, HotWordMonitoredCache.HotWordCache> getHotWords(String owner, String groupCode) {
		return this.hotWordCache.getCache(groupCode,
				() -> {
					Optional<HotWordGroup> groupOpt = new LambdaQueryChainWrapper<>(this.hotWordGroupMapper)
							.eq(HotWordGroup::getOwner, owner)
							.eq(HotWordGroup::getCode, groupCode).oneOpt();
					return groupOpt.map(hotWordGroup -> this.lambdaQuery()
									.eq(HotWord::getGroupId, hotWordGroup.getGroupId())
									.list().stream()
									.collect(Collectors.toMap(HotWord::getWord, w ->
										new HotWordMonitoredCache.HotWordCache(w.getWord(), w.getUrl(), w.getUrlTarget())
									))
					).orElseGet(Map::of);
				});
	}

	@Override
	public String replaceHotWords(String text, String owner, String[] groupCodes, String target, String replacementTemplate) {
		if (Objects.isNull(groupCodes) || groupCodes.length == 0) {
			return text;
		}
		if (StringUtils.isEmpty(replacementTemplate)) {
			replacementTemplate = WordConstants.HOT_WORD_REPLACEMENT;
		}
		for (String groupCode : groupCodes) {
			Map<String, HotWordMonitoredCache.HotWordCache> hotWords = this.getHotWords(owner, groupCode);
            for (Entry<String, HotWordMonitoredCache.HotWordCache> e : hotWords.entrySet()) {
                if (StringUtils.isEmpty(target)) {
                    target = e.getValue().getTarget();
                }
                String replacement = StringUtils.messageFormat(replacementTemplate, e.getValue().getUrl(),
                        e.getValue().getWord(), target);
                text = StringUtils.replaceEx(text, e.getKey(), replacement);
            }
		}
		return text;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void addHotWord(HotWord hotWord) {
		RLock lock = redissonClient.getLock("HotWord");
		lock.lock();
		try {
			boolean checkUnique = checkUnique(hotWord.getGroupId(), null, hotWord.getWord());
			Assert.isTrue(checkUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("word"));

			HotWordGroup tagWordGroup = hotWordGroupMapper.selectById(hotWord.getGroupId());
			Assert.notNull(tagWordGroup, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("groupId", hotWord.getGroupId()));

			hotWord.setWordId(IdUtils.getSnowflakeId());
			hotWord.setOwner(tagWordGroup.getOwner());
			this.save(hotWord);

			tagWordGroup.setWordTotal(tagWordGroup.getWordTotal() + 1);
			hotWordGroupMapper.updateById(tagWordGroup);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void editHotWord(HotWord hotWord) {
		HotWord dbWord = this.getById(hotWord.getWordId());
		Assert.notNull(dbWord, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("wordId", hotWord.getWordId()));

		boolean checkUnique = checkUnique(hotWord.getGroupId(), hotWord.getWordId(), hotWord.getWord());
		Assert.isTrue(checkUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("word"));

		dbWord.setWord(hotWord.getWord());
		dbWord.setRemark(hotWord.getRemark());
		dbWord.updateBy(hotWord.getUpdateBy());
		this.updateById(hotWord);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteHotWords(List<Long> hotWordIds) {
		RLock lock = redissonClient.getLock("HotWord");
		lock.lock();
		try {
			List<HotWord> hotWords = this.listByIds(hotWordIds);
			Map<Long, Integer> groupWordDecrease = new HashMap<>();
			hotWords.forEach(tag -> {
				groupWordDecrease.put(tag.getGroupId(), groupWordDecrease.getOrDefault(tag.getGroupId(), 0) + 1);
			});
			this.removeByIds(hotWordIds);
			groupWordDecrease.forEach((k, v) -> {
				HotWordGroup group = hotWordGroupMapper.selectById(k);
				group.setWordTotal(group.getWordTotal() - v);
				hotWordGroupMapper.updateById(group);
			});
		} finally {
			lock.unlock();
		}
	}

	private boolean checkUnique(Long groupId, Long wordId, String word) {
		return this.lambdaQuery()
				.eq(HotWord::getGroupId, groupId)
				.ne(wordId != null && wordId > 0, HotWord::getWordId, wordId)
				.eq(HotWord::getWord, word)
				.count() == 0;
	}
}
