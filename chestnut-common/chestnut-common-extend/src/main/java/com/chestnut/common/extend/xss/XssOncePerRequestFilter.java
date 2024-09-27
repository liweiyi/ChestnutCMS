package com.chestnut.common.extend.xss;

import com.chestnut.common.extend.enums.XssMode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * XSSFilter
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class XssOncePerRequestFilter extends OncePerRequestFilter {

    private final XssMode xssMode;

    public XssOncePerRequestFilter(XssMode xssMode) {
        this.xssMode = xssMode;
    }

    @Override
    protected void doFilterInternal( HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        XssHttpServletRequestWrapper wrapper = new XssHttpServletRequestWrapper(request, xssMode);
        filterChain.doFilter(wrapper, response);
    }
}
