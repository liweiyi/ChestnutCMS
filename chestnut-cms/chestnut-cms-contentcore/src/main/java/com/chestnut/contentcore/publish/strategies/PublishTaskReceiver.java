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
package com.chestnut.contentcore.publish.strategies;

import com.chestnut.contentcore.publish.CmsStaticizeService;
import com.chestnut.contentcore.publish.IStaticizeType;
import com.chestnut.contentcore.publish.strategies.RedisStreamPublishStrategy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;

import java.util.Map;
import java.util.Objects;

/**
 * 发布任务消费监听
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@RequiredArgsConstructor
public class PublishTaskReceiver implements StreamListener<String, MapRecord<String, String, String>> {

    private final static Logger logger = LoggerFactory.getLogger("publish");

    private final CmsStaticizeService cmsStaticizeService;

    private final StringRedisTemplate redisTemplate;

    @Setter
    @Getter
    private Consumer consumer;

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        String stream = message.getStream();
        if (Objects.nonNull(stream)) {
            try {
                Map<String, String> map = message.getValue();
                String type = MapUtils.getString(map, "type");

                IStaticizeType staticizeType = cmsStaticizeService.getStaticizeType(type);
                if (Objects.nonNull(staticizeType)) {
                    staticizeType.staticize(map.get("id"));
                }
            } catch(Exception e) {
                logger.error("Publish err.", e);
            } finally {
                redisTemplate.opsForStream().acknowledge(
                        stream,
                        RedisStreamPublishStrategy.PublishConsumerGroup,
                        message.getId().getValue()
                );
                redisTemplate.opsForStream().delete(stream, message.getId().getValue());
            }
        }
    }
}
