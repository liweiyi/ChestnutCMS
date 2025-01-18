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

import com.chestnut.common.utils.image.WatermarkPosition;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * 图片水印
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class ImageWatermarkOp implements ImageOp {

    private BufferedImage biWatermarkImage;

    private final float ratio;

    private final float opacity;

    private final WatermarkPosition position;

    private AlphaComposite alphaComposite;

    private Point point;

    public ImageWatermarkOp(BufferedImage biWatermarkImage, float ratio, float opacity, WatermarkPosition position) {
        this.biWatermarkImage = biWatermarkImage;
        this.ratio = ratio;
        this.opacity = opacity;
        this.position = position;
    }

    @Override
    public void prepare(int originalWidth, int originalHeight) {
        if (opacity < 1) {
            alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);
        }
        // 水印图大小处理
        float maxWatermarkImageW = originalWidth * ratio;
        float maxWatermarkImageH = originalHeight * ratio;
        if (maxWatermarkImageW < biWatermarkImage.getWidth() || maxWatermarkImageH < biWatermarkImage.getHeight()) {
            float scale = Math.min(maxWatermarkImageW / biWatermarkImage.getWidth(),
                    maxWatermarkImageH / biWatermarkImage.getHeight());
            biWatermarkImage = resizeWatermarkImage(biWatermarkImage, scale);
        }
        this.calculate(originalWidth, originalHeight,
                biWatermarkImage.getWidth(), biWatermarkImage.getHeight(), position);
    }

    @Override
    public BufferedImage op(BufferedImage image) {
        BufferedImage newImg = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        Graphics2D g = newImg.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, 0, 0, null);
        if (Objects.nonNull(alphaComposite)) {
            g.setComposite(alphaComposite);
        }
        g.drawImage(biWatermarkImage, this.point.x, this.point.y, null);
        g.dispose();

        return newImg;
    }

    public BufferedImage resizeWatermarkImage(BufferedImage bufferedImage, double rate) {
        int width = (int) (bufferedImage.getWidth() * rate);
        int height = (int) (bufferedImage.getHeight() * rate);

        Image resizeImage = bufferedImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage newImage = new BufferedImage(width, height, bufferedImage.getType());
        Graphics2D g2d = newImage.createGraphics();
        g2d.drawImage(resizeImage, 0, 0, null);
        g2d.dispose();
        return newImage;
    }

    public static BufferedImage resize(BufferedImage image, int width, int height) {
        Image resizeImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage newImage = new BufferedImage(width, height, image.getType());
        Graphics2D g2d = newImage.createGraphics();
        g2d.drawImage(resizeImage, 0, 0, null);
        g2d.dispose();
        return newImage;
    }

    private void calculate(int sourceWidth, int sourceHeight, int targetWidth, int targetHeight,
                           WatermarkPosition position) {
        this.point = calculateWatermarkPoint(sourceWidth, sourceHeight, targetWidth, targetHeight, 0, position);
    }

    public static Point calculateWatermarkPoint(int sourceWidth, int sourceHeight, int targetWidth, int targetHeight,
                                  int padding, WatermarkPosition position) {
        if (position == WatermarkPosition.RANDOM) {
            position = WatermarkPosition.random();
        }
        int x = 0, y = 0;
        if (position == WatermarkPosition.TOP_LEFT) {
            x = padding;
            y = padding;
        } else if (position == WatermarkPosition.TOP_CENTER) {
            x = sourceWidth / 2 - targetWidth / 2;
            y = padding;
        } else if (position == WatermarkPosition.TOP_RIGHT) {
            x = sourceWidth - targetWidth - padding;
            y = padding;
        } else if (position == WatermarkPosition.CENTER_LEFT) {
            x = padding;
            y = sourceHeight / 2 - targetHeight / 2;
        } else if (position == WatermarkPosition.CENTER) {
            x = sourceWidth / 2 - targetWidth / 2;
            y = sourceHeight / 2 - targetHeight / 2;
        } else if (position == WatermarkPosition.CENTER_RIGHT) {
            x = sourceWidth - targetWidth - padding;;
            y = sourceHeight / 2 - targetHeight / 2;
        } else if (position == WatermarkPosition.BOTTOM_LEFT) {
            x = padding;
            y = sourceHeight - targetHeight - padding;
        } else if (position == WatermarkPosition.BOTTOM_CENTER) {
            x = sourceWidth / 2 - targetWidth / 2;
            y = sourceHeight - targetHeight - padding;
        } else if (position == WatermarkPosition.BOTTOM_RIGHT) {
            x = sourceWidth - targetWidth - padding;
            y = sourceHeight - targetHeight - padding;
        }
        return new Point(x, y);
    }
}