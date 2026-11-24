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
package com.chestnut.media;

import com.chestnut.common.exception.TipMessage;

/**
 * 模块接口与任务进度提示。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public enum CmsMediaTips implements TipMessage {

    /** 正在删除音频内容数据：{0}/{1} */
    DELETING_AUDIO_DATA,
    /** 正在删除音频内容备份数据：{0}/{1} */
    DELETING_AUDIO_BACKUP_DATA,
    /** 正在删除视频内容数据：{0}/{1} */
    DELETING_VIDEO_DATA,
    /** 正在删除视频内容备份数据：{0}/{1} */
    DELETING_VIDEO_BACKUP_DATA,
    /** 删除音频内容数据失败，请查看系统日志 */
    DELETE_AUDIO_FAILED,
    /** 删除音频内容备份数据失败，请查看系统日志 */
    DELETE_AUDIO_BACKUP_FAILED,
    /** 删除视频内容数据失败，请查看系统日志 */
    DELETE_VIDEO_FAILED,
    /** 删除视频内容备份数据失败，请查看系统日志 */
    DELETE_VIDEO_BACKUP_FAILED,
    /** 正在统计音频资源引用：{0}/{1} */
    STAT_AUDIO_REFERENCES,
    /** 正在统计视频资源引用：{0}/{1} */
    STAT_VIDEO_REFERENCES,

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
