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

/**
 * ImageErrorCode
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public enum ImageErrorCode implements ErrorCode {

    /**
     * 输入源图不能为空
     */
    SOURCE_INPUT_NOT_NULL,

    /**
     * 输入源图文件不存在
     */
    SOURCE_FILE_NOT_EXISTS,

    /**
     * 图片处理失败
     */
    IMAGE_PROCESS_FAIL,

    /**
     * 不支持的图片操作：{0}
     */
    UNSUPPORTED_OP,

    /**
     * 不支持的图片格式：{0}
     */
    UNSUPPORTED_IMAGE_FORMAT;

    @Override
    public String value() {
        return "{ERR.IMG." + this.name() + "}";
    }

    @Override
    public GlobalException exception(Throwable cause, Object... args) {
        return ErrorCode.super.exception(cause, args);
    }
}
