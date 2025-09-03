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
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.SysConstants;
import com.chestnut.system.domain.SysDictData;
import com.chestnut.system.domain.SysDictType;
import com.chestnut.system.domain.dto.CreateDictTypeRequest;
import com.chestnut.system.domain.dto.UpdateDictTypeRequest;
import com.chestnut.system.exception.SysErrorCode;
import com.chestnut.system.fixed.FixedDictUtils;
import com.chestnut.system.mapper.SysDictDataMapper;
import com.chestnut.system.mapper.SysDictTypeMapper;
import com.chestnut.system.service.ISysDictTypeService;
import com.chestnut.system.service.ISysI18nDictService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 字典 业务层处理
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Service
@RequiredArgsConstructor
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType>
		implements ISysDictTypeService, CommandLineRunner {

	private final RedisCache redisCache;

	private final SysDictDataMapper dictDataMapper;

	private final ISysI18nDictService i18nDictService;

	@Override
	public List<SysDictData> selectDictDatasByType(String dictType) {
        return this.redisCache.getCacheList(SysConstants.CACHE_SYS_DICT_KEY + dictType, SysDictData.class,
				() -> new LambdaQueryChainWrapper<>(this.dictDataMapper).eq(SysDictData::getDictType, dictType)
						.orderByAsc(SysDictData::getDictSort).list());
	}

	@Override
	public Optional<SysDictData> optDictData(String dictType, String dictValue) {
		return this.selectDictDatasByType(dictType).stream().filter(d -> d.getDictValue().equals(dictValue))
				.findFirst();
	}

	@Override
	public void deleteDictTypeByIds(List<Long> dictIds) {
		List<SysDictType> dicts = this.listByIds(dictIds);
		for (SysDictType dictType : dicts) {
			// 不能删除系统固定字典数据
			Assert.isFalse(FixedDictUtils.isFixedDictType(dictType.getDictType()),
					CommonErrorCode.FIXED_DICT::exception);
			// 删除字典数据项
			this.dictDataMapper
					.delete(new LambdaQueryWrapper<SysDictData>().eq(SysDictData::getDictType, dictType.getDictType()));

			this.removeById(dictType);
			this.deleteCache(dictType.getDictType());
			// 删除国际化配置
			this.i18nDictService.deleteByLangKey(langKey(dictType.getDictType()), false);
			this.i18nDictService.deleteByLangKey(langKey(dictType.getDictType()) + ".", true);
		}
	}

	@Override
	@Transactional(rollbackFor = Throwable.class)
	public void insertDictType(CreateDictTypeRequest req) {
		Assert.isTrue(this.checkDictTypeUnique(req.getDictType(), null),
				() -> SysErrorCode.DICT_TYPE_CONFLICT.exception(req.getDictType()));

		SysDictType dict = new SysDictType();
		dict.setDictId(IdUtils.getSnowflakeId());
		dict.setDictType(req.getDictType());
		dict.setDictName(req.getDictName());
		dict.setRemark(req.getRemark());
		dict.createBy(req.getOperator().getUsername());
		this.save(dict);

		i18nDictService.saveOrUpdate(LocaleContextHolder.getLocale().toLanguageTag(), langKey(dict.getDictType()), dict.getDictName());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateDictType(UpdateDictTypeRequest req) {
		SysDictType dbDict = this.getById(req.getDictId());
		Assert.notNull(dbDict, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("dictId", req.getDictId()));
		Assert.isTrue(this.checkDictTypeUnique(req.getDictType(), req.getDictId()),
				() -> CommonErrorCode.DATA_CONFLICT.exception("dictType:" + req.getDictType()));

		// 类型变更需要更新关联字典项数据
		String oldDictType = dbDict.getDictType();

		dbDict.setDictName(req.getDictName());
		dbDict.setDictType(req.getDictType());
		dbDict.setRemark(req.getRemark());
		dbDict.setUpdateTime(LocalDateTime.now());
		this.updateById(dbDict);
		// 类型变更需要更新字典数据，更新缓存
		if (!oldDictType.equals(dbDict.getDictType())) {
			// 不能修改系统固定字典类型
			Assert.isFalse(FixedDictUtils.isFixedDictType(oldDictType), CommonErrorCode.FIXED_DICT::exception);
			// 修改字典数据类型，更新缓存
			new LambdaUpdateChainWrapper<>(this.dictDataMapper).set(SysDictData::getDictType, dbDict.getDictType())
					.eq(SysDictData::getDictType, oldDictType).update();
			this.deleteCache(req.getDictType());
			this.deleteCache(oldDictType);
			// 国际化
			i18nDictService.changeLangKey(langKey(oldDictType), langKey(dbDict.getDictType()), true);
		}
	}

	@Override
	public boolean checkDictTypeUnique(String dictType, Long dictId) {
		long result = this.lambdaQuery().eq(SysDictType::getDictType, dictType)
				.ne(IdUtils.validate(dictId), SysDictType::getDictId, dictId).count();
		return result == 0;
	}

	@Override
	public void resetDictCache() {
		clearDictCache();
		loadingDictCache();
	}

	@Override
	public void loadingDictCache() {
		List<SysDictData> list = this.dictDataMapper.selectList(null);
		Map<String, List<SysDictData>> collect = list.stream().sorted(Comparator.comparing(SysDictData::getDictSort))
				.collect(Collectors.groupingBy(SysDictData::getDictType));
		collect.forEach((k, v) -> this.redisCache.setCacheList(SysConstants.CACHE_SYS_DICT_KEY + k, v));
	}

	@Override
	public void clearDictCache() {
		this.redisCache.deleteObjects(this.redisCache.keys(SysConstants.CACHE_SYS_DICT_KEY + "*"));
	}

	private void deleteCache(String dictType) {
		this.redisCache.deleteObject(SysConstants.CACHE_SYS_DICT_KEY + dictType);
	}

	private String langKey(String dictType) {
		return "DICT." + dictType;
	}

	/**
	 * 将列表指定字段getter值通过字典数据映射字典名称到指定字段setter
	 */
	@Override
	public <T> void decode(String dictType, List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		Map<String, String> map = this.selectDictDatasByType(dictType).stream()
				.collect(Collectors.toMap(SysDictData::getDictValue, SysDictData::getDictLabel));
		list.forEach(dt -> {
			String dictValue = getter.apply(dt);
			if (map.containsKey(dictValue)) {
				setter.accept(dt, I18nUtils.get(map.get(dictValue)));
			} else {
				setter.accept(dt, dictValue);
			}
		});
	}

	/**
	 * 初始化固定字典数据到数据库
	 */
	@Override
	public void run(String... args) throws Exception {
		this.resetDictCache();
		// 初始化固定字典数据
		FixedDictUtils.allFixedDicts().forEach(dict -> {
			List<SysDictData> dictDatas = this.selectDictDatasByType(dict.getDictType());
			if (dictDatas.isEmpty()) {
				// 无字典数据，先添加字典类型
				SysDictType dictType = this.lambdaQuery().eq(SysDictType::getDictType, dict.getDictType()).one();
				if (Objects.isNull(dictType)) {
					SysDictType dt = new SysDictType();
					dt.setDictId(IdUtils.getSnowflakeId());
					dt.setDictType(dict.getDictType());
					dt.setDictName(I18nUtils.get(dict.getDictName()));
					dt.setRemark(dict.getRemark());
					dt.createBy(SysConstants.SYS_OPERATOR);
					this.save(dt);
				}
			}
			// 添加未存储的字典数据
			dict.getDataList().forEach(d -> {
				boolean contains = dictDatas.stream()
						.anyMatch(d2 -> StringUtils.equals(d2.getDictValue(), d.getValue()));
				if (!contains) {
					SysDictData data = new SysDictData();
					data.setDictCode(IdUtils.getSnowflakeId());
					data.setDictType(dict.getDictType());
					data.setDictValue(d.getValue());
					data.setDictLabel(I18nUtils.get(d.getLabel()));
					data.setDictSort(d.getSort());
					data.setRemark(d.getRemark());
					data.createBy(SysConstants.SYS_OPERATOR);
					this.dictDataMapper.insert(data);
				}
			});
			this.deleteCache(dict.getDictType());
		});
	}
}
