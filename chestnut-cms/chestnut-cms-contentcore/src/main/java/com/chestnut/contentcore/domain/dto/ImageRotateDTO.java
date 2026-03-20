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
package com.chestnut.contentcore.domain.dto;

import com.chestnut.common.annotation.XComment;
import lombok.Getter;
import lombok.Setter;

@XComment("{API.DOC.CMS.IMAGE_PROCESS.ROTATE_DTO}")
@Getter
@Setter
public class ImageRotateDTO {

    @XComment("{API.DOC.CMS.RESOURCE_MGMT.RESOURCE_ID}")
    private Long resourceId;

    /**
     * 缩略图宽度
     */
    @XComment("{API.DOC.CMS.IMAGE_PROCESS.WIDTH}")
    private Integer width;

    /**
     * 缩略图高度
     */
    @XComment("{API.DOC.CMS.IMAGE_PROCESS.HEIGHT}")
    private Integer height;

    /**
     * 旋转角度
     */
    @XComment("{API.DOC.CMS.IMAGE_PROCESS.ROTATE_DEGREE}")
    private Integer rotate;

    /**
     * 水平翻转
     */
    private Boolean flipX;

    /**
     * 垂直翻转
     */
    private Boolean flipY;
}
