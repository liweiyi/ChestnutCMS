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
package com.chestnut.customform.rule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chestnut.customform.CmsCustomFormMetaModelType;
import com.chestnut.customform.domain.CmsCustomForm;
import com.chestnut.customform.domain.CmsCustomFormData;
import com.chestnut.customform.mapper.CustomFormDataMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * CustomFormLimitRule_UUID
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Order(3)
@RequiredArgsConstructor
@Component(ICustomFormLimitRule.BEAN_PREFIX + CustomFormLimitRule_UUID.ID)
public class CustomFormLimitRule_UUID implements ICustomFormLimitRule {

    public static final String ID = "UUID";

    private final CustomFormDataMapper customFormDataMapper;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "{CUSTOM_FORM.LIMIT_RULE.UUID}";
    }

    @Override
    public boolean check(CmsCustomForm form, Map<String, Object> dataMap) {
        Object uuid = MapUtils.getString(dataMap, CmsCustomFormMetaModelType.FIELD_UUID.getCode());
        Long count = customFormDataMapper.selectCount(new LambdaQueryWrapper<CmsCustomFormData>()
                .eq(CmsCustomFormData::getModelId, form.getFormId())
                .eq(CmsCustomFormData::getUuid, uuid));
        return count == 0;
    }

    @Override
    public int getOrder() {
        return 3;
    }
}
