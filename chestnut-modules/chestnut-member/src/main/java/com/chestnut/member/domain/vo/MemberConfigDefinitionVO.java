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
package com.chestnut.member.domain.vo;

import com.chestnut.common.annotation.XComment;
import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.member.config.IMemberConfig;
import com.chestnut.member.config.MemberConfigControlType;
import lombok.Getter;
import lombok.Setter;

/**
 * 会员配置项定义。
 */
@XComment("{API.DOC.MEMBER.CONFIG_DEFINITION}")
@Getter
@Setter
public class MemberConfigDefinitionVO {

    @XComment("{API.DOC.MEMBER.CONFIG_KEY}")
    private String id;

    @XComment("{API.DOC.MEMBER.CONFIG_NAME}")
    private String name;

    @XComment("{API.DOC.MEMBER.CONFIG_CONTROL_TYPE}")
    private MemberConfigControlType controlType;

    @XComment("{API.DOC.MEMBER.CONFIG_MESSAGE_TYPE}")
    private String messageType;

    @XComment("{API.DOC.MEMBER.CONFIG_DEFAULT_VALUE}")
    private Object defaultValue;

    @XComment("{API.DOC.MEMBER.CONFIG_ORDER}")
    private int order;

    public static MemberConfigDefinitionVO of(IMemberConfig<?> config) {
        MemberConfigDefinitionVO definition = new MemberConfigDefinitionVO();
        definition.setId(config.getId());
        definition.setName(I18nUtils.get(config.getName()));
        definition.setControlType(config.getControlType());
        definition.setMessageType(config.getMessageType());
        definition.setDefaultValue(config.getDefaultValue());
        definition.setOrder(config.getOrder());
        return definition;
    }
}
