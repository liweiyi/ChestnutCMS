package com.chestnut.xmodel.core.impl;

import com.chestnut.xmodel.core.IMetaFieldValidation;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * 手机号码校验
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMetaFieldValidation.BEAN_PREFIX + MetaFieldValidation_PhoneNumber.TYPE)
public class MetaFieldValidation_PhoneNumber implements IMetaFieldValidation {

    static final String PhoneNumberPattern = "^1[3-9]\\\\d{9}$";

    static final String TYPE = "PhoneNumber";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean validate(Object fieldValue, Map<String, Object> args) {
        if (fieldValue == null) {
            return true;
        }
        return Pattern.matches(PhoneNumberPattern, fieldValue.toString());
    }
}
