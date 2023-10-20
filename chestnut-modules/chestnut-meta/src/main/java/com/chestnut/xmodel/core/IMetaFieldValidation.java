package com.chestnut.xmodel.core;

import com.chestnut.common.i18n.I18nUtils;

import java.util.Map;

/**
 * 元数据字段校验规则
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IMetaFieldValidation {

    String BEAN_PREFIX = "MetaFieldValidation_";

    String getType();

    default boolean validate(Object fieldValue, Map<String, Object> args) {
        return true;
    }

    default String getErrorMessage(Object... args) {
        return I18nUtils.get("{META.Validation." + getType() + "}", args);
    }
}
