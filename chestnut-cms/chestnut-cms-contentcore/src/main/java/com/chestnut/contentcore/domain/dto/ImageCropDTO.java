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

@XComment("{API.DOC.CMS.IMAGE_PROCESS.CROP_DTO}")
@Getter
@Setter
public class ImageCropDTO {

    @XComment("{API.DOC.CMS.RESOURCE_MGMT.RESOURCE_ID}")
    private Long resourceId;

    @XComment("{API.DOC.CMS.IMAGE_PROCESS.X}")
    private Integer x;

    @XComment("{API.DOC.CMS.IMAGE_PROCESS.Y}")
    private Integer y;

    @XComment("{API.DOC.CMS.IMAGE_PROCESS.WIDTH}")
    private Integer width;

    @XComment("{API.DOC.CMS.IMAGE_PROCESS.HEIGHT}")
    private Integer height;
}
