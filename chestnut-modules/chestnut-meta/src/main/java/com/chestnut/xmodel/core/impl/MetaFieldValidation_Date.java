package com.chestnut.xmodel.core.impl;

import com.chestnut.xmodel.core.IMetaFieldValidation;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 * 日期格式：yyyy-MM-dd
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMetaFieldValidation.BEAN_PREFIX + MetaFieldValidation_Date.TYPE)
public class MetaFieldValidation_Date implements IMetaFieldValidation {

    static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    static final String TYPE = "Date";

    @Override
    public String getType() {
        return TYPE;
    }

    public boolean validate(Object fieldValue, Map<String, Object> args) {
        if (fieldValue == null) {
            return true;
        }
        try {
            LocalDate parse = LocalDate.parse(fieldValue.toString(), FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
