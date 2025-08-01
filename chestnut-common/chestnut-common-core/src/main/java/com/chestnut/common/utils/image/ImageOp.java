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
package com.chestnut.common.utils.image;

import java.awt.*;

/**
 * 图片操作
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface ImageOp {

    /**
     * 操作参数校验
     */
    default void validate() {}

    /**
     * 预处理
     */
    default void prepare(int originalWidth, int originalHeight) {}

    /**
     * 源图尺寸
     */
    void setDimension(Dimension dimension);

    Dimension getDimension();
}