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

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 文字水印位置坐标信息
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@NoArgsConstructor
public class TextWatermarkProperties {

    private int x = 0;

    private int y = 0;

    /**
     * 最大行宽
     */
    private int width = 0;

    /**
     * 行间距
     */
    private float lineHeight = 1.0f;

    /**
     * 字间距
     */
    private int spacing = 0;

    public TextWatermarkProperties(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public TextWatermarkProperties(int x, int y, int width) {
        this.x = x;
        this.y = y;
        this.width = width;
    }

    public TextWatermarkProperties(int x, int y, int width, float lineHeight) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.lineHeight = lineHeight;
    }

    public enum TextAlign {
        LEFT, RIGHT, CENTER;

        public String value() {
            return this.name().toLowerCase();
        }
    }
}