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

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * 图片翻转
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class FlipOp implements ImageOp {

    /**
     * 是否水平翻转
     */
    private final boolean horizontal;

    public FlipOp(boolean horizontal) {
        this.horizontal = horizontal;
    }

    @Override
    public BufferedImage op(BufferedImage image) {
        BufferedImage rotatedImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        Graphics2D g = (Graphics2D) rotatedImage.getGraphics();

        AffineTransform at = new AffineTransform();
        if (this.horizontal) {
            at.concatenate(AffineTransform.getScaleInstance(-1, 1));
            at.concatenate(AffineTransform.getTranslateInstance(-image.getWidth(), 0));
        } else {
            at.concatenate(AffineTransform.getScaleInstance(1, -1));
            at.concatenate(AffineTransform.getTranslateInstance(0, -image.getHeight()));
        }
        g.setTransform(at);
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return rotatedImage;
    }
}