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
package com.chestnut.contentcore.fixed.config;

import com.chestnut.common.utils.NumberUtils;
import com.chestnut.common.utils.SpringUtils;
import com.chestnut.system.fixed.FixedConfig;
import com.chestnut.system.service.ISysConfigService;
import org.springframework.stereotype.Component;

@Component(FixedConfig.BEAN_PREFIX + MaxCatalogPagePublishTask.ID)
public class MaxCatalogPagePublishTask extends FixedConfig {

    public static final String ID = "MaxCatalogPagePublishTask";

    private static final int DEFAULT_VALUE = 5;

    private static final ISysConfigService configService = SpringUtils.getBean(ISysConfigService.class);

    public MaxCatalogPagePublishTask() {
        super(ID, "{CONFIG." + ID + "}", String.valueOf(DEFAULT_VALUE), null);
    }

    public static int getValue() {
        String configValue = configService.selectConfigByKey(ID);
        return NumberUtils.toInt(configValue, DEFAULT_VALUE);
    }
}
