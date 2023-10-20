package com.chestnut.xmodel.core.impl;

import com.chestnut.xmodel.core.IMetaFieldValidation;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 * 日期时间格式：yyyy-MM-dd HH:mm:ss
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMetaFieldValidation.BEAN_PREFIX + MetaFieldValidation_DateTime.TYPE)
public class MetaFieldValidation_DateTime implements IMetaFieldValidation {

    static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static final String TYPE = "DateTime";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean validate(Object fieldValue, Map<String, Object> args) {
        if (fieldValue == null) {
            return true;
        }
        try {
            LocalDateTime parse = LocalDateTime.parse(fieldValue.toString(), FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
