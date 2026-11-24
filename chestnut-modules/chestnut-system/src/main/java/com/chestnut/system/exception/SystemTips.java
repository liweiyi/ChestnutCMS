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
package com.chestnut.system.exception;

import com.chestnut.common.exception.TipMessage;

/**
 * 系统接口与任务操作提示
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public enum SystemTips implements TipMessage {

    /**
     * 请选择需要导入的文件
     */
    IMPORT_FILE_REQUIRED,

    /**
     * 修改密码失败，旧密码错误
     */
    OLD_PASSWORD_INVALID,

    /**
     * 新密码不能与旧密码相同
     */
    PASSWORD_UNCHANGED,

    /**
     * 上传图片异常，请联系管理员
     */
    IMAGE_UPLOAD_FAILED,

    /**
     * 验证码未启用
     */
    CAPTCHA_DISABLED,

    /**
     * 任务不能中断：{0}
     */
    TASK_NOT_INTERRUPTIBLE,

    /**
     * 下载路径必须指向公开文件
     */
    DOWNLOAD_PATH_DENIED,

    /**
     * 脚本内容不能为空
     */
    SCRIPT_REQUIRED,

    /**
     * 定时任务已中断：{0}
     */
    TASK_INTERRUPTED,

    /**
     * 登录成功
     */
    LOGIN_SUCCEEDED,

    /**
     * 登录成功，正在跳转…
     */
    LOGIN_REDIRECTING,

    /**
     * 第三方登录失败（{0}，错误码：{1}）
     */
    THIRD_PARTY_LOGIN_FAILED,

    /**
     * 欢迎使用{0}，当前版本：v{1}，请通过前端地址访问。
     */
    WELCOME;

    @Override
    public String value() {
        return "TIP.SYS.OPERATION." + name();
    }
}
