package com.chestnut.xmodel.core.impl;

import com.chestnut.common.utils.ObjectUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.xmodel.core.IMetaFieldValidation;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * 正则表达式校验
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMetaFieldValidation.BEAN_PREFIX + MetaFieldValidation_Regex.TYPE)
public class MetaFieldValidation_Regex implements IMetaFieldValidation {

    static final String TYPE = "Regex";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean validate(Object fieldValue, Map<String, Object> args) {
        String regex = MapUtils.getString(args, "regex");
        if (ObjectUtils.isAnyNull(fieldValue) || StringUtils.isEmpty(regex)) {
            return true;
        }
        return Pattern.matches(regex, fieldValue.toString());
    }
}
