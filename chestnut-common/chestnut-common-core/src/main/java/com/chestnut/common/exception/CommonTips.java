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
package com.chestnut.common.exception;

import com.chestnut.common.async.AsyncTaskManager;

import java.util.Locale;

/**
 * 接口响应和异步任务使用的通用提示。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public enum CommonTips implements TipMessage {

    /** 执行成功 */
    OPERATION_SUCCEEDED("CC.COMMON.R.SUCCESS"),
    /** 执行失败 */
    OPERATION_FAILED("CC.COMMON.R.FAIL"),
    /** 任务执行成功 */
    TASK_SUCCEEDED("AsyncTask.SuccessMsg"),
    /** 不支持请求方法：{0} */
    METHOD_NOT_SUPPORTED("TIP.COMMON.REQUEST.METHOD_NOT_SUPPORTED"),
    /** 请求参数格式错误 */
    INVALID_REQUEST("TIP.COMMON.REQUEST.INVALID"),
    /** 请求参数“{0}”无效 */
    INVALID_PARAMETER("TIP.COMMON.REQUEST.INVALID_PARAMETER");

    private final String key;

    CommonTips(String key) {
        this.key = key;
    }

    @Override
    public String value() {
        return key;
    }

    public static String failureReason(Throwable error) {
        return failureReason(error, AsyncTaskManager.getLocale());
    }

    public static String failureReason(Throwable error, Locale locale) {
        if (error instanceof GlobalException globalException) {
            return globalException.userMessage(locale);
        }
        return OPERATION_FAILED.locale(locale);
    }
}
