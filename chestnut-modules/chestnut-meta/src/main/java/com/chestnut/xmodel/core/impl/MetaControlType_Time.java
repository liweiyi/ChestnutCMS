package com.chestnut.xmodel.core.impl;

import com.chestnut.xmodel.core.IMetaControlType;
import org.springframework.stereotype.Component;

/**
 * 元数据模型字段类型：时间选择框
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMetaControlType.BEAN_PREFIX + MetaControlType_Time.TYPE)
public class MetaControlType_Time implements IMetaControlType {

    public static final String TYPE = "time";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getName() {
        return "{META.CONTROL_TYPE." + TYPE + "}";
    }
}
