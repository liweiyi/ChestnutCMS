/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageRotateDTO {

    private Long resourceId;

    /**
     * 缩略图宽度
     */
    private Integer width;

    /**
     * 缩略图高度
     */
    private Integer height;

    /**
     * 旋转角度
     */
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
