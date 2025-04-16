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

import com.chestnut.customform.domain.CmsCustomForm;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * CustomFormLimitRule_Unlimited
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Order(1)
@RequiredArgsConstructor
@Component(ICustomFormLimitRule.BEAN_PREFIX + CustomFormLimitRule_Unlimited.ID)
public class CustomFormLimitRule_Unlimited implements ICustomFormLimitRule {

    public static final String ID = "Unlimited";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "{CUSTOM_FORM.LIMIT_RULE.UNLIMITED}";
    }

    @Override
    public boolean check(CmsCustomForm form, Map<String, Object> dataMap) {
        return true;
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
