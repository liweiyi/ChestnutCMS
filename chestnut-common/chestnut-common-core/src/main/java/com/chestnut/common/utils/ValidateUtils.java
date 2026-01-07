package com.chestnut.common.utils;

import com.chestnut.common.validation.RegexConsts;

import java.util.regex.Pattern;

/**
 * ValidateUtils
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class ValidateUtils {

    public static final Pattern PatternChineseMainlandPhoneNumber = Pattern.compile(RegexConsts.REGEX_PHONE);

    /**
     * 校验中国大陆手机号码
     *
     * @param phoneNumber 手机号码
     */
    public static boolean validateChineseMainlandPhoneNumber(String phoneNumber) {
        return PatternChineseMainlandPhoneNumber.matcher(phoneNumber).matches();
    }
}
