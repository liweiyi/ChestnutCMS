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
package com.chestnut.message.core;

import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * MessageTypeInfo
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class MessageTypeInfo {

    private String label;

    private String value;

    private List<PropDefinition> props = new ArrayList<>();

    public static MessageTypeInfo of(IMessageType<?> messageType) {
        MessageTypeInfo info = new MessageTypeInfo();
        info.label = I18nUtils.get(messageType.getName());
        info.value = messageType.getType();
        Field[] fields = messageType.getPropsClass().getDeclaredFields();
        List<PropDefinition> props = new ArrayList<>(fields.length);
        for (Field field : fields) {
            if (field.isAnnotationPresent(Control.class)) {
                Control control = field.getAnnotation(Control.class);
                PropDefinition prop = new PropDefinition();
                prop.label = I18nUtils.get(control.label());
                prop.key = field.getName();
                prop.type = control.type().name();
                if (control.options().length > 0) {
                    prop.options = Stream.of(control.options()).map(option ->
                            new BaseRestController.SelectOption(option.value(), I18nUtils.get(option.label()))
                    ).toList();
                }
                prop.pairsKeyName = I18nUtils.get(control.pairsKeyName());
                prop.pairsValueName = I18nUtils.get(control.pairsValueName());
                props.add(prop);
            }
        }
        info.setProps(props);
        return info;
    }

    @Getter
    @Setter
    public static class PropDefinition {
        private String label;
        private String key;
        private String type;
        private List<BaseRestController.SelectOption> options;
        private String pairsKeyName;
        private String pairsValueName;
    }
}
