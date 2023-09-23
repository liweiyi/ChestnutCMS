package com.chestnut.xmodel.core.impl;

import com.chestnut.common.utils.StringUtils;
import com.chestnut.xmodel.core.IMetaControlType;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 元数据模型字段类型：多选框
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMetaControlType.BEAN_PREFIX + MetaControlType_Checkbox.TYPE)
public class MetaControlType_Checkbox implements IMetaControlType {

    public static final String TYPE = "checkbox";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getName() {
        return "{META.CONTROL_TYPE." + TYPE + "}";
    }

    @Override
    public ParseResult parseFieldValue(Object value) {
        if (Objects.isNull(value)) {
            return null;
        }
        if (StringUtils.isBlank(value.toString())) {
            return new ParseResult(new Object[] { new String[0] });
        }
        String[] arr = StringUtils.split(value.toString(), StringUtils.COMMA);
        return new ParseResult(new Object[] { arr });
    }
}
