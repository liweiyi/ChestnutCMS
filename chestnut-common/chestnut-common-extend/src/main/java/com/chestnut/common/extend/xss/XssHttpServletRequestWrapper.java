/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
package com.chestnut.common.extend.xss;

import com.chestnut.common.extend.enums.XssMode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.Map;
import java.util.Objects;

/**
 * XssHttpServletRequestWrapper
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private final XssMode xssMode;

    public XssHttpServletRequestWrapper(HttpServletRequest request, XssMode xssMode) {
        super(request);
        this.xssMode = xssMode;
    }

    @Override
    public String getParameter(String name) {
        String parameter = super.getParameter(name);
        return XssContextHolder.xssProcess(parameter, xssMode);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (Objects.nonNull(values)) {
            for (int i = 0; i < values.length; i++) {
                values[i] = XssContextHolder.xssProcess(values[i], xssMode);
            }
        }
        return values;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> parameterMap = super.getParameterMap();
        if (Objects.nonNull(parameterMap)) {
            parameterMap.keySet().forEach(key -> {
                String[] values = parameterMap.get(key);
                for (int i = 0; i < values.length; i++) {
                    values[i] = XssContextHolder.xssProcess(values[i], xssMode);
                }
            });
        }
        return parameterMap;
    }

    @Override
    public String getHeader(String name) {
        String header = super.getHeader(name);
        return XssContextHolder.xssProcess(header, xssMode);
    }
}
