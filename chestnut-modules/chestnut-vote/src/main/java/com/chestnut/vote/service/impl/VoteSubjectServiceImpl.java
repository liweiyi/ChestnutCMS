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
package com.chestnut.vote.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.vote.domain.Vote;
import com.chestnut.vote.domain.VoteSubject;
import com.chestnut.vote.domain.VoteSubjectItem;
import com.chestnut.vote.domain.dto.CreateVoteSubjectRequest;
import com.chestnut.vote.domain.dto.SaveSubjectItemsRequest;
import com.chestnut.vote.domain.dto.UpdateVoteSubjectRequest;
import com.chestnut.vote.fixed.dict.VoteSubjectType;
import com.chestnut.vote.mapper.VoteSubjectItemMapper;
import com.chestnut.vote.mapper.VoteSubjectMapper;
import com.chestnut.vote.service.IVoteService;
import com.chestnut.vote.service.IVoteSubjectService;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
	public void addVoteSubject(CreateVoteSubjectRequest req) {
		Vote vote = this.voteService.getById(req.getVoteId());
		Assert.notNull(vote, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("voteId", req.getVoteId()));

		Map<Long, VoteSubject> dbSubjects = this.lambdaQuery().eq(VoteSubject::getVoteId, req.getVoteId())
				.orderByAsc(VoteSubject::getSortFlag).list().stream()
				.collect(Collectors.toMap(VoteSubject::getSubjectId, s -> s));

		VoteSubject voteSubject = new VoteSubject();
		voteSubject.setSubjectId(IdUtils.getSnowflakeId());
		voteSubject.setVoteId(req.getVoteId());
		voteSubject.setType(req.getType());
		voteSubject.setTitle(req.getTitle());
		voteSubject.createBy(req.getOperator().getUsername());
		if (IdUtils.validate(req.getNextSubjectId())) {
			// 在指定主题后的所有主题排序号+1
			VoteSubject nextSubject = dbSubjects.get(req.getNextSubjectId());
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
		this.voteService.clearVoteCache(req.getVoteId());
	}

	@Override
	public void updateVoteSubject(UpdateVoteSubjectRequest req) {
		VoteSubject db = this.getById(req.getSubjectId());
		Assert.notNull(db,
				() -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("subjectId", req.getSubjectId()));

		db.setTitle(req.getTitle());
		if (!db.getType().equals(req.getType())) {
			db.setType(req.getType());
			if (VoteSubjectType.isInput(db.getType())) {
				// 类型修改成输入则移除所有选项
				this.voteSubjectItemMapper.delete(
						new LambdaQueryWrapper<VoteSubjectItem>().eq(VoteSubjectItem::getSubjectId, db.getSubjectId()));
			}
			db.setType(req.getType());
		}
		db.updateBy(req.getOperator().getUsername());
		this.updateById(db);
		// 更新缓存
		this.voteService.clearVoteCache(db.getVoteId());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteVoteSubjects(@NotEmpty List<Long> subjectIds) {
		List<VoteSubject> subjects = this.listByIds(subjectIds);
		if (subjects.isEmpty()) {
			return;
		}
		this.removeByIds(subjects);
		// 删除选项
		this.voteSubjectItemMapper
				.delete(new LambdaQueryWrapper<VoteSubjectItem>().in(VoteSubjectItem::getSubjectId, subjectIds));
		// 更新缓存
		subjects.stream().map(VoteSubject::getVoteId).distinct()
				.forEach(this.voteService::clearVoteCache);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveSubjectItems(SaveSubjectItemsRequest req) {
		VoteSubject subject = this.getById(req.getSubjectId());
		Assert.notNull(subject, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("subjectId", req.getSubjectId()));

		List<VoteSubjectItem> dbItems = new LambdaQueryChainWrapper<>(this.voteSubjectItemMapper)
				.eq(VoteSubjectItem::getSubjectId, subject.getSubjectId()).list();
		Map<Long, VoteSubjectItem> dbItemMap = dbItems.stream().collect(Collectors.toMap(VoteSubjectItem::getItemId, item -> item));

		List<Long> updateItemIds = req.getItemList().stream().map(SaveSubjectItemsRequest.SubjectItem::getItemId)
				.filter(IdUtils::validate).toList();
		List<Long> removeItemIds = dbItems.stream().map(VoteSubjectItem::getItemId)
				.filter(itemId -> !updateItemIds.contains(itemId)).toList();
		if (!removeItemIds.isEmpty()) {
			this.voteSubjectItemMapper.deleteByIds(removeItemIds);
		}
		List<SaveSubjectItemsRequest.SubjectItem> itemList = req.getItemList();
		for (int i = 0; i < itemList.size(); i++) {
			SaveSubjectItemsRequest.SubjectItem item = itemList.get(i);

			VoteSubjectItem subjectItem = dbItemMap.get(item.getItemId());
			if (Objects.isNull(subjectItem)) {
				subjectItem = new VoteSubjectItem();
				subjectItem.setItemId(IdUtils.getSnowflakeId());
				subjectItem.setVoteId(subject.getVoteId());
				subjectItem.setSubjectId(subject.getSubjectId());
				subjectItem.setTotal(0);
				subjectItem.createBy(req.getOperator().getUsername());
			}
			subjectItem.setType(item.getType());
			subjectItem.setContent(item.getContent());
			subjectItem.setDescription(item.getDescription());
			subjectItem.setSortFlag(i);
			subjectItem.updateBy(req.getOperator().getUsername());
			this.voteSubjectItemMapper.insertOrUpdate(subjectItem);
		}
		// 更新缓存
		this.voteService.clearVoteCache(subject.getVoteId());
	}
}