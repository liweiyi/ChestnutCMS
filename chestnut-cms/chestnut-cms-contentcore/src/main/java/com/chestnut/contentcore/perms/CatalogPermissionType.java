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
package com.chestnut.contentcore.perms;

import cn.dev33.satoken.annotation.SaMode;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.util.CmsPrivUtils;
import com.chestnut.system.permission.IPermissionType;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 栏目权限类型
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IPermissionType.BEAN_PREFIX + CatalogPermissionType.ID)
public class CatalogPermissionType implements IPermissionType {

	public static final String ID = "Catalog";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "栏目权限";
	}

	@Override
	public String serialize(Set<String> permissionKeys) {
		Map<String, BitSet> map = new HashMap<>();
		permissionKeys.forEach(key -> {
			String[] arr = key.split(Spliter);
			if (arr.length == 3) {
				map.computeIfAbsent(arr[2], siteId -> new BitSet())
						.set(CatalogPrivItem.valueOf(arr[1]).bitIndex());
			}
		});
		return CmsPrivUtils.serializeBitSetPermission(map);
	}

	/**
	 * {<catalogId: [long]>,...}
	 */
	@Override
	public Set<String> deserialize(String json) {
		Set<String> privs = new HashSet<>();
		if (StringUtils.isNotEmpty(json)) {
			Map<String, BitSet> map = CmsPrivUtils.deserializeBitSetPermission(json);
			map.forEach((k, v) -> {
				if (!v.isEmpty()) {
					Long siteId = Long.valueOf(k);
					for (CatalogPrivItem privItem : CatalogPrivItem.values()) {
						if (v.get(privItem.bitIndex())) {
							privs.add(privItem.getPermissionKey(siteId));
						}
					}
				}
			});
		}
		return privs;
	}

	@Override
	public boolean hasPermission(List<String> permissionKeys, String json, SaMode mode) {
		Map<String,BitSet> parse = CmsPrivUtils.deserializeBitSetPermission(json);
		if (mode == SaMode.AND) {
			for (String key : permissionKeys) {
				String[] split = StringUtils.split(key, Spliter);
				BitSet bitSet = parse.get(split[2]);
				if (bitSet == null || !bitSet.get(CatalogPrivItem.valueOf(split[1]).bitIndex())) {
					return false;
				}
			}
			return true;
		} else {
			for (String key : permissionKeys) {
				String[] split = StringUtils.split(key, Spliter);
				BitSet bitSet = parse.get(split[2]);
				if (bitSet != null && bitSet.get(CatalogPrivItem.valueOf(split[1]).bitIndex())) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * 栏目权限项
	 */
	public enum CatalogPrivItem implements BitSetPrivItem {

		View(0, "查看"),

		Edit(1, "编辑"),

		Delete(2, "删除"),

		Publish(3, "发布"),

		ShowHide(4, "显示/隐藏"),

		Move(5, "移动"),

		Sort(6, "排序"),

		AddContent(7, "新增内容"),

		EditContent(8, "编辑内容"),

		DeleteContent(9, "删除内容");

		/**
		 * 权限项在bitset中的位置序号，从0开始，不可随意变更，变更后会导致原权限信息错误
		 */
		private final int bitIndex;

		private final String label;

		CatalogPrivItem(int bitIndex, String label) {
			this.bitIndex = bitIndex;
			this.label = label;
		}

		@Override
		public int bitIndex() {
			return this.bitIndex;
		}

		public String label() {
			return this.label;
		}

		public String getPermissionKey(Long catalogId) {
			return ID + ":" + this.name() + ":" + catalogId;
		}
	}
}
