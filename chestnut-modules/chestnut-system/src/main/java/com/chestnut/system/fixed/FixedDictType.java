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
package com.chestnut.system.fixed;

import java.util.ArrayList;
import java.util.List;

import com.chestnut.common.utils.StringUtils;

import lombok.Getter;
import lombok.Setter;

/**
 * 固定字典数据，此类定义的数据为系统运行必须，不可更改，系统模块的字典数据持久化管理会自动持久化此数据到数据库方便使用。
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class FixedDictType {
	
	public static final String BEAN_PREFIX = "FIXED_DICT.";
	
	/**
	 * 字典类型唯一标识
	 */
	private String dictType;

	/**
	 * 字典类型名称
	 */
	private String dictName;
	
	/**
	 * 是否允许动态添加字典数据项
	 */
	private boolean allowAdd;

	/**
	 * 字典数据列表
	 */
	private List<DictData> dataList = new ArrayList<>();
	
	/**
	 * 备注
	 */
	private String remark;
	
	public FixedDictType() {}

	public FixedDictType(String dictType, String dictName) {
		this(dictType, dictName, false, null);
	}

	public FixedDictType(String dictType, String dictName, String remark) {
		this(dictType, dictName, false, remark);
	}
	
	public FixedDictType(String dictType, String dictName, boolean allowAdd) {
		this(dictType, dictName, allowAdd, null);
	}
	
	public FixedDictType(String dictType, String dictName, boolean allowAdd, String remark) {
		this.dictType = dictType;
		this.dictName = dictName;
		this.allowAdd = allowAdd;
		this.remark = remark;
	}
	
	protected void addDictData(String dictLabel, String dictValue, int dictSort) {
		this.addDictData(dictLabel, dictValue, dictSort, null);
	}
	
	protected void addDictData(String dictLabel, String dictValue, int dictSort, String remark) {
		if (this.dataList.stream().anyMatch(d -> d.getValue().equals(dictValue))) {
			throw new RuntimeException(StringUtils.messageFormat("固定字典【{0}】数据值重复：{1}", dictType, dictValue));
		}
		this.dataList.add(new DictData(dictLabel, dictValue, dictSort, remark));
	}
	
	@Getter
	@Setter
	public static class DictData {

	    private String label;

	    private String value;
		
	    private long sort;
	    
	    private String remark;
	    
	    public DictData() {}

	    public DictData(String dictLabel, String dictValue, long dictSort) {
			this(dictLabel, dictValue, dictSort, null);
		}

	    public DictData(String dictLabel, String dictValue, long dictSort, String remark) {
			this.label = dictLabel;
			this.value = dictValue;
			this.sort = dictSort;
			this.remark = remark;
		}
	}
}
