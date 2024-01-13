package com.chestnut.xmodel.core;

import com.chestnut.xmodel.dto.XModelFieldDataDTO;

/**
 * 元数据模型字段类型
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IMetaControlType {

    String BEAN_PREFIX = "MetaControlType_";

    /**
     * 类型
     */
    String getType();

    /**
     * 名称
     */
    String getName();

    /**
     * 处理组件关联字段数据
     */
    default void parseFieldValue(XModelFieldDataDTO fieldData) {
    }
}
