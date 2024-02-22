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
import com.chestnut.system.domain.SysI18nDict;
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
		List<SysDictData> dictDatas = this.redisCache.getCacheObject(SysConstants.CACHE_SYS_DICT_KEY + dictType, () -> {
			return new LambdaQueryChainWrapper<>(this.dictDataMapper).eq(SysDictData::getDictType, dictType).orderByAsc(SysDictData::getDictSort).list();
		});
		return dictDatas;
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
			this.i18nDictService.remove(new LambdaQueryWrapper<SysI18nDict>()
					.likeRight(SysI18nDict::getLangKey, "DICT." + dictType.getDictType()));
		}
	}

	@Override
	@Transactional(rollbackFor = Throwable.class)
	public void insertDictType(SysDictType dict) {
		Assert.isTrue(this.checkDictTypeUnique(dict),
				() -> SysErrorCode.DICT_TYPE_CONFLICT.exception(dict.getDictType()));
		dict.setDictId(IdUtils.getSnowflakeId());
		dict.setCreateTime(LocalDateTime.now());
		this.save(dict);

		SysI18nDict i18nDict = new SysI18nDict();
		i18nDict.setLangKey("DICT." + dict.getDictType());
		i18nDict.setLangTag(LocaleContextHolder.getLocale().toLanguageTag());
		i18nDict.setLangValue(dict.getDictName());
		i18nDictService.batchSaveI18nDicts(List.of(i18nDict));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateDictType(SysDictType dict) {
		SysDictType dbDict = this.getById(dict.getDictId());
		Assert.notNull(dbDict, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("dictId", dict.getDictId()));
		Assert.isTrue(this.checkDictTypeUnique(dict),
				() -> CommonErrorCode.DATA_CONFLICT.exception("dictType:" + dict.getDictType()));

		// 类型变更需要更新关联字典项数据
		String oldDictType = dbDict.getDictType();

		dbDict.setDictName(dict.getDictName());
		dbDict.setDictType(dict.getDictType());
		dbDict.setRemark(dict.getRemark());
		dbDict.setUpdateTime(LocalDateTime.now());
		this.updateById(dbDict);
		// 类型变更需要更新字典数据，更新缓存
		if (!oldDictType.equals(dbDict.getDictType())) {
			// 不能修改系统固定字典类型
			Assert.isFalse(FixedDictUtils.isFixedDictType(oldDictType), CommonErrorCode.FIXED_DICT::exception);
			// 修改字典数据类型，更新缓存
			new LambdaUpdateChainWrapper<>(this.dictDataMapper).set(SysDictData::getDictType, dbDict.getDictType())
					.eq(SysDictData::getDictType, oldDictType).update();
			this.deleteCache(dict.getDictType());
		}
	}

	@Override
	public boolean checkDictTypeUnique(SysDictType dict) {
		long result = this.lambdaQuery().eq(SysDictType::getDictType, dict.getDictType())
				.ne(IdUtils.validate(dict.getDictId()), SysDictType::getDictId, dict.getDictId()).count();
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
		list.stream().sorted(Comparator.comparing(SysDictData::getDictSort))
				.collect(Collectors.groupingBy(SysDictData::getDictType)).forEach(this::setCache);
	}

	@Override
	public void clearDictCache() {
		this.redisCache.deleteObject(this.redisCache.keys(SysConstants.CACHE_SYS_DICT_KEY + "*"));
	}

	private void deleteCache(String dictType) {
		this.redisCache.deleteObject(SysConstants.CACHE_SYS_DICT_KEY + dictType);
	}

	private void setCache(String dictType, List<SysDictData> list) {
		this.redisCache.setCacheObject(SysConstants.CACHE_SYS_DICT_KEY + dictType, list);
	}

	/**
	 * 将列表指定字段getter值通过字典数据映射字典名称到指定字段setter
	 *
	 * @param <T>
	 * @param dictType
	 * @param list
	 * @param getter
	 * @param setter
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
