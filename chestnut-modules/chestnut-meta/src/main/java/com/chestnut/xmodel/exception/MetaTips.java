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
package com.chestnut.xmodel.exception;

import com.chestnut.common.exception.TipMessage;

/**
 * 元数据模型操作提示
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public enum MetaTips implements TipMessage {

    /**
     * 元数据模型未定义主键
     */
    PRIMARY_KEY_MISSING,

    /**
     * 元数据模型主键“{0}”不能为空
     */
    PRIMARY_KEY_REQUIRED;

    @Override
    public String value() {
        return "TIP.META.DATA." + name();
    }
}
