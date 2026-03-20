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
package com.chestnut.contentcore.fixed.dict;

import com.chestnut.common.utils.SpringUtils;
import com.chestnut.system.fixed.FixedDictType;
import com.chestnut.system.service.ISysDictTypeService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 内容复制类型
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(FixedDictType.BEAN_PREFIX + ContentCopyType.TYPE)
public class ContentCopyType extends FixedDictType {

	public static final String TYPE = "CMSContentCopyType";

    public static final int NONE = 0;

    /**
     * 独立复制，完整拷贝内容所有信息，拷贝的内容变更与源内容无关，仅仅记录来源
     */
	public static final int Independency = 1;

    /**
     * 映射，仅拷贝基础内容信息，可独立修改基础信息，也就是CmsContent表的数据可独立修改，内容详情也就是扩展表共享自来源不可修改
     */
	public static final int Mapping = 2;

	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);

	public ContentCopyType() {
		super(TYPE, "{DICT." + TYPE + "}", false);
		super.addDictData("{DICT." + TYPE + "." + Independency + "}", String.valueOf(Independency), 1);
		super.addDictData("{DICT." + TYPE + "." + Mapping + "}", String.valueOf(Mapping), 2);
	}

	public static <T> void decode(List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		dictTypeService.decode(TYPE, list, getter, setter);
	}

    /**
     * 是否独立复制内容
     */
    public static boolean isIndependency(Integer v) {
        return Objects.equals(v, Independency);
    }

    /**
     * 是否映射内容
     */
    public static boolean isMapping(Integer v) {
        return Objects.equals(v, Mapping);
    }

    public record ContentCopyInfo(int copyType, long copyId, String sourceCatalogName, String sourceSiteName) {

        public static ContentCopyInfo of(int copyType, long copyId) {
            return new ContentCopyInfo(copyType, copyId, "", "");
        }
    }
}
