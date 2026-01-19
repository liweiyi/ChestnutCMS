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
