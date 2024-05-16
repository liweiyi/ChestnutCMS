package com.chestnut.contentcore.publish.strategies;

import com.chestnut.contentcore.config.properties.CMSPublishProperties;
import com.chestnut.contentcore.publish.CmsStaticizeService;
import com.chestnut.contentcore.publish.IPublishStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

/**
 * 发布策略：Redis Stream
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = CMSPublishProperties.PREFIX, name = "strategy", havingValue = RedisStreamPublishStrategy.ID)
public class RedisStreamPublishStrategy implements IPublishStrategy {

    public static final String ID = "RedisStream";

    public static final String PublishStreamName = "ChestnutCMSPublishStream";

    public static final String PublishConsumerGroup = "ChestnutCMSPublishConsumerGroup";

    private final CMSPublishProperties properties;

    private final StringRedisTemplate redisTemplate;

    private final CmsStaticizeService cmsStaticizeService;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void publish(String dataType, String dataId) {
        MapRecord<String, String, String> record = MapRecord.create(PublishStreamName, Map.of(
                "type", dataType,
                "id", dataId
        ));
        redisTemplate.opsForStream().add(record);
    }

    @Override
    public long getTaskCount() {
        StreamInfo.XInfoStream info = redisTemplate.opsForStream().info(PublishStreamName);
        return info.streamLength();
    }

    @Override
    public void cleanTasks() {
		try {
            redisTemplate.delete(PublishStreamName);
			redisTemplate.opsForStream().createGroup(PublishStreamName, PublishConsumerGroup);
		} catch (Exception ignored) {
		}
    }

    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer() {
        // 启动清理消息队列数据
        if (properties.isClearOnStart()) {
            redisTemplate.delete(PublishStreamName);
        }
        // 监听容器配置
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> streamMessageListenerContainerOptions = StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                .builder()
                .batchSize(10) // 一次拉取消息数量
                .pollTimeout(Duration.ofSeconds(2)) // 拉取消息超时时间
                .executor(cmsPublishThreadPoolTaskExecutor())
                .build();
        // 创建监听容器
        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container = StreamMessageListenerContainer
                .create(redisTemplate.getRequiredConnectionFactory(), streamMessageListenerContainerOptions);
        //创建消费者组
        try {
            redisTemplate.opsForStream().createGroup(PublishStreamName, PublishConsumerGroup);
        } catch (Exception e) {
            log.info("消费者组:{} 已存在", PublishConsumerGroup);
        }
        // 添加消费者
        for (int i = 0; i < properties.getConsumerCount(); i++) {
            Consumer consumer = Consumer.from(PublishConsumerGroup, "cms-publish-consumer-" + i);
            PublishTaskReceiver publishTaskReceiver = new PublishTaskReceiver(cmsStaticizeService, redisTemplate);
            publishTaskReceiver.setConsumer(consumer);
            container.receive(consumer, StreamOffset.create(PublishStreamName, ReadOffset.lastConsumed()), publishTaskReceiver);
        }
        container.start();
        return container;
    }

    @Bean
    ThreadPoolTaskExecutor cmsPublishThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix(properties.getPool().getThreadNamePrefix());
        executor.setCorePoolSize(properties.getPool().getCoreSize());
        executor.setQueueCapacity(properties.getPool().getQueueCapacity());
        executor.setMaxPoolSize(properties.getPool().getMaxSize());
        executor.setKeepAliveSeconds((int) properties.getPool().getKeepAlive().getSeconds());
        executor.setAllowCoreThreadTimeOut(this.properties.getPool().isAllowCoreThreadTimeout());
        executor.setWaitForTasksToCompleteOnShutdown(properties.getShutdown().isAwaitTermination());
        executor.setAwaitTerminationSeconds((int) properties.getShutdown().getAwaitTerminationPeriod().toSeconds());
        log.info("Cms publish task executor initialize: {}", executor.getThreadNamePrefix());
        executor.initialize();
        return executor;
    }
}
