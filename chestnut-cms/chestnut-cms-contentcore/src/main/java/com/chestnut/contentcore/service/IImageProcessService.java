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
package com.chestnut.contentcore.service;

import com.chestnut.contentcore.domain.dto.ImageCropDTO;
import com.chestnut.contentcore.domain.dto.ImageRotateDTO;

import java.io.IOException;

/**
 * IImageProcessService
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IImageProcessService {

    /**
     * 图片裁剪
     *
     * @param dto
     * @throws IOException
     */
    void cropImage(ImageCropDTO dto) throws IOException;

    /**
     * 图片翻转
     *
     * @param dto
     * @throws IOException
     */
    void rotateImage(ImageRotateDTO dto) throws IOException;
}
