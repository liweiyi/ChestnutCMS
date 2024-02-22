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
package com.chestnut.common.security.aspectj;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.annotation.*;
import cn.dev33.satoken.basic.SaBasicUtil;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.strategy.SaStrategy;
import com.chestnut.common.security.IUserType;
import com.chestnut.common.security.SecurityService;
import com.chestnut.common.security.SecurityUtils;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.exception.SecurityErrorCode;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.ServletUtils;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.annotation.Order;
import org.springframework.expression.Expression;
import org.springframework.stereotype.Component;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

/**
 * SA-TOKEN认证切面
 *
 * <p>所有BEAN均支持使用注解鉴权，且使用@Priv注解的权限字符串支持SpEL表达式。</p>
 *
 * 例如校验可编辑的菜单数据：@Priv(value = "sys:menu:edit:${#menu.menuIdId}")
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Aspect
@Component
@Order(-100)
@RequiredArgsConstructor
public class SaCheckAspect {

    public static final String POINTCUT_SIGN = "@within(cn.dev33.satoken.annotation.SaCheckLogin)" +
            " || @annotation(cn.dev33.satoken.annotation.SaCheckLogin)" +
            " || @within(cn.dev33.satoken.annotation.SaCheckRole)" +
            " || @annotation(cn.dev33.satoken.annotation.SaCheckRole)" +
            " || @within(cn.dev33.satoken.annotation.SaCheckPermission)" +
            " || @annotation(cn.dev33.satoken.annotation.SaCheckPermission)" +
            " || @within(cn.dev33.satoken.annotation.SaCheckSafe)" +
            " || @annotation(cn.dev33.satoken.annotation.SaCheckSafe)" +
            " || @within(cn.dev33.satoken.annotation.SaCheckDisable)" +
            " || @annotation(cn.dev33.satoken.annotation.SaCheckDisable)" +
            " || @within(cn.dev33.satoken.annotation.SaCheckBasic)" +
            " || @annotation(cn.dev33.satoken.annotation.SaCheckBasic)" +
            " || @within(com.chestnut.common.security.anno.Priv)" +
            " || @annotation(com.chestnut.common.security.anno.Priv)";

    private final SecurityService securityService;

    private final AuthEvaluator authEvaluator;

    @Pointcut(POINTCUT_SIGN)
    public void pointcut() {
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        if (!(Boolean) SaStrategy.instance.isAnnotationPresent.apply(method, SaIgnore.class)) {
            // 先校验 Method 所属 Class 上的注解
            this.checkPermission(method.getDeclaringClass(), joinPoint, method);
            // 再校验 Method 上的注解
            this.checkPermission(method, joinPoint, method);
        }
        return joinPoint.proceed();
    }

    private void checkPermission(AnnotatedElement target, ProceedingJoinPoint joinPoint, Method method) {
        // 校验 @Priv 注解
        Priv priv = (Priv) SaStrategy.instance.getAnnotation.apply(target, Priv.class);
        if (priv != null) {
            IUserType ut = securityService.getUserType(priv.type());
            Assert.notNull(ut, () -> SecurityErrorCode.UNKNOWN_USER_TYPE.exception(priv.type()));

            StpLogic stpLogic = SaManager.getStpLogic(priv.type(), false);
            stpLogic.checkLogin();
            if (!SecurityUtils.isSuperAdmin(stpLogic.getLoginIdAsLong()) && priv.value().length > 0) {
                String[] perms = this.parsePerms(priv.value(), joinPoint.getTarget(), method, joinPoint.getArgs());
                if (priv.mode() == SaMode.AND) {
                    stpLogic.checkPermissionAnd(perms);
                } else {
                    stpLogic.checkPermissionOr(perms);
                }
            }
        } else {
            // 校验 @SaCheckLogin 注解
            SaCheckLogin checkLogin = (SaCheckLogin) SaStrategy.instance.getAnnotation.apply(target, SaCheckLogin.class);
            if (checkLogin != null) {
                SaManager.getStpLogic(checkLogin.type(), false).checkByAnnotation(checkLogin);
            }

            // 校验 @SaCheckRole 注解
            SaCheckRole checkRole = (SaCheckRole) SaStrategy.instance.getAnnotation.apply(target, SaCheckRole.class);
            if (checkRole != null) {
                SaManager.getStpLogic(checkRole.type(), false).checkByAnnotation(checkRole);
            }

            // 校验 @SaCheckPermission 注解
            SaCheckPermission checkPermission = (SaCheckPermission) SaStrategy.instance.getAnnotation.apply(target, SaCheckPermission.class);
            if (checkPermission != null) {
                SaManager.getStpLogic(checkPermission.type(), false).checkByAnnotation(checkPermission);
            }
        }
        // 校验 @SaCheckSafe 注解
        SaCheckSafe checkSafe = (SaCheckSafe) SaStrategy.instance.getAnnotation.apply(target, SaCheckSafe.class);
        if (checkSafe != null) {
            SaManager.getStpLogic(checkSafe.type(), false).checkByAnnotation(checkSafe);
        }

        // 校验 @SaCheckDisable 注解
        SaCheckDisable checkDisable = (SaCheckDisable) SaStrategy.instance.getAnnotation.apply(target, SaCheckDisable.class);
        if (checkDisable != null) {
            SaManager.getStpLogic(checkDisable.type(), false).checkByAnnotation(checkDisable);
        }

        // 校验 @SaCheckBasic 注解
        SaCheckBasic checkBasic = (SaCheckBasic) SaStrategy.instance.getAnnotation.apply(target, SaCheckBasic.class);
        if (checkBasic != null) {
            SaBasicUtil.check(checkBasic.realm(), checkBasic.account());
        }
    }

    private String[] parsePerms(String[] perms, Object rootObj, Method method, Object... args) {
        MethodBasedEvaluationContext evaluationContext = new MethodBasedEvaluationContext(rootObj,
                method, args, AuthEvaluator.PARAMETER_NAME_DISCOVERER);
        evaluationContext.setVariable("_header", ServletUtils.getHeaderCaseInsensitiveMap(ServletUtils.getRequest()));
        evaluationContext.setVariable("_cookie", ServletUtils.getCookieValues(ServletUtils.getRequest()));
        for (int i = 0; i < perms.length; i++) {
            Expression expression = this.authEvaluator.parseExpression(perms[i]);
            if (expression != null) {
                //使用Spring EL表达式解析后的权限定义替换原来的权限定义
                perms[i] = expression.getValue(evaluationContext, String.class);
            }
        }
        return perms;
    }
}