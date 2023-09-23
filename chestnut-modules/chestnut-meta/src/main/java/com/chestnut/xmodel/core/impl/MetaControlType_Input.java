package com.chestnut.xmodel.core.impl;

import com.chestnut.xmodel.core.IMetaControlType;
import org.springframework.stereotype.Component;

/**
 * 元数据模型字段类型：单行文本
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMetaControlType.BEAN_PREFIX + MetaControlType_Input.TYPE)
public class MetaControlType_Input implements IMetaControlType {

    public static final String TYPE = "input";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getName() {
        return "{META.CONTROL_TYPE." + TYPE + "}";
    }
}
