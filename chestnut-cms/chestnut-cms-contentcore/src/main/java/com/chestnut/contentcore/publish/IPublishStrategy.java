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
     * @param dataType 数据类型
     * @param dataId 数据ID
     */
    void publish(String dataType, String dataId);

    /**
     * 获取待处理发布任务数量
     */
    long getTaskCount();

    /**
     * 清空所有待处理发布任务
     */
    void cleanTasks();
}
