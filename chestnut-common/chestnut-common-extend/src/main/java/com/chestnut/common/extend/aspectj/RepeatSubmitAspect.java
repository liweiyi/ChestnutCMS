package com.chestnut.common.extend.aspectj;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.chestnut.common.utils.ServletUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;

import com.chestnut.common.extend.ExtendConstants;
import com.chestnut.common.extend.annotation.RepeatSubmit;
import com.chestnut.common.extend.exception.RepeatSubmitErrorCode;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.security.IUserType;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.stp.StpLogic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 防重复提交，仅对已登录用户生效
 */
@Slf4j
@Aspect
@RequiredArgsConstructor
public class RepeatSubmitAspect {

	private final RedisCache redisCache;

	private final List<IUserType> userTypes;

	@Before("@annotation(repeatSubmit)")
	public void doBefore(JoinPoint point, RepeatSubmit repeatSubmit) throws Throwable {
		// PREFIX + CLASS.METHOD + TOKEN
		MethodSignature signature = (MethodSignature) point.getSignature();
		Method method = signature.getMethod();
		Class<?> targetClass = method.getDeclaringClass();
		StringBuffer sb = new StringBuffer();
		sb.append(ExtendConstants.REPEAT_SUBMIT_KEY).append(targetClass.getName()).append(".").append(method.getName())
				.append(".");
		// 获取用户唯一标识作为缓存key组成部分，默认取前端参数uuid，无参数时尝试获取用户登录token。
		String uuid = ServletUtils.getParameter("uuid");
		if (StringUtils.isEmpty(uuid)) {
			for (IUserType ut : userTypes) {
				StpLogic stpLogic = SaManager.getStpLogic(ut.getType());
				if (stpLogic.isLogin()) {
					uuid = stpLogic.getTokenValue();
					break;
				}
			}
		}
		if (StringUtils.isEmpty(uuid)) {
			log.warn("Method '{0}' annotation by @RepeatSubmit, but no parameter `uuid` or no login user.", sb.toString());
			return;
		}
		sb.append(uuid);
		String cacheKey = sb.toString();
		if (this.redisCache.hasKey(cacheKey)) {
			throw RepeatSubmitErrorCode.REPEAT_ERR.exception();
		}
		this.redisCache.setCacheObject(cacheKey, 1, repeatSubmit.interval(), TimeUnit.MILLISECONDS);
	}
}
