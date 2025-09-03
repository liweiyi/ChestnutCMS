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
package com.chestnut.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.SysConstants;
import com.chestnut.system.domain.SysDictData;
import com.chestnut.system.domain.dto.CreateDictDataRequest;
import com.chestnut.system.domain.dto.UpdateDictDataRequest;
import com.chestnut.system.fixed.FixedDictType;
import com.chestnut.system.fixed.FixedDictUtils;
import com.chestnut.system.mapper.SysDictDataMapper;
import com.chestnut.system.service.ISysDictDataService;
import com.chestnut.system.service.ISysI18nDictService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 字典 业务层处理
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Service
@RequiredArgsConstructor
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData> implements ISysDictDataService {

	private final RedisCache redisCache;

	private final ISysI18nDictService i18nDictService;

	@Override
	@Transactional(rollbackFor = Throwable.class)
	public void deleteDictDataByIds(List<Long> dictDataIds) {
		List<SysDictData> list = this.listByIds(dictDataIds);
		for (SysDictData data : list) {
			Assert.isFalse(isFixed(data), CommonErrorCode.FIXED_DICT::exception);
		}
		this.removeBatchByIds(list);
		// 删除缓存
		list.stream().map(SysDictData::getDictType).distinct().forEach(dictType -> {
			this.redisCache.deleteObject(SysConstants.CACHE_SYS_DICT_KEY + dictType);
		});
		// 删除国际化配置
		for (SysDictData data : list) {
			this.i18nDictService.deleteByLangKey(langKey(data.getDictType(), data.getDictValue()), false);
		}
	}

	private boolean isFixed(SysDictData data) {
		FixedDictType dictType = FixedDictUtils.getFixedDictType(data.getDictType());
		return dictType != null && dictType.getDataList().stream().anyMatch(d -> d.getValue().equals(data.getDictValue()));
	}

	@Override
	@Transactional(rollbackFor = Throwable.class)
	public void insertDictData(CreateDictDataRequest req) {
		Assert.isTrue(this.checkDictValueUnique(req.getDictType(), req.getDictValue(), null),
				() -> CommonErrorCode.DATA_CONFLICT.exception("dictValue:" + req.getDictType()));

		FixedDictType dictType = FixedDictUtils.getFixedDictType(req.getDictType());
		if (Objects.nonNull(dictType) && !dictType.isAllowAdd()) {
			throw CommonErrorCode.FIXED_DICT_NOT_ALLOW_ADD.exception();
		}
		SysDictData dictData = new SysDictData();
		BeanUtils.copyProperties(req, dictData);
		dictData.setDictCode(IdUtils.getSnowflakeId());
		dictData.createBy(req.getOperator().getUsername());
		this.save(dictData);

		i18nDictService.saveOrUpdate(LocaleContextHolder.getLocale().toLanguageTag(),
				langKey(req.getDictType(), req.getDictValue()), req.getDictLabel());
		this.redisCache.deleteObject(SysConstants.CACHE_SYS_DICT_KEY + req.getDictType());
	}

	@Override
	public void updateDictData(UpdateDictDataRequest req) {
		SysDictData dbData = this.getById(req.getDictCode());
		// 是否存在
		Assert.notNull(dbData, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("dictCode", req.getDictCode()));
		// 是否系统固定字典数据，固定数据不允许修改字典数据值
		Assert.isFalse(isFixed(dbData) && !StringUtils.equals(dbData.getDictValue(), req.getDictValue()),
				CommonErrorCode.FIXED_DICT::exception);
		// 字典数据值是否冲突
		Assert.isTrue(this.checkDictValueUnique(req.getDictType(), req.getDictValue(), req.getDictCode()),
				() -> CommonErrorCode.DATA_CONFLICT.exception("dictValue:" + req.getDictValue()));

		String oldDictValue = dbData.getDictValue();
		dbData.setDictValue(req.getDictValue());
		dbData.setDictLabel(req.getDictLabel());
		dbData.setDictSort(req.getDictSort());
		dbData.setIsDefault(req.getIsDefault());
		dbData.setCssClass(req.getCssClass());
		dbData.setListClass(req.getListClass());
		dbData.setRemark(req.getRemark());
		dbData.updateBy(req.getOperator().getUsername());

		if (this.updateById(dbData)) {
			this.redisCache.deleteObject(SysConstants.CACHE_SYS_DICT_KEY + dbData.getDictType());
			if (!StringUtils.equals(oldDictValue, dbData.getDictValue())) {
				i18nDictService.changeLangKey(langKey(dbData.getDictType(), oldDictValue),
						langKey(dbData.getDictType(), dbData.getDictValue()), false);
			}
		}
	}

	public boolean checkDictValueUnique(String dictType, String dictValue, Long dictCode) {
		long result = this.count(new LambdaQueryWrapper<SysDictData>()
				.eq(SysDictData::getDictValue, dictValue).eq(SysDictData::getDictType, dictType)
				.ne(IdUtils.validate(dictCode), SysDictData::getDictCode, dictCode));
		return result == 0;
	}

	private String langKey(String dictType, String dictValue) {
		return "DICT." + dictType + "." + dictValue;
	}
}
