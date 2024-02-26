/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
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
package com.chestnut.stat.core;

/**
 * 统计事件类型
 */
public interface IStatEventHandler {

    static final String BEAN_PREFIX = "StatEventHandler_";

    /**
     * 事件类型，唯一标识
     */
    String getType();

    /**
     * 处理事件
     */
    void handle(StatEvent event);
}