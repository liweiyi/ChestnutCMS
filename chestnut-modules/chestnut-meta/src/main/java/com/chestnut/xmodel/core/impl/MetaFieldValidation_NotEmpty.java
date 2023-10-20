package com.chestnut.xmodel.core.impl;

import com.chestnut.xmodel.core.IMetaFieldValidation;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * 非空判断
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMetaFieldValidation.BEAN_PREFIX + MetaFieldValidation_NotEmpty.TYPE)
public class MetaFieldValidation_NotEmpty implements IMetaFieldValidation {

    static final String TYPE = "NotEmpty";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean validate(Object fieldValue, Map<String, Object> args) {
        return Objects.nonNull(fieldValue) && !fieldValue.toString().isBlank();
    }
}
