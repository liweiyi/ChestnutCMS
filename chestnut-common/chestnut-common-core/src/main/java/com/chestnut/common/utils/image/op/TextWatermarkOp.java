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
package com.chestnut.common.utils.image.op;

import com.chestnut.common.exception.ImageException;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.image.TextWatermarkProperties;
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
public class TextWatermarkOp extends JDKImageOp {

    private final String text;

    private final Font font;

    private final Color color;

    private final float opacity;

    private WatermarkPosition position;

    private AlphaComposite alphaComposite;

    private FontMetrics fontMetrics;

    private TextWatermarkProperties point;

    public TextWatermarkOp(String text, Font font, Color color, float opacity, WatermarkPosition position) {
        this.text = text;
        this.font = font;
        this.color = color;
        this.opacity = Math.min(opacity, 1f);
        this.position = position;
    }

    public TextWatermarkOp(String text, Font font, Color color, float opacity, TextWatermarkProperties point) {
        this.text = text;
        this.font = font;
        this.color = color;
        this.opacity = Math.min(opacity, 1f);
        this.point = point;
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
        if (Objects.isNull(point)) {
            Point p = ImageWatermarkOp.calculateWatermarkPoint(originalWidth, originalHeight,
                    fontMetrics.stringWidth(text), fontMetrics.getAscent(), 10, position);
            point = new TextWatermarkProperties(p.x, p.y, 0);
        }
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
//        if (point.getWidth() > 0) {
            drawString(g, watermarkText.getText(), point);
//        } else {
//            g.drawString(watermarkText.getText(), point.getX(), point.getY() + fontMetrics.getAscent() - fontMetrics.getDescent());
//        }
        g.dispose();

        return newImage;
    }

    /**
     * 在指定最大宽度内自动换行绘制文本
     *
     * @param g2d       Graphics2D 对象
     * @param text      要绘制的文本
     * @param point     水印位置信息
     */
    public void drawString(Graphics2D g2d, String text, TextWatermarkProperties point) {
        int y = point.getY();
        FontMetrics fm = g2d.getFontMetrics(); // 用于测量文本宽度
        int lineHeight = (int) (fm.getHeight() * point.getLineHeight());  // 每行高度（包含 ascent 和 descent）

        int lineWidth = 0;
        for (int i = 0; i < text.length(); i++) {
            String c = String.valueOf(text.charAt(i));
            int cWidth = fm.stringWidth(c);
            // 超出行宽并有当前行有至少一个字才换行
            if (point.getWidth() > 0 && lineWidth + cWidth > point.getWidth() && lineWidth > 0) {
                y += lineHeight;
                lineWidth = 0;
            }
            g2d.drawString(c, point.getX() + lineWidth, y + fontMetrics.getAscent() - fontMetrics.getDescent());
            lineWidth += cWidth + point.getSpacing();
        }
    }

    private FontMetrics getFontMetrics(Font font) {
        BufferedImage fontImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics fontImageGraphics = fontImage.getGraphics();
        return fontImageGraphics.getFontMetrics(font);
    }
}