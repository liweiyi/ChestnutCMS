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
package com.chestnut.system.security;

import tools.jackson.databind.node.ObjectNode;

/**
 * 后端第三方登录提供者
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IThirdPartyLoginProvider {

    String BEAN_PREFIX = "ThirdPartyLoginProvider_";

    /**
     * 唯一标识
     */
    String getId();

    /**
     * 名称
     */
    String getName();

    /**
     * 配置脱敏处理
     */
    void dealSensitive(ObjectNode configProps);

    /**
     * 更新登录配置
     */
    void updateConfigProps(ObjectNode oldProps, ObjectNode newProps);
}
