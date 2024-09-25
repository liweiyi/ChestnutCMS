package com.chestnut.common.extend.xss;

import com.chestnut.common.extend.enums.XssMode;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

/**
 * XSSFilter
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class XssFilter implements Filter {

    private final XssMode xssMode;

    public XssFilter(XssMode xssMode) {
        this.xssMode = xssMode;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        XssHttpServletRequestWrapper wrapper = new XssHttpServletRequestWrapper(request, xssMode);
        filterChain.doFilter(wrapper, servletResponse);
    }
}
