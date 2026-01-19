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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.SortUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.word.cache.TagWordMonitoredCache;
import com.chestnut.word.domain.TagWord;
import com.chestnut.word.domain.TagWordGroup;
import com.chestnut.word.domain.dto.BatchAddTagRequest;
import com.chestnut.word.domain.dto.CreateTagWordRequest;
import com.chestnut.word.domain.dto.UpdateTagWordRequest;
import com.chestnut.word.mapper.TagWordGroupMapper;
import com.chestnut.word.mapper.TagWordMapper;
import com.chestnut.word.service.ITagWordService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TagWordServiceImpl extends ServiceImpl<TagWordMapper, TagWord> implements ITagWordService {

	private static final String LOCK_TAG_WORD = "CC-TagWord";

	private final RedissonClient redissonClient;

	private final TagWordGroupMapper tagWordGroupMapper;

    private final TagWordMonitoredCache tagWordMonitoredCache;

    @Override
    public List<TagWordMonitoredCache.TagWordCache> getTagWords(String owner, String groupCode) {
		Map<String, TagWordMonitoredCache.TagWordCache> cache = tagWordMonitoredCache.getTagWords(owner, groupCode);
        if (Objects.nonNull(cache)) {
            return cache.values().stream().toList();
        }
        TagWordGroup tagWordGroup = tagWordGroupMapper.selectOne(
                new LambdaQueryWrapper<TagWordGroup>().eq(TagWordGroup::getCode, groupCode));
        if (Objects.nonNull(tagWordGroup)) {
            List<TagWord> tagWords = this.lambdaQuery().eq(TagWord::getGroupId, tagWordGroup.getGroupId()).list();
            List<TagWordMonitoredCache.TagWordCache> caches = tagWords.stream().map(TagWordMonitoredCache.TagWordCache::new).toList();
            tagWordMonitoredCache.setTagWords(owner, groupCode, caches);
            return caches;
        }
        return null;
    }

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void addTagWord(CreateTagWordRequest req) {
		RLock lock = redissonClient.getLock(LOCK_TAG_WORD);
		lock.lock();
		try {
			boolean checkUnique = checkUnique(req.getGroupId(), null, req.getWord());
			Assert.isTrue(checkUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("word"));

			TagWordGroup tagWordGroup = tagWordGroupMapper.selectById(req.getGroupId());
			Assert.notNull(tagWordGroup, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("groupId", req.getGroupId()));

			TagWord tagWord = new TagWord();
			tagWord.setWordId(IdUtils.getSnowflakeId());
			tagWord.setOwner(tagWordGroup.getOwner());
			tagWord.setGroupId(req.getGroupId());
			tagWord.setOwner(req.getOwner());
			tagWord.setWord(req.getWord());
			tagWord.setLogo(req.getLogo());
			tagWord.setSortFlag(Objects.requireNonNullElse(req.getSortFlag(), SortUtils.getDefaultSortValue()));
			tagWord.setRemark(req.getRemark());
			tagWord.createBy(req.getOperator().getUsername());
			this.save(tagWord);

			tagWordGroup.setWordTotal(tagWordGroup.getWordTotal() + 1);
			tagWordGroupMapper.updateById(tagWordGroup);
            tagWordMonitoredCache.addCache(tagWordGroup.getOwner(), tagWordGroup.getCode(), List.of(tagWord));
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void batchAddTagWord(BatchAddTagRequest req) {
		RLock lock = redissonClient.getLock(LOCK_TAG_WORD);
		lock.lock();
		try {
			TagWordGroup tagWordGroup = tagWordGroupMapper.selectById(req.getGroupId());
			Assert.notNull(tagWordGroup, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("groupId", req.getGroupId()));

			for (String word : req.getWords()) {
				boolean checkUnique = checkUnique(req.getGroupId(), null, word);
				Assert.isTrue(checkUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("word"));
			}

			List<TagWord> list = req.getWords().stream().filter(StringUtils::isNotBlank).map(word -> {
				TagWord tagWord = new TagWord();
				tagWord.setWordId(IdUtils.getSnowflakeId());
				tagWord.setGroupId(req.getGroupId());
				tagWord.setOwner(tagWordGroup.getOwner());
				tagWord.setWord(word);
				tagWord.setSortFlag(SortUtils.getDefaultSortValue());
				tagWord.createBy(req.getOperator().getUsername());
				return tagWord;
			}).toList();

			this.saveBatch(list, 100);

			tagWordGroup.setWordTotal(tagWordGroup.getWordTotal() + list.size());
			tagWordGroupMapper.updateById(tagWordGroup);
            // 更新缓存
            tagWordMonitoredCache.addCache(tagWordGroup.getOwner(), tagWordGroup.getCode(), list);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void editTagWord(UpdateTagWordRequest req) {
		TagWord dbTagWord = this.getById(req.getWordId());
		Assert.notNull(dbTagWord, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("wordId", req.getWordId()));

		boolean checkUnique = checkUnique(dbTagWord.getGroupId(), dbTagWord.getWordId(), req.getWord());
		Assert.isTrue(checkUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("word"));

		dbTagWord.setWord(req.getWord());
		dbTagWord.setLogo(req.getLogo());
		dbTagWord.setSortFlag(req.getSortFlag());
		dbTagWord.setRemark(req.getRemark());
		dbTagWord.updateBy(req.getOperator().getUsername());
		this.updateById(dbTagWord);
        // 更新缓存
        TagWordGroup tagWordGroup = this.tagWordGroupMapper.selectById(dbTagWord.getGroupId());
        tagWordMonitoredCache.addCache(tagWordGroup.getOwner(), tagWordGroup.getCode(), List.of(dbTagWord));
    }

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteTagWords(List<Long> tagWordIds) {
        List<TagWord> tagWords = this.listByIds(tagWordIds);
        if (tagWords.isEmpty()) {
            return;
        }
		RLock lock = redissonClient.getLock(LOCK_TAG_WORD);
		lock.lock();
		try {
			Map<Long, Integer> groupWordDecrease = new HashMap<>();
			tagWords.forEach(tag -> {
				groupWordDecrease.put(tag.getGroupId(), groupWordDecrease.getOrDefault(tag.getGroupId(), 0) + 1);
			});
            // 删除数据
            this.removeByIds(tagWordIds);
            // 删除缓存
            TagWordGroup tagWordGroup = this.tagWordGroupMapper.selectById(tagWords.get(0).getGroupId());
            tagWordMonitoredCache.removeCache(tagWordGroup.getOwner(), tagWordGroup.getCode(), tagWordIds.toArray(new Long[0]));
            // 更新分组词数
			groupWordDecrease.forEach((k, v) -> {
				TagWordGroup group = tagWordGroupMapper.selectById(k);
				group.setWordTotal(group.getWordTotal() - v);
				tagWordGroupMapper.updateById(group);
			});
		} finally {
			lock.unlock();
		}
	}

	private boolean checkUnique(Long groupId, Long wordId, String word) {
		return this.lambdaQuery().eq(TagWord::getGroupId, groupId)
				.ne(IdUtils.validate(wordId), TagWord::getWordId, wordId)
				.eq(TagWord::getWord, word)
				.count() == 0;
	}
}