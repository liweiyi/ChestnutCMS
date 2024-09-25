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
