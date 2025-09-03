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

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * 图片裁剪
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class CropOp extends JDKImageOp {

    private final int x;
    private final int y;
    private final int width;
    private final int height;

    public CropOp(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void validate() {
        if (x < 0 || y < 0 || width <= 0 || height <= 0) {
            throw new ImageException("Invalid crop args: x = %d, y = %d, width = %d, height = %d".formatted(x, y, width, height));
        }
    }

    @Override
    public void prepare(int originalWidth, int originalHeight) {
        if (x + width > originalWidth) {
            throw new ImageException("Crop area exceeds " + x + " + " + width + " >= " + originalWidth);
        }
        if (y + height > originalHeight) {
            throw new ImageException("Crop area exceeds " + y + " + " + height + " >= " + originalHeight);
        }
    }

    @Override
    public BufferedImage op(BufferedImage image) {
        BufferedImage target = new BufferedImage(width, height, image.getType());
        Graphics2D graphics = target.createGraphics();
        graphics.drawImage(image, 0, 0, width, height, x, y, x + width, y + height, null);
        graphics.dispose();
        return target;
    }
}