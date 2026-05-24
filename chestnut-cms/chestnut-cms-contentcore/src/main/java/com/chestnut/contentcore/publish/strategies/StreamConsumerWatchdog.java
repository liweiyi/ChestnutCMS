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
package com.chestnut.contentcore.publish.strategies;

import com.chestnut.contentcore.config.properties.CMSPublishProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RStream;
import org.redisson.api.RedissonClient;
import org.redisson.api.StreamConsumer;
import org.redisson.client.codec.StringCodec;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * RedisStream 发布订阅看门狗：
 * <p>
 * 定时检查 Redis 中消费者组的 consumer.idleTime，当所有 consumer 长时间未拉取消息
 * 且 stream 仍有积压时，判定监听容器的订阅已死，自动 stop/start 恢复。
 * </p>
 * <p>
 * 主防线是 {@link RedisStreamPublishStrategy} 中的
 * {@code cancelSubscriptionOnError(false)} + 自定义 errorHandler，本看门狗仅作兜底。
 * 仅在 {@code chestnut.cms.publish.strategy=RedisStream} 时生效。
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = CMSPublishProperties.PREFIX,
        name = "strategy",
        havingValue = RedisStreamPublishStrategy.ID
)
public class StreamConsumerWatchdog {

    private final CMSPublishProperties properties;

    private final RedissonClient redissonClient;

    private final StreamMessageListenerContainer<?, ?> streamMessageListenerContainer;

    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;

    private final AtomicBoolean shuttingDown = new AtomicBoolean(false);

    /** 一小时滑动窗口内的重启时间戳，同步由 synchronized 保证 */
    private final Deque<Long> recentRestarts = new ArrayDeque<>();

    private volatile ScheduledFuture<?> scheduledFuture;

    @PostConstruct
    public void start() {
        CMSPublishProperties.Watchdog cfg = properties.getWatchdog();
        if (!cfg.isEnabled()) {
            log.info("RedisStream publish watchdog disabled by configuration");
            return;
        }
        Duration interval = cfg.getCheckInterval();
        PeriodicTrigger trigger = new PeriodicTrigger(interval);
        trigger.setFixedRate(false);
        this.scheduledFuture = threadPoolTaskScheduler.schedule(this::check, trigger);
        log.info("RedisStream publish watchdog started, interval={}s, idleThreshold={}s, maxRestartPerHour={}",
                interval.getSeconds(),
                cfg.getIdleTimeThreshold().getSeconds(),
                cfg.getMaxRestartPerHour());
    }

    @EventListener(ContextClosedEvent.class)
    public void onContextClosed(ContextClosedEvent event) {
        shuttingDown.set(true);
        cancelSchedule();
    }

    @PreDestroy
    public void stop() {
        shuttingDown.set(true);
        cancelSchedule();
    }

    private void cancelSchedule() {
        ScheduledFuture<?> f = this.scheduledFuture;
        if (f != null) {
            f.cancel(false);
        }
    }

    /**
     * 单次检查入口（包级可见以便测试）。
     */
    void check() {
        if (shuttingDown.get()) {
            return;
        }
        try {
            if (!streamMessageListenerContainer.isRunning()) {
                log.debug("StreamMessageListenerContainer is not running, skip watchdog check");
                return;
            }
            RStream<String, String> stream = redissonClient.getStream(
                    RedisStreamPublishStrategy.PUBLISH_STREAM_NAME, StringCodec.INSTANCE);
            long size;
            try {
                size = stream.size();
            } catch (Exception e) {
                log.debug("Watchdog failed to read stream size, skip this round: {}", e.toString());
                return;
            }
            if (size <= 0) {
                // 没有积压消息，即便 consumer 长时间空闲也属正常
                return;
            }

            List<StreamConsumer> consumers;
            try {
                consumers = stream.listConsumers(RedisStreamPublishStrategy.PUBLISH_CONSUMER_GROUP);
            } catch (Exception e) {
                log.debug("Watchdog failed to list consumers, skip this round: {}", e.toString());
                return;
            }

            if (consumers == null || consumers.isEmpty()) {
                tryRestart("no active consumers registered in group (stream size=" + size + ")");
                return;
            }

            long minIdle = Long.MAX_VALUE;
            for (StreamConsumer c : consumers) {
                long idle = c.getIdleTime();
                if (idle < minIdle) {
                    minIdle = idle;
                }
            }
            long thresholdMs = properties.getWatchdog().getIdleTimeThreshold().toMillis();
            if (minIdle > thresholdMs) {
                tryRestart(String.format(
                        "min consumer idleTime=%dms exceeds threshold=%dms (stream size=%d, consumers=%d)",
                        minIdle, thresholdMs, size, consumers.size()));
            }
        } catch (Throwable t) {
            log.warn("RedisStream publish watchdog check error", t);
        }
    }

    private void tryRestart(String reason) {
        if (shuttingDown.get()) {
            return;
        }
        int max = properties.getWatchdog().getMaxRestartPerHour();
        long now = System.currentTimeMillis();
        long windowStart = now - TimeUnit.HOURS.toMillis(1);
        int recentCount;
        synchronized (recentRestarts) {
            while (!recentRestarts.isEmpty() && recentRestarts.peekFirst() < windowStart) {
                recentRestarts.pollFirst();
            }
            if (recentRestarts.size() >= max) {
                log.error("RedisStream subscription suspected dead ({}), but restart rate limit reached ({}/hour). Manual intervention required.",
                        reason, max);
                return;
            }
            recentRestarts.addLast(now);
            recentCount = recentRestarts.size();
        }

        log.warn("RedisStream subscription suspected dead: {}. Restarting listener container (count={}/hour within window)...",
                reason, recentCount);
        try {
            streamMessageListenerContainer.stop();
            Thread.sleep(500);
            if (shuttingDown.get()) {
                log.info("Context is closing, skip re-start after stop");
                return;
            }
            streamMessageListenerContainer.start();
            log.info("RedisStream listener container restarted. running={}",
                    streamMessageListenerContainer.isRunning());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.warn("Watchdog restart interrupted");
        } catch (Throwable t) {
            log.error("Failed to restart RedisStream listener container", t);
        }
    }
}
