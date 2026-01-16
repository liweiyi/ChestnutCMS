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
package com.chestnut.exmodel;

import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.util.InternalUrlUtils;
import com.chestnut.xmodel.core.IMetaControlType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * 元数据模型字段类型：资源文件上传
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMetaControlType.BEAN_PREFIX + MetaControlType_CmsResource.TYPE)
public class MetaControlType_CmsResource implements IMetaControlType {

    public static final String TYPE = "CMSResource";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getName() {
        return "{META.CONTROL_TYPE." + TYPE + "}";
    }

    @Override
    public String valueAsString(Object obj) {
        if (Objects.isNull(obj)) {
            return "[]";
        }
        String jsonStr = JacksonUtils.to(obj);
        List<CmsResourceMetaValue> list = JacksonUtils.fromList(jsonStr, CmsResourceMetaValue.class);
        if (Objects.nonNull(list)) {
            list.forEach(v -> v.src = null);
        }
        jsonStr = JacksonUtils.to(list);
        return jsonStr;
    }

    @Override
    public Object stringAsValue(String valueStr) {
        if (StringUtils.isBlank(valueStr)) {
            return List.of();
        }
        if (JacksonUtils.isJson(valueStr)) {
            List<CmsResourceMetaValue> metaValue = JacksonUtils.fromList(valueStr, CmsResourceMetaValue.class);
            if (Objects.nonNull(metaValue)) {
                metaValue.forEach(item -> item.setSrc(InternalUrlUtils.getActualPreviewUrl(item.getPath())));
            }
            return metaValue;
        } else {
            CmsResourceMetaValue metaValue = new CmsResourceMetaValue();
            metaValue.setPath(valueStr);
            return List.of(metaValue);
        }
    }

    @Setter
    @Getter
    public static class CmsResourceMetaValue {

        private String name;

        private String path;

        private String src;
    }
}
