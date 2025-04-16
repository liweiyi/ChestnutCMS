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
package com.chestnut.customform.rule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chestnut.customform.CmsCustomFormMetaModelType;
import com.chestnut.customform.domain.CmsCustomForm;
import com.chestnut.customform.domain.CmsCustomFormData;
import com.chestnut.customform.mapper.CustomFormDataMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * CustomFormLimitRule_IP
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Order(2)
@RequiredArgsConstructor
@Component(ICustomFormLimitRule.BEAN_PREFIX + CustomFormLimitRule_IP.ID)
public class CustomFormLimitRule_IP implements ICustomFormLimitRule {

    public static final String ID = "IP";

    private final CustomFormDataMapper customFormDataMapper;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "{CUSTOM_FORM.LIMIT_RULE.IP}";
    }

    @Override
    public boolean check(CmsCustomForm form, Map<String, Object> dataMap) {
        Object ipAddr = MapUtils.getString(dataMap, CmsCustomFormMetaModelType.FIELD_CLIENT_IP.getCode());
        Long count = customFormDataMapper.selectCount(new LambdaQueryWrapper<CmsCustomFormData>()
                .eq(CmsCustomFormData::getModelId, form.getFormId())
                .eq(CmsCustomFormData::getClientIp, ipAddr));
        return count == 0;
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
