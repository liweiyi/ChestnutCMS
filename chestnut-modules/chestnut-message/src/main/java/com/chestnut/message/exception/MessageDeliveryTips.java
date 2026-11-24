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
package com.chestnut.message.exception;

import com.chestnut.common.exception.TipMessage;

/**
 * 消息发送和任务失败提示
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public enum MessageDeliveryTips implements TipMessage {

    /**
     * 消息发送失败：配置={0}，类型={1}，名称={2}
     */
    NOTIFY_FAILED,

    /**
     * 消息事件测试失败：{0}
     */
    TEST_FAILED,

    /**
     * 消息模板加载失败
     */
    TEMPLATE_LOAD_FAILED;

    @Override
    public String value() {
        return "TIP.MSG.DELIVERY." + name();
    }
}
