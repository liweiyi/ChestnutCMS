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

import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.NumberUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.xmodel.core.IMetaControlType;
import com.chestnut.xmodel.dto.XModelFieldDataDTO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 元数据模型字段控件类型：内容选择
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IMetaControlType.BEAN_PREFIX + MetaControlType_ContentSelect.TYPE)
public class MetaControlType_ContentSelect implements IMetaControlType {

    public static final String TYPE = "CMSContentSelect";

    private final IContentService contentService;

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
        return JacksonUtils.to(obj);
    }

    @Override
    public Object stringAsValue(String valueStr) {
        if (StringUtils.isBlank(valueStr)) {
            return new ContentSelectMetaValue();
        }
        ContentSelectMetaValue v;
        if (NumberUtils.isDigits(valueStr)) {
            v = new ContentSelectMetaValue();
            v.setContentId(Long.valueOf(valueStr));
        } else {
            v = JacksonUtils.from(valueStr, ContentSelectMetaValue.class);
        }
        if (IdUtils.validate(v.getContentId())) {
            Optional<CmsContent> content = contentService.dao().lambdaQuery()
                    .select(List.of(CmsContent::getContentId, CmsContent::getTitle))
                    .eq(CmsContent::getContentId, v.getContentId()).oneOpt();
            if (content.isEmpty()) {
                v.setContentId(0L);
            } else {
                v.setTitle(content.get().getTitle());
            }
        }
        return v;
    }

    @Getter
    @Setter
    public static class ContentSelectMetaValue {

        private Long contentId = 0L;

        private String title;
    }
}
