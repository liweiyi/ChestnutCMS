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

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * 图片缩放
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class ResizeOp implements ImageOp {

    private int width;
    private int height;

    public ResizeOp(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void validate() {
        if (width <= 0 || height <= 0) {
            throw new ImageException("Invalid resize args: width = %d, height = %d".formatted(width, height));
        }
    }

    @Override
    public void prepare(int originalWidth, int originalHeight) {
        double rate = calResizeRate(originalWidth, originalHeight, width, height);
        if (rate >= 1) {
            this.width = originalWidth;
            this.height = originalHeight;
            return;
        }
        width = (int) (originalWidth * rate);
        height = (int) (originalHeight * rate);
    }

    @Override
    public BufferedImage op(BufferedImage image) {
        Image resizeImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage newImage = new BufferedImage(width, height, image.getType());
        Graphics2D g2d = newImage.createGraphics();
        g2d.drawImage(resizeImage, 0, 0, null);
        g2d.dispose();
        return newImage;
    }

    public static double calResizeRate(int width, int height, int toWidth, int toHeight) {
        double rate;
        if (toWidth == 0 && height > toHeight) {
            rate = toHeight * 1.0 / height;
        } else if (toHeight == 0 && width > toWidth) {
            rate = toWidth * 1.0 / width;
        } else {
            rate = Math.min(toWidth * 1.0 / width, toHeight * 1.0 / height);
        }
        return Math.min(rate, 1);
    }
}