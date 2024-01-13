package com.chestnut.word.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.SortUtils;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.word.domain.HotWord;
import com.chestnut.word.domain.HotWordGroup;
import com.chestnut.word.exception.WordErrorCode;
import com.chestnut.word.mapper.HotWordGroupMapper;
import com.chestnut.word.mapper.HotWordMapper;
import com.chestnut.word.service.IHotWordGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HotWordGroupServiceImpl extends ServiceImpl<HotWordGroupMapper, HotWordGroup>
		implements IHotWordGroupService {

	private final HotWordMapper hotWordMapper;

	@Override
	public HotWordGroup addHotWordGroup(HotWordGroup group) {
		this.checkUnique(group.getGroupId(), group.getName(), group.getCode());

		group.setGroupId(IdUtils.getSnowflakeId());
		group.setWordTotal(0L);
		group.setSortFlag(SortUtils.getDefaultSortValue());
		group.createBy(StpAdminUtil.getLoginUser().getUsername());
		this.save(group);
		return group;
	}

	@Override
	public void updateHotWordGroup(HotWordGroup group) {
		HotWordGroup db = this.getById(group.getGroupId());
		Assert.notNull(db, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("groupId", group.getGroupId()));

		this.checkUnique(group.getGroupId(), group.getName(), group.getCode());

		db.setName(group.getName());
		db.setCode(group.getCode());
		db.setRemark(group.getRemark());
		db.updateBy(group.getUpdateBy());
		this.updateById(db);
	}

	@Override
	public void deleteHotWordGroups(List<Long> groupIds) {
		for (Long groupId : groupIds) {
			this.removeById(groupId);
			this.hotWordMapper.delete(new LambdaQueryWrapper<HotWord>().eq(HotWord::getGroupId, groupId));
		}
	}

	private void checkUnique(Long groupId, String name, String code) {
		Long count = this.lambdaQuery().ne(groupId != null && groupId > 0, HotWordGroup::getGroupId, groupId)
				.and(wrapper -> wrapper.eq(HotWordGroup::getName, name).or().eq(HotWordGroup::getCode, code))
				.count();
		Assert.isTrue(count == 0, WordErrorCode.CONFLIECT_HOT_WORD_GROUP::exception);
	}
}
