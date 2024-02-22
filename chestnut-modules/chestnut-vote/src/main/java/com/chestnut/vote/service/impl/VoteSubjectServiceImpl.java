/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
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
package com.chestnut.vote.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.vote.domain.Vote;
import com.chestnut.vote.domain.VoteSubject;
import com.chestnut.vote.domain.VoteSubjectItem;
import com.chestnut.vote.domain.dto.SaveSubjectItemsDTO;
import com.chestnut.vote.fixed.dict.VoteSubjectType;
import com.chestnut.vote.mapper.VoteSubjectItemMapper;
import com.chestnut.vote.mapper.VoteSubjectMapper;
import com.chestnut.vote.service.IVoteService;
import com.chestnut.vote.service.IVoteSubjectService;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VoteSubjectServiceImpl extends ServiceImpl<VoteSubjectMapper, VoteSubject> implements IVoteSubjectService {

	private final IVoteService voteService;

	private final VoteSubjectItemMapper voteSubjectItemMapper;

	@Override
	public List<VoteSubject> getVoteSubjectList(Long voteId) {
		Vote vote = this.voteService.getById(voteId);
		Assert.notNull(vote, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("voteId", voteId));

		List<VoteSubject> subjects = this.lambdaQuery().eq(VoteSubject::getVoteId, voteId)
				.orderByAsc(VoteSubject::getSortFlag).list();

		Map<Long, List<VoteSubjectItem>> itemsMap = new LambdaQueryChainWrapper<>(this.voteSubjectItemMapper)
				.eq(VoteSubjectItem::getVoteId, voteId).orderByAsc(VoteSubjectItem::getSortFlag).list().stream()
				.collect(Collectors.groupingBy(VoteSubjectItem::getSubjectId));

		vote.setSubjectList(subjects);
		subjects.forEach(subject -> {
			List<VoteSubjectItem> items = itemsMap.getOrDefault(subject.getSubjectId(), List.of());
			items.forEach(item -> item.setVoteTotal(vote.getTotal()));
			subject.setItemList(items);
		});
		return subjects;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void addVoteSubject(VoteSubject voteSubject) {
		Vote vote = this.voteService.getById(voteSubject.getVoteId());
		Assert.notNull(vote, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("voteId", voteSubject.getVoteId()));

		Map<Long, VoteSubject> dbSubjects = this.lambdaQuery().eq(VoteSubject::getVoteId, voteSubject.getVoteId())
				.orderByAsc(VoteSubject::getSortFlag).list().stream()
				.collect(Collectors.toMap(VoteSubject::getSubjectId, s -> s));

		voteSubject.setSubjectId(IdUtils.getSnowflakeId());
		if (IdUtils.validate(voteSubject.getNextSubjectId())) {
			// 在指定主题后的所有主题排序号+1
			VoteSubject nextSubject = dbSubjects.get(voteSubject.getNextSubjectId());
			dbSubjects.values().stream().filter(s -> s.getSortFlag() >= nextSubject.getSortFlag()).forEach(s -> {
				this.lambdaUpdate().set(VoteSubject::getSortFlag, s.getSortFlag() + 1)
						.eq(VoteSubject::getSubjectId, s.getSubjectId()).update();
			});
			voteSubject.setSortFlag(nextSubject.getSortFlag());
		} else {
			voteSubject.setSortFlag(dbSubjects.size());
		}
		this.save(voteSubject);
		// 更新缓存
		this.voteService.clearVoteCache(voteSubject.getVoteId());
	}

	@Override
	public void updateVoteSubject(VoteSubject voteSubject) {
		VoteSubject db = this.getById(voteSubject.getSubjectId());
		Assert.notNull(db,
				() -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("subjectId", voteSubject.getSubjectId()));

		db.setTitle(voteSubject.getTitle());
		if (!db.getType().equals(voteSubject.getType())) {
			db.setType(voteSubject.getType());
			if (VoteSubjectType.isInput(db.getType())) {
				// 类型修改成输入则移除所有选项
				this.voteSubjectItemMapper.delete(
						new LambdaQueryWrapper<VoteSubjectItem>().eq(VoteSubjectItem::getSubjectId, db.getSubjectId()));
			}
		}
		this.updateById(db);
		// 更新缓存
		this.voteService.clearVoteCache(db.getVoteId());
	}

	@Override
	public void deleteVoteSubjects(@NotEmpty List<Long> subjectIds) {
		List<VoteSubject> subjects = this.listByIds(subjectIds);
		this.removeByIds(subjects);
		// 删除选项
		this.voteSubjectItemMapper
				.delete(new LambdaQueryWrapper<VoteSubjectItem>().in(VoteSubjectItem::getSubjectId, subjectIds));
		// 更新缓存
		subjects.stream().map(VoteSubject::getVoteId).distinct()
				.forEach(voteId -> this.voteService.clearVoteCache(voteId));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveSubjectItems(SaveSubjectItemsDTO dto) {
		VoteSubject subject = this.getById(dto.getSubjectId());
		Assert.notNull(subject, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("subjectId", dto.getSubjectId()));

		List<VoteSubjectItem> dbItems = new LambdaQueryChainWrapper<>(this.voteSubjectItemMapper)
				.eq(VoteSubjectItem::getSubjectId, subject.getSubjectId()).list();

		List<Long> updateItemIds = dto.getItemList().stream().filter(item -> IdUtils.validate(item.getItemId()))
				.map(VoteSubjectItem::getItemId).toList();
		List<Long> remmoveItemIds = dbItems.stream().filter(item -> !updateItemIds.contains(item.getItemId()))
				.map(VoteSubjectItem::getItemId).toList();
		if (remmoveItemIds.size() > 0) {
			this.voteSubjectItemMapper.deleteBatchIds(remmoveItemIds);
		}

		Map<Long, VoteSubjectItem> updateMap = dbItems.stream().filter(item -> updateItemIds.contains(item.getItemId()))
				.collect(Collectors.toMap(VoteSubjectItem::getItemId, item -> item));
		List<VoteSubjectItem> itemList = dto.getItemList();
		for (int i = 0; i < itemList.size(); i++) {
			VoteSubjectItem item = itemList.get(i);
			item.setSortFlag(i);
			if (IdUtils.validate(item.getItemId())) {
				VoteSubjectItem dbItem = updateMap.get(item.getItemId());
				BeanUtils.copyProperties(item, dbItem, "total");
				dbItem.updateBy(dto.getOperator().getUsername());
				this.voteSubjectItemMapper.updateById(dbItem);
			} else {
				item.setItemId(IdUtils.getSnowflakeId());
				item.setVoteId(subject.getVoteId());
				item.setSubjectId(subject.getSubjectId());
				item.setTotal(0);
				item.updateBy(dto.getOperator().getUsername());
				this.voteSubjectItemMapper.insert(item);
			}
		}
		// 更新缓存
		this.voteService.clearVoteCache(subject.getVoteId());
	}
}