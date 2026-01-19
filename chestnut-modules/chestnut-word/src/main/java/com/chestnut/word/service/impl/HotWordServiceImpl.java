/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
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
import com.chestnut.word.domain.dto.CreateHotWordRequest;
import com.chestnut.word.domain.dto.UpdateHotWordRequest;
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
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotWordServiceImpl extends ServiceImpl<HotWordMapper, HotWord> implements IHotWordService {

	private static final String LOCK_HOT_WORD = "CC-HotWord";

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
	public String replaceHotWords(String text, String owner, String[] groupCodes, int replaceCount, String target, String replacementTemplate) {
		if (Objects.isNull(groupCodes) || groupCodes.length == 0) {
			return text;
		}
		if (StringUtils.isEmpty(replacementTemplate)) {
			replacementTemplate = WordConstants.HOT_WORD_REPLACEMENT;
		}
        // 先清理原热词链接
        text = WordConstants.HOT_WORD_LINK_PATTERN.matcher(text).replaceAll("$1");
        // 查找<a>标签替换占位符
        Map<String, String> linkMap = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        Matcher matcher = WordConstants.LINK_PATTERN.matcher(text);
        int index = 0;
        while (matcher.find()) {
            String tagStr = matcher.group();
            String placeholder = "{{{" + IdUtils.simpleUUID() + "}}}";
            linkMap.put(placeholder, tagStr);
            sb.append(text, index, matcher.start()).append(placeholder);
            index = matcher.end();
        }
        sb.append(text.substring(index));
        text = sb.toString();
        // 查找热词替换占位符
        int replaced = 0;
        Map<String, HotWordMonitoredCache.HotWordCache> replaceMap = new HashMap<>();
		for (String groupCode : groupCodes) {
			Map<String, HotWordMonitoredCache.HotWordCache> hotWords = this.getHotWords(owner, groupCode);
            for (Entry<String, HotWordMonitoredCache.HotWordCache> e : hotWords.entrySet()) {
                if (StringUtils.isEmpty(target)) {
                    target = e.getValue().getTarget();
                }
                int find = StringUtils.indexOf(text, e.getKey());
                if (find > 0) {
                    replaced++;
                    String placeholder = "{{{" + IdUtils.simpleUUID() + "}}}";
                    text = StringUtils.replaceEx(text, e.getKey(), placeholder);
                    replaceMap.put(placeholder, e.getValue());
                    if (replaceCount > 0 && replaced >= replaceCount) {
                        break;
                    }
                }
            }
            if (replaceCount > 0 && replaced >= replaceCount) {
                break;
            }
		}
        // 先还原已存在的a标签
        for (Entry<String, String> e : linkMap.entrySet()) {
            text = StringUtils.replaceEx(text, e.getKey(), e.getValue());
        }
        // 替换热词占位符
        if (replaced > 0) {
            for (Entry<String, HotWordMonitoredCache.HotWordCache> e : replaceMap.entrySet()) {
                String replacement = StringUtils.messageFormat(replacementTemplate, e.getValue().getUrl(),
                        e.getValue().getWord(), target);
                text = StringUtils.replaceEx(text, e.getKey(), replacement);
            }
        }
		return text;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void addHotWord(CreateHotWordRequest req) {
		RLock lock = redissonClient.getLock(LOCK_HOT_WORD);
		lock.lock();
		try {
			boolean checkUnique = checkUnique(req.getGroupId(), null, req.getWord());
			Assert.isTrue(checkUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("word"));

			HotWordGroup tagWordGroup = hotWordGroupMapper.selectById(req.getGroupId());
			Assert.notNull(tagWordGroup, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("groupId", req.getGroupId()));

			HotWord word = new HotWord();
			word.setWordId(IdUtils.getSnowflakeId());
			word.setOwner(tagWordGroup.getOwner());
			word.setGroupId(tagWordGroup.getGroupId());
			word.setWord(req.getWord());
			word.setUrl(req.getUrl());
			word.setUrlTarget(req.getUrlTarget());
			word.setUseCount(0L);
			word.setHitCount(0L);
			word.setRemark(req.getRemark());
			word.createBy(req.getOperator().getUsername());
			this.save(word);

			tagWordGroup.setWordTotal(tagWordGroup.getWordTotal() + 1);
			hotWordGroupMapper.updateById(tagWordGroup);
            // 清理缓存
            hotWordCache.deleteCache(tagWordGroup.getCode());
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void editHotWord(UpdateHotWordRequest req) {
		HotWord dbWord = this.getById(req.getWordId());
		Assert.notNull(dbWord, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("wordId", req.getWordId()));

		boolean checkUnique = checkUnique(req.getGroupId(), req.getWordId(), req.getWord());
		Assert.isTrue(checkUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("word"));

		dbWord.setWord(req.getWord());
		dbWord.setUrl(req.getUrl());
		dbWord.setUrlTarget(req.getUrlTarget());
		dbWord.setRemark(req.getRemark());
		dbWord.updateBy(req.getOperator().getUsername());
		this.updateById(dbWord);
        // 清理缓存
        HotWordGroup tagWordGroup = hotWordGroupMapper.selectById(dbWord.getGroupId());
        hotWordCache.deleteCache(tagWordGroup.getCode());
    }

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteHotWords(List<Long> hotWordIds) {
		RLock lock = redissonClient.getLock(LOCK_HOT_WORD);
		lock.lock();
		try {
			List<HotWord> hotWords = this.listByIds(hotWordIds);
			Map<Long, Integer> groupWordDecrease = new HashMap<>();
			hotWords.forEach(tag -> {
				groupWordDecrease.put(tag.getGroupId(), groupWordDecrease.getOrDefault(tag.getGroupId(), 0) + 1);
			});
			this.removeByIds(hotWordIds);
			groupWordDecrease.forEach((groupId, removeCount) -> {
				HotWordGroup group = hotWordGroupMapper.selectById(groupId);
				group.setWordTotal(group.getWordTotal() - removeCount);
				hotWordGroupMapper.updateById(group);
                // 清理缓存
                hotWordCache.deleteCache(group.getCode());
			});
		} finally {
			lock.unlock();
		}
	}

	private boolean checkUnique(Long groupId, Long wordId, String word) {
		return this.lambdaQuery()
				.eq(HotWord::getGroupId, groupId)
				.ne(IdUtils.validate(wordId), HotWord::getWordId, wordId)
				.eq(HotWord::getWord, word)
				.count() == 0;
	}
}
