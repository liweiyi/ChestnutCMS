/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chestnut.common.utils;

import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.validation.RegexConsts;
import com.google.common.net.InetAddresses;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * ValidationUtils
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class ValidationUtils {

    // 地理区号与前端选项一致，来源 Google libphonenumber（Apache-2.0）：
    // https://github.com/google/libphonenumber/blob/master/java/libphonenumber/src/com/google/i18n/phonenumbers/CountryCodeToRegionCodeMap.java
    private static final List<String> CALLING_CODES = Arrays.stream(
                    "+86 +1 +7 +20 +27 +30 +31 +32 +33 +34 +36 +39 +40 +41 +43 +44 +45 +46 +47 +48 +49 +51 +52 +53 +54 +55 +56 +57 +58 +60 +61 +62 +63 +64 +65 +66 +81 +82 +84 +90 +91 +92 +93 +94 +95 +98 +211 +212 +213 +216 +218 +220 +221 +222 +223 +224 +225 +226 +227 +228 +229 +230 +231 +232 +233 +234 +235 +236 +237 +238 +239 +240 +241 +242 +243 +244 +245 +246 +247 +248 +249 +250 +251 +252 +253 +254 +255 +256 +257 +258 +260 +261 +262 +263 +264 +265 +266 +267 +268 +269 +290 +291 +297 +298 +299 +350 +351 +352 +353 +354 +355 +356 +357 +358 +359 +370 +371 +372 +373 +374 +375 +376 +377 +378 +380 +381 +382 +383 +385 +386 +387 +389 +420 +421 +423 +500 +501 +502 +503 +504 +505 +506 +507 +508 +509 +590 +591 +592 +593 +594 +595 +596 +597 +598 +599 +670 +672 +673 +674 +675 +676 +677 +678 +679 +680 +681 +682 +683 +685 +686 +687 +688 +689 +690 +691 +692 +850 +852 +853 +855 +856 +880 +886 +960 +961 +962 +963 +964 +965 +966 +967 +968 +970 +971 +972 +973 +974 +975 +976 +977 +992 +993 +994 +995 +996 +998".split(" "))
            .sorted(Comparator.comparingInt(String::length).reversed()).toList();

    public static final Pattern PatternChineseMainlandPhoneNumber = Pattern.compile(RegexConsts.REGEX_PHONE);

    public static final Pattern PatternPhoneNumber = Pattern.compile("^\\+[1-9][0-9]{1,14}$");

    private static final Pattern PatternEmail = Pattern.compile(
            "^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*@"
                    + "[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?"
                    + "(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+$");

    private static final Pattern PatternIpAddress = Pattern.compile("^[0-9A-Fa-f:.]+$");

    private static final Pattern PatternInvalidFilenameCharacter = Pattern.compile("[\\\\/:*?\"<>|\\p{javaISOControl}]");

    private static final Pattern PatternReservedFilename = Pattern.compile(
            "^(?:CON|PRN|AUX|NUL|COM[1-9¹²³]|LPT[1-9¹²³])$", Pattern.CASE_INSENSITIVE);

    private static final Pattern CODE_PATTERN = Pattern.compile("[0-9]{6}");

    public static final Pattern PatternUsername= Pattern.compile("^[A-Za-z][A-Za-z0-9_]+$");

    public record PhoneNumber(String phoneCallingCode, String phoneNumber) {

        /**
         * 创建手机号实例，区号为空时默认+86
         *
         * @param phoneCallingCode 区号
         * @param phoneNumber 本地号码
         */
        public static PhoneNumber create(String phoneCallingCode, String phoneNumber) {
            if (StringUtils.isEmpty(phoneCallingCode)) {
                return new PhoneNumber("+86", StringUtils.trim(phoneNumber));
            }
            return new PhoneNumber(StringUtils.trim(phoneCallingCode), StringUtils.trim(phoneNumber));
        }

        public String getFullPhoneNumber() {
            if (StringUtils.isAnyBlank(phoneCallingCode, phoneNumber)) {
                return null;
            }
            return phoneCallingCode + phoneNumber;
        }

        public boolean isValid() {
            if (StringUtils.isAnyBlank(phoneCallingCode, phoneNumber)) {
                return false;
            }
            if (!CALLING_CODES.contains(phoneCallingCode)) {
                return false;
            }
            String fullPhoneNumber = this.getFullPhoneNumber();
            return Objects.nonNull(fullPhoneNumber) && PatternPhoneNumber.matcher(fullPhoneNumber).matches();
        }

        public void validate() {
            if (!isValid()) {
                throw CommonErrorCode.INVALID_PHONE_NUMBER.exception();
            }
        }
    }

    /**
     * 校验中国大陆手机号码
     *
     * @param phoneNumber 手机号码
     */
    public static <X extends Throwable> void validateChineseMainlandPhoneNumber(String phoneNumber, Supplier<X> errorSupplier) throws X {
        boolean matches = phoneNumber != null && PatternChineseMainlandPhoneNumber.matcher(phoneNumber).matches();
        if (!matches) {
            throw errorSupplier.get();
        }
    }

    public static void validateChineseMainlandPhoneNumber(String phoneNumber) {
        validateChineseMainlandPhoneNumber(phoneNumber, CommonErrorCode.INVALID_PHONE_NUMBER::exception);
    }

    /**
     * 校验带国际地区码的手机号格式：以“+”开头，后接最多15位数字，首位不能为0
     *
     * @param phoneNumber 手机号，例如：+8618888888888
     */
    public static <X extends Throwable> void validatePhoneNumber(String phoneNumber, Supplier<X> errorSupplier) throws X {
        boolean matches = phoneNumber != null && PatternPhoneNumber.matcher(phoneNumber).matches();
        if (!matches) {
            throw errorSupplier.get();
        }
    }

    public static void validatePhoneNumber(String phoneNumber) {
        validatePhoneNumber(phoneNumber, CommonErrorCode.INVALID_PHONE_NUMBER::exception);
    }

    public static <X extends Throwable> void validatePhoneCallingCode(String phoneCallingCode, Supplier<X> errorSupplier) throws X {
        boolean matches = phoneCallingCode != null && CALLING_CODES.contains(phoneCallingCode);
        if (!matches) {
            throw errorSupplier.get();
        }
    }

    public static void validatePhoneCallingCode(String phoneCallingCode) {
        validatePhoneCallingCode(phoneCallingCode, CommonErrorCode.INVALID_PHONE_NUMBER::exception);
    }

    /**
     * 校验Email有效格式，支持常见的非引号本地部分和ASCII域名
     *
     * @param email Email地址
     */
    public static <X extends Throwable> void validateEmail(String email, Supplier<? extends X> errorSupplier) throws X {
        boolean matches = email != null && email.length() <= 254
                && email.indexOf('@') > 0 && email.indexOf('@') <= 64
                && PatternEmail.matcher(email).matches();
        if (!matches) {
            throw errorSupplier.get();
        }
    }

    public static void validateEmail(String email) {
        validateEmail(email, CommonErrorCode.INVALID_EMAIL::exception);
    }

    /**
     * 校验IPv4/IPv6地址字面量，不含端口、网段和作用域标识
     *
     * @param ipAddress IP地址
     */
    public static <X extends Throwable> void validateIpAddress(String ipAddress,  Supplier<X> errorSupplier) throws X {
        // 只解析地址字面量，避免将主机名通过DNS解析后误判为合法IP。
        boolean matches = ipAddress != null && PatternIpAddress.matcher(ipAddress).matches()
                && InetAddresses.isInetAddress(ipAddress);
        if (!matches) {
            throw errorSupplier.get();
        }
    }

    public static void validateIpAddress(String ipAddress) {
        validateIpAddress(ipAddress, CommonErrorCode.INVALID_IP_ADDRESS::exception);
    }

    /**
     * 校验单个文件名称是否合法，拒绝路径、控制字符和Windows保留名称
     *
     * @param filename 文件名称
     */
    public static <X extends Throwable> void validateFilename(String filename, Supplier<X> errorSupplier) throws X {
        boolean matches = StringUtils.isNotBlank(filename)
                && !".".equals(filename) && !"..".equals(filename)
                && !filename.endsWith(".") && !filename.endsWith(" ")
                && !PatternInvalidFilenameCharacter.matcher(filename).find();
        if (!matches) {
            throw errorSupplier.get();
        }

        int dotIndex = filename.indexOf('.');
        String basename = dotIndex < 0 ? filename : filename.substring(0, dotIndex);
        matches = PatternReservedFilename.matcher(basename.stripTrailing()).matches();
        if (matches) {
            throw errorSupplier.get();
        }
    }

    public static void validateFilename(String filename) {
        validateFilename(filename, CommonErrorCode.INVALID_FILENAME::exception);
    }

    public static <X extends Throwable> void notBlank(String value, Supplier<? extends X> exceptionSupplier) throws X {
        if (StringUtils.isBlank(value)) {
            throw exceptionSupplier.get();
        }
    }

    public static void notBlank(String value) {
        notBlank(value, CommonErrorCode.NOT_EMPTY::exception);
    }

    public static <X extends Throwable> void notEmpty(String value, Supplier<? extends X> exceptionSupplier) throws X {
        if (StringUtils.isEmpty(value)) {
            throw exceptionSupplier.get();
        }
    }

    public static void notEmpty(String value) {
        notEmpty(value, CommonErrorCode.NOT_EMPTY::exception);
    }

    public static <X extends Throwable> void notNull(Object value, Supplier<? extends X> exceptionSupplier) throws X {
        if (value == null) {
            throw exceptionSupplier.get();
        }
    }

    public static  void notNull(Object value) {
        notNull(value, CommonErrorCode.NOT_NULL::exception);
    }

    /**
     * 根据正则表达式完整匹配指定字符串，空字符串是否通过由正则表达式决定
     *
     * @param value  待校验字符串
     * @param regexp 正则表达式
     */
    public static <X extends Throwable> void validatePattern(String value, String regexp, Supplier<? extends X> exceptionSupplier) throws X {
        Assert.notNull(value, () -> CommonErrorCode.NOT_NULL.exception("value"));
        Assert.notNull(regexp, () -> CommonErrorCode.NOT_NULL.exception("regexp"));
        if (ObjectUtils.isAnyNull(value, regexp)) {
            throw exceptionSupplier.get();
        }

        Pattern pattern;
        try {
            pattern = Pattern.compile(regexp);
        } catch (PatternSyntaxException e) {
            throw new RuntimeException(e);
        }
        if (!pattern.matcher(value).matches()) {
            throw exceptionSupplier.get();
        }
    }

    /**
     * 6位数字短信验证码校验
     */
    public static <X extends Throwable> void validateSmsCode(String code, Supplier<? extends X> exceptionSupplier) throws X {
        boolean matches = Objects.nonNull(code) && CODE_PATTERN.matcher(code).matches();
        if (!matches) {
            throw exceptionSupplier.get();
        }
    }

    /**
     * 校验指定长度用户名格式
     *
     * @param username 用户名
     * @param minLen 最小长度
     * @param maxLen 最大长度
     */
    public static <X extends Throwable> void validateUsername(String username, int minLen, int maxLen, Supplier<? extends X> exceptionSupplier) throws X {
        boolean matches = username != null && username.length() >= minLen
                && username.length() <= maxLen
                && PatternUsername.matcher(username).matches();
        if (!matches) {
            throw exceptionSupplier.get();
        }
    }

    public static void validateUsername(String username, int minLen, int maxLen) {
        validateUsername(username, minLen, maxLen, CommonErrorCode.INVALID_USERNAME::exception);
    }
}
