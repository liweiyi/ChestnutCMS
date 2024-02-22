/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
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
package com.chestnut.common.i18n;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * <TODO description class purpose>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class I18nPlaceholderHelper {
    private static final Log logger = LogFactory.getLog(PropertyPlaceholderHelper.class);
    private static final Map<String, String> wellKnownSimplePrefixes = new HashMap<>(4);
    private final String placeholderPrefix;
    private final String placeholderSuffix;
    private final String simplePrefix;
    @Nullable
    private final String valueSeparator;
    private final boolean ignoreUnresolvablePlaceholders;

    public I18nPlaceholderHelper(String placeholderPrefix, String placeholderSuffix) {
        this(placeholderPrefix, placeholderSuffix, null, true);
    }

    public I18nPlaceholderHelper(String placeholderPrefix,
                                 String placeholderSuffix,
                                 @Nullable String valueSeparator,
                                 boolean ignoreUnresolvablePlaceholders) {
        Assert.notNull(placeholderPrefix, "'placeholderPrefix' must not be null");
        Assert.notNull(placeholderSuffix, "'placeholderSuffix' must not be null");
        this.placeholderPrefix = placeholderPrefix;
        this.placeholderSuffix = placeholderSuffix;
        String simplePrefixForSuffix = (String)wellKnownSimplePrefixes.get(this.placeholderSuffix);
        if (simplePrefixForSuffix != null && this.placeholderPrefix.endsWith(simplePrefixForSuffix)) {
            this.simplePrefix = simplePrefixForSuffix;
        } else {
            this.simplePrefix = this.placeholderPrefix;
        }

        this.valueSeparator = valueSeparator;
        this.ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
    }

    public String replacePlaceholders(String value, final Properties properties) {
        Assert.notNull(properties, "'properties' must not be null");
        Objects.requireNonNull(properties);
        return this.replacePlaceholders(value, properties::getProperty);
    }

    public String replacePlaceholders(String value, PlaceholderResolver placeholderResolver) {
        Assert.notNull(value, "'value' must not be null");
        return this.parseStringValue(value, placeholderResolver, null);
    }

    protected String parseStringValue(String value, PlaceholderResolver placeholderResolver, @Nullable Set<String> visitedPlaceholders) {
        int startIndex = value.indexOf(this.placeholderPrefix);
        if (startIndex == -1) {
            return value;
        } else {
            StringBuilder result = new StringBuilder(value);

            while(startIndex != -1) {
                int endIndex = this.findPlaceholderEndIndex(result, startIndex);
                if (endIndex != -1) {
                    String placeholder = result.substring(startIndex + this.placeholderPrefix.length(), endIndex);
                    String originalPlaceholder = placeholder;
                    if (visitedPlaceholders == null) {
                        visitedPlaceholders = new HashSet<>(4);
                    }

                    if (!visitedPlaceholders.add(placeholder)) {
                        throw new IllegalArgumentException("Circular placeholder reference '" + placeholder + "' in property definitions");
                    }

                    placeholder = this.parseStringValue(placeholder, placeholderResolver, visitedPlaceholders);
                    String propVal = placeholderResolver.resolvePlaceholder(placeholder);
                    if (propVal == null && this.valueSeparator != null) {
                        int separatorIndex = placeholder.indexOf(this.valueSeparator);
                        if (separatorIndex != -1) {
                            String actualPlaceholder = placeholder.substring(0, separatorIndex);
                            String defaultValue = placeholder.substring(separatorIndex + this.valueSeparator.length());
                            propVal = placeholderResolver.resolvePlaceholder(actualPlaceholder);
                            if (propVal == null) {
                                propVal = defaultValue;
                            }
                        }
                    }

                    if (StringUtils.hasLength(propVal)) {
                        propVal = this.parseStringValue(propVal, placeholderResolver, visitedPlaceholders);
                        result.replace(startIndex, endIndex + this.placeholderSuffix.length(), propVal);
                        if (logger.isTraceEnabled()) {
                            logger.trace("Resolved placeholder '" + placeholder + "'");
                        }

                        startIndex = result.indexOf(this.placeholderPrefix, startIndex + propVal.length());
                    } else {
                        if (!this.ignoreUnresolvablePlaceholders) {
                            throw new IllegalArgumentException("Could not resolve placeholder '" + placeholder + "' in value \"" + value + "\"");
                        }
                        startIndex = result.indexOf(this.placeholderPrefix, endIndex + this.placeholderSuffix.length());
                    }

                    visitedPlaceholders.remove(originalPlaceholder);
                } else {
                    startIndex = -1;
                }
            }

            return result.toString();
        }
    }

    private int findPlaceholderEndIndex(CharSequence buf, int startIndex) {
        int index = startIndex + this.placeholderPrefix.length();
        int withinNestedPlaceholder = 0;

        while(index < buf.length()) {
            if (StringUtils.substringMatch(buf, index, this.placeholderSuffix)) {
                if (withinNestedPlaceholder <= 0) {
                    return index;
                }

                --withinNestedPlaceholder;
                index += this.placeholderSuffix.length();
            } else if (StringUtils.substringMatch(buf, index, this.simplePrefix)) {
                ++withinNestedPlaceholder;
                index += this.simplePrefix.length();
            } else {
                ++index;
            }
        }

        return -1;
    }

    static {
        wellKnownSimplePrefixes.put("}", "{");
        wellKnownSimplePrefixes.put("]", "[");
        wellKnownSimplePrefixes.put(")", "(");
    }

    @FunctionalInterface
    public interface PlaceholderResolver {
        @Nullable
        String resolvePlaceholder(String placeholderName);
    }
}
