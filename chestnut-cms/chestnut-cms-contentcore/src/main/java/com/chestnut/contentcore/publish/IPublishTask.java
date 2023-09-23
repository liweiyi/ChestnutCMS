package com.chestnut.contentcore.publish;

import org.apache.poi.ss.formula.functions.T;

import java.util.Map;

/**
 * <TODO description class purpose>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IPublishTask {

    String BeanPrefix = "PublishTask_";

    String getType();

    void publish(Map<String, String> dataMap);
}
