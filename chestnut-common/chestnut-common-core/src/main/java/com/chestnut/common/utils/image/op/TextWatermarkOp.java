/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
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
package com.chestnut.common.utils.image.op;

import com.chestnut.common.exception.ImageException;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.image.WatermarkPosition;
import com.chestnut.common.utils.image.WatermarkText;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * 文字水印
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class TextWatermarkOp implements ImageOp {

    private final String text;

    private final Font font;

    private final Color color;

    private final float opacity;

    private final WatermarkPosition position;

    private AlphaComposite alphaComposite;

    private FontMetrics fontMetrics;

    private Point point;

    public TextWatermarkOp(String text, Font font, Color color, float opacity, WatermarkPosition position) {
        this.text = text;
        this.font = font;
        this.color = color;
        this.opacity = Math.min(opacity, 1f);
        this.position = position;
    }

    @Override
    public void validate() {
        if (StringUtils.isBlank(this.text)) {
            throw new ImageException("Text cannot be empty for text watermark image op.");
        }
        if (opacity == 0) {
            throw new ImageException("Opacity cannot be zero for text watermark image op.");
        }
    }

    @Override
    public void prepare(int originalWidth, int originalHeight) {
        if (opacity < 1) {
            alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);
        }
        fontMetrics = this.getFontMetrics(font);
        point = ImageWatermarkOp.calculateWatermarkPoint(originalWidth, originalHeight,
                fontMetrics.stringWidth(text), fontMetrics.getAscent(), 10, position);
    }

    @Override
    public BufferedImage op(BufferedImage image) {
        WatermarkText watermarkText = WatermarkText.of(text, font, color, position, opacity);
        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = newImage.createGraphics();
        g.drawImage(image, 0, 0, null);
        if (Objects.nonNull(alphaComposite)) {
            g.setComposite(alphaComposite);
        }
        g.setColor(watermarkText.getColor());
        g.setFont(watermarkText.getFont());
        g.drawString(watermarkText.getText(), point.x, point.y + fontMetrics.getAscent() - fontMetrics.getDescent());
        g.dispose();

        return newImage;
    }

    private FontMetrics getFontMetrics(Font font) {
        BufferedImage fontImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics fontImageGraphics = fontImage.getGraphics();
        return fontImageGraphics.getFontMetrics(font);
    }
}