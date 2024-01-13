package com.chestnut.xmodel.core.impl;

import com.chestnut.common.utils.StringUtils;
import com.chestnut.xmodel.core.IMetaControlType;
import com.chestnut.xmodel.dto.XModelFieldDataDTO;
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
    public void parseFieldValue(XModelFieldDataDTO fieldData) {
        Object value = fieldData.getValue();
        if (Objects.isNull(value) || StringUtils.isBlank(value.toString())) {
            fieldData.setValue(new String[0]);
            return;
        }
        String[] arr = StringUtils.split(value.toString(), StringUtils.COMMA);
        fieldData.setValue(arr);
    }
}
