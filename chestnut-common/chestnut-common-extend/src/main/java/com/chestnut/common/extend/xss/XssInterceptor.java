package com.chestnut.common.extend.xss;

import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

import com.chestnut.common.extend.annotation.XssIgnore;

public class XssInterceptor implements AsyncHandlerInterceptor {
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if (handler instanceof HandlerMethod handlerMethod) {
			XssIgnore xssIgnore = handlerMethod.getMethodAnnotation(XssIgnore.class);
			if (Objects.nonNull(xssIgnore)) {
				XssContextHolder.ignore();
			}
		}
		return true;
	}
	
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		XssContextHolder.remove();
	}
	
	/**
	 * 如果返回一个current类型的变量，会启用一个新的线程。执行完preHandle方法之后立即会调用afterConcurrentHandlingStarted
	 * 然后新线程再以次执行preHandle,postHandle,afterCompletion**
	 */
	@Override
	public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		XssContextHolder.remove();
	}
}
