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
package com.chestnut.stat.core;

import com.chestnut.common.utils.IP2RegionUtils;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.common.utils.StringUtils;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * RequestData
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class RequestEventData {

    /**
     * 请求域
     */
    private String host;

    /**
     * 请求地址
     */
    private String uri;

    /**
     * IP地址
     */
    private String ip;

    /**
     * IP所属地区
     */
    private String address;

    /**
     * 来源地址
     */
    private String referer;

    /**
     * 浏览器类型
     */
    private String browser;

    /**
     * UserAgent
     */
    private String userAgent;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 语言
     */
    private String locale;

    /**
     * 事件发生时间
     */
    private LocalDateTime evtTime;

    /**
     * 提取request请求信息到RequestEvent
     *
     * @param request
     */
    public void fill(HttpServletRequest request) {
        this.setHost(request.getRemoteHost());
        this.setUri(request.getRequestURI());
        this.setIp(ServletUtils.getIpAddr(request));
        this.setAddress(IP2RegionUtils.ip2Region(this.getIp()));
        this.setReferer(ServletUtils.getReferer(request));
        this.setLocale(StringUtils.substringBefore(ServletUtils.getAcceptLanaguage(request), ","));

        this.setUserAgent(ServletUtils.getUserAgent(request));
        UserAgent ua = ServletUtils.parseUserAgent(request);
        this.setBrowser(ua.getBrowser().getName());
        this.setOs(ua.getOperatingSystem().getName());
        this.setDeviceType(ua.getOperatingSystem().getDeviceType().getName());
    }
}
