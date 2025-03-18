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

import com.chestnut.common.exception.ImageException;
import com.chestnut.common.utils.Assert;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;

/**
 * WatermarkText
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class WatermarkText extends Watermark {

    /**
     * 文字
     */
    private String text;

    /**
     * 字体信息
     */
    private Font font;

    /**
     * 文字颜色
     */
    private Color color;

    private WatermarkText() {}

    public static WatermarkText of(String text, WatermarkPosition position) {
        Assert.notEmpty(text, () -> new ImageException("Watermark text is empty."));
        return of(text, new Font("宋体", Font.BOLD, 24), Color.BLACK, position, 1f);
    }

    public static WatermarkText of(String text, Font font, Color color, WatermarkPosition position, float opacity) {
        Assert.notEmpty(text, () -> new ImageException("Watermark text is empty."));
        if (position == WatermarkPosition.RANDOM) {
            position = WatermarkPosition.random();
        }
        WatermarkText watermarkText = new WatermarkText();
        watermarkText.setText(text);
        watermarkText.setFont(font);
        watermarkText.setColor(color);
        watermarkText.setOpacity(opacity);
        watermarkText.setPosition(position);
        return watermarkText;
    }
}
