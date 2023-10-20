package com.chestnut.xmodel.core.impl;

import com.chestnut.xmodel.core.IMetaFieldValidation;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Email
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMetaFieldValidation.BEAN_PREFIX + MetaFieldValidation_Email.TYPE)
public class MetaFieldValidation_Email implements IMetaFieldValidation {

    static final String EmailPattern = "^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)*(\\.[a-zA-Z]{2,})$";

    static final String TYPE = "Email";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean validate(Object fieldValue, Map<String, Object> args) {
        if (fieldValue == null) {
            return true;
        }
        return Pattern.matches(EmailPattern, fieldValue.toString());
    }
}
