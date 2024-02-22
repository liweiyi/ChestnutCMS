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
package com.chestnut.stat.service.impl;

import com.chestnut.common.redis.RedisCache;
import com.chestnut.stat.core.IStatEventHandler;
import com.chestnut.stat.core.StatEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * StatEventService
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Service
@RequiredArgsConstructor
public class StatEventService {

    private final Map<String, IStatEventHandler> eventHandlers;

    private final RedisCache redisCache;

    IStatEventHandler getStatEventHandler(String type) {
        return eventHandlers.get(IStatEventHandler.BEAN_PREFIX + type);
    }

    public void dealStatEvent(StatEvent event) {
        IStatEventHandler handler = getStatEventHandler(event.getType());
        if (Objects.isNull(handler)) {
            return;
        }
        handler.handle(event);
    }
}
