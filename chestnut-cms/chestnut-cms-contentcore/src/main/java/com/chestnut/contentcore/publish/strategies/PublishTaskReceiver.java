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
package com.chestnut.contentcore.publish.strategies;

import com.chestnut.contentcore.publish.CmsStaticizeService;
import com.chestnut.contentcore.publish.IStaticizeType;
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
        if (Objects.isNull(stream)) {
            logger.warn("Publish task consumer message fail: steam is null.");
            return;
        }
        try {
            Map<String, String> map = message.getValue();
            String type = MapUtils.getString(map, "type");
            String id = map.get("id");
            
            if (Objects.isNull(type) || Objects.isNull(id)) {
                logger.warn("Message format error, missing type or id: {}", map);
            } else {
                IStaticizeType staticizeType = cmsStaticizeService.getStaticizeType(type);
                if (Objects.nonNull(staticizeType)) {
                    staticizeType.staticize(id);
                    logger.debug("Publish task processed successfully: type={}, id={}", type, id);
                } else {
                    logger.warn("Staticize type not found: {}", type);
                }
            }
        } catch(Exception e) {
            logger.error("Publish task processing failed: stream={}, messageId={}, type={}, id={}", 
                    stream, message.getId().getValue(), 
                    MapUtils.getString(message.getValue(), "type"),
                    message.getValue().get("id"), e);
        } finally {
            try {
                redisTemplate.opsForStream().acknowledge(
                        stream,
                        RedisStreamPublishStrategy.PUBLISH_CONSUMER_GROUP,
                        message.getId().getValue()
                );
                redisTemplate.opsForStream().delete(stream, message.getId().getValue());
            } catch (Exception e) {
                logger.error("Failed to ACK message: stream={}, messageId={}", stream, message.getId().getValue(), e);
            }
        }
    }
}
