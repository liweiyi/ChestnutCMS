package com.chestnut.media;

import com.chestnut.common.exception.TipMessage;

/**
 * CmsMediaTips
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public enum CmsMediaTips implements TipMessage {

    /**
     * 正在导出音频内容数据
     */
    EXPORTING_AUDIO,

    /**
     * 正在导入音频内容数据
     */
    IMPORTING_AUDIO,

    /**
     * 导入音频内容数据失败
     */
    IMPORT_AUDIO_FAIL,

    /**
     * 正在导出视频内容数据
     */
    EXPORTING_VIDEO,

    /**
     * 正在导入视频内容数据
     */
    IMPORTING_VIDEO,

    /**
     * 导入视频内容数据失败
     */
    IMPORT_VIDEO_FAIL,
    ;

    @Override
    public String value() {
        return "TIP.CMS.MEDIA." + this.name();
    }
}
