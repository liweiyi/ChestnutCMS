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
import com.chestnut.system.domain.SysI18nDict;
import com.chestnut.system.fixed.FixedDictType;
import com.chestnut.system.fixed.FixedDictUtils;
import com.chestnut.system.mapper.SysDictDataMapper;
import com.chestnut.system.service.ISysDictDataService;
import com.chestnut.system.service.ISysI18nDictService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

	/**
	 * 批量删除字典数据信息
	 */
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
			// 删除国际化配置
			this.i18nDictService.remove(new LambdaQueryWrapper<SysI18nDict>()
					.likeRight(SysI18nDict::getLangKey, "DICT." + dictType + "."));
		});
	}

	/**
	 * 是否系统固定字典数据
	 *
	 * @param data
	 * @return
	 */
	private boolean isFixed(SysDictData data) {
		FixedDictType dictType = FixedDictUtils.getFixedDictType(data.getDictType());
		return dictType != null && dictType.getDataList().stream().anyMatch(d -> d.getValue().equals(data.getDictValue()));
	}

	/**
	 * 新增保存字典数据信息
	 *
	 * @param data 字典数据信息
	 * @return 结果
	 */
	@Override
	@Transactional(rollbackFor = Throwable.class)
	public void insertDictData(SysDictData data) {
		Assert.isTrue(this.checkDictValueUnique(data),
				() -> CommonErrorCode.DATA_CONFLICT.exception("dictValue:" + data.getDictType()));

		FixedDictType dictType = FixedDictUtils.getFixedDictType(data.getDictType());
		if (Objects.nonNull(dictType) && !dictType.isAllowAdd()) {
			throw CommonErrorCode.FIXED_DICT_NOT_ALLOW_ADD.exception();
		}

		data.setDictCode(IdUtils.getSnowflakeId());
		data.setCreateTime(LocalDateTime.now());
		this.save(data);

		SysI18nDict i18nDict = new SysI18nDict();
		i18nDict.setLangKey("DICT." + data.getDictType() + "." + data.getDictValue());
		i18nDict.setLangTag(LocaleContextHolder.getLocale().toLanguageTag());
		i18nDict.setLangValue(data.getDictLabel());
		i18nDictService.batchSaveI18nDicts(List.of(i18nDict));

		this.redisCache.deleteObject(SysConstants.CACHE_SYS_DICT_KEY + data.getDictType());
	}

	/**
	 * 修改保存字典数据信息
	 *
	 * @param data 字典数据信息
	 * @return 结果
	 */
	@Override
	public void updateDictData(SysDictData data) {
		SysDictData dbData = this.getById(data.getDictCode());
		// 是否存在
		Assert.notNull(dbData, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("dictCode", data.getDictCode()));
		// 是否系统固定字典数据，固定数据不允许修改字典数据值
		Assert.isFalse(isFixed(dbData) && !StringUtils.equals(dbData.getDictValue(), data.getDictValue()),
				CommonErrorCode.FIXED_DICT::exception);
		// 字典数据值是否冲突
		Assert.isTrue(this.checkDictValueUnique(data),
				() -> CommonErrorCode.DATA_CONFLICT.exception("dictValue:" + data.getDictValue()));

		dbData.setDictValue(data.getDictValue());
		dbData.setDictLabel(data.getDictLabel());
		dbData.setDictSort(data.getDictSort());
		dbData.setIsDefault(data.getIsDefault());
		dbData.setCssClass(data.getCssClass());
		dbData.setListClass(data.getListClass());
		dbData.setRemark(data.getRemark());
		dbData.setUpdateTime(LocalDateTime.now());

		if (this.updateById(dbData)) {
			this.redisCache.deleteObject(SysConstants.CACHE_SYS_DICT_KEY + data.getDictType());
		}
	}

	/**
	 * 校验字典数据项值是否冲突
	 *
	 * @param data
	 * @return
	 */
	public boolean checkDictValueUnique(SysDictData data) {
		long result = this.count(new LambdaQueryWrapper<SysDictData>()
				.eq(SysDictData::getDictValue, data.getDictValue()).eq(SysDictData::getDictType, data.getDictType())
				.ne(IdUtils.validate(data.getDictCode()), SysDictData::getDictCode, data.getDictCode()));
		return result == 0;
	}
}
