package com.chestnut.contentcore.publish;

/**
 * IPublishStrategy
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IPublishStrategy {

    /**
     * 发布策略ID
     */
    String getId();

    /**
     * 创建发布任务
     *
     * @param dataType
     * @param dataId
     */
    void publish(String dataType, String dataId);

    long getTaskCount();

    void cleanTasks();
}
