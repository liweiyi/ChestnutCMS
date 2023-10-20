package com.chestnut.xmodel.core.impl;

import com.chestnut.common.utils.NumberUtils;
import com.chestnut.xmodel.core.IMetaFieldValidation;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * 数字校验
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMetaFieldValidation.BEAN_PREFIX + MetaFieldValidation_Number.TYPE)
public class MetaFieldValidation_Number implements IMetaFieldValidation {

    static final String TYPE = "Number";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean validate(Object fieldValue, Map<String, Object> args) {
        return Objects.isNull(fieldValue) || NumberUtils.isCreatable(fieldValue.toString());
    }
}
