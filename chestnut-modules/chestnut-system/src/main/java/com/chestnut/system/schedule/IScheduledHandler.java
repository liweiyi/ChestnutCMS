package com.chestnut.system.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 本地轻量级定时任务接口
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IScheduledHandler {

    String BEAN_PREFIX = "ScheduledHandler_";

    Logger logger = LoggerFactory.getLogger("cron");

    /**
     * 定时任务ID唯一标识
     *
     * @return
     */
    String getId();

    /**
     * 定时任务名称
     *
     * @return
     */
    String getName();

    /**
     * 执行任务
     */
    void exec() throws Exception;
}
