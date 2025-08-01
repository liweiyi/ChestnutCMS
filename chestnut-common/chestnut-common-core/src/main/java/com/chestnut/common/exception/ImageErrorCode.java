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
