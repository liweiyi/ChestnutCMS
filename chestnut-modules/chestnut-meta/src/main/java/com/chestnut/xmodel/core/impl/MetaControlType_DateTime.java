package com.chestnut.xmodel.core.impl;

import com.chestnut.xmodel.core.IMetaControlType;
import org.springframework.stereotype.Component;

/**
 * 元数据模型字段类型：日期时间选择框
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMetaControlType.BEAN_PREFIX + MetaControlType_DateTime.TYPE)
public class MetaControlType_DateTime implements IMetaControlType {

    public static final String TYPE = "datetime";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getName() {
        return "{META.CONTROL_TYPE." + TYPE + "}";
    }
}
