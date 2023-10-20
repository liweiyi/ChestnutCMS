package com.chestnut.xmodel.core.impl;

import com.chestnut.common.utils.NumberUtils;
import com.chestnut.xmodel.core.IMetaFieldValidation;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * 整数校验
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMetaFieldValidation.BEAN_PREFIX + MetaFieldValidation_Int.TYPE)
public class MetaFieldValidation_Int implements IMetaFieldValidation {

    static final String TYPE = "Int";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean validate(Object fieldValue, Map<String, Object> args) {
        return Objects.isNull(fieldValue) || NumberUtils.isDigits(fieldValue.toString());
    }
}
