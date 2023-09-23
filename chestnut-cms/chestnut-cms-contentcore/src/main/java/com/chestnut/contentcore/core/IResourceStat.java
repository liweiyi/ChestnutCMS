package com.chestnut.contentcore.core;

import java.util.Map;

/**
 * 资源引用统计
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IResourceStat {

    /**
     * 查找资源引用次数
     */
    Map<Long, Integer> findQuotedResource();
}
