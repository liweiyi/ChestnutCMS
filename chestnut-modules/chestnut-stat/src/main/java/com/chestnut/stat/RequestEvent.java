package com.chestnut.stat;

import java.time.LocalDateTime;

import com.chestnut.common.utils.IP2RegionUtils;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.common.utils.StringUtils;

import eu.bitwalker.useragentutils.UserAgent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestEvent {
	
	/**
	 * 请求域
	 */
	private String host;
	
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
