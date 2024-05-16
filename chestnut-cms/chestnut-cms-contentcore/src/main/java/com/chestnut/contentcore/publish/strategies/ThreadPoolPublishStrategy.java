package com.chestnut.contentcore.publish.strategies;

import com.chestnut.contentcore.config.properties.CMSPublishProperties;
import com.chestnut.contentcore.publish.CmsStaticizeService;
import com.chestnut.contentcore.publish.IPublishStrategy;
import com.chestnut.contentcore.publish.IStaticizeType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 发布策略：ThreadPool
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = CMSPublishProperties.PREFIX, name = "strategy", havingValue = ThreadPoolPublishStrategy.ID)
public class ThreadPoolPublishStrategy implements IPublishStrategy, CommandLineRunner {

    public static final String ID = "ThreadPool";

    private final CMSPublishProperties properties;

    private final CmsStaticizeService cmsStaticizeService;

    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void publish(String dataType, String dataId) {
        IStaticizeType staticizeType = cmsStaticizeService.getStaticizeType(dataType);
        if (Objects.nonNull(staticizeType)) {
            threadPoolTaskExecutor.execute(() -> {
                staticizeType.staticize(dataId);
            });
        }
    }

    @Override
    public long getTaskCount() {
        // 返回线程池队列信息
        return threadPoolTaskExecutor.getQueueSize();
    }

    @Override
    public void cleanTasks() {
        // 清空线程池队列
        threadPoolTaskExecutor.getThreadPoolExecutor().getQueue().clear();
    }

    @Override
    public void run(String... args) throws Exception {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix(properties.getPool().getThreadNamePrefix());
        executor.setCorePoolSize(properties.getPool().getCoreSize());
        executor.setQueueCapacity(properties.getPool().getQueueCapacity());
        executor.setMaxPoolSize(properties.getPool().getMaxSize());
        executor.setKeepAliveSeconds((int) properties.getPool().getKeepAlive().getSeconds());
        executor.setAllowCoreThreadTimeOut(this.properties.getPool().isAllowCoreThreadTimeout());
        executor.setWaitForTasksToCompleteOnShutdown(properties.getShutdown().isAwaitTermination());
        executor.setAwaitTerminationSeconds((int) properties.getShutdown().getAwaitTerminationPeriod().toSeconds());
        executor.initialize();
        this.threadPoolTaskExecutor = executor;
    }
}
