/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
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
 * 图片旋转
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class RotateOp extends JDKImageOp {

    private int degree;

    private int newWidth;

    private int newHeight;

    private double radians;

    public RotateOp(int degree) {
        this.degree = degree;
    }

    @Override
    public void prepare(int originalWidth, int originalHeight) {
        if (degree % 360 == 0) {
            degree = 0;
            return;
        }
        this.calculate(originalWidth, originalHeight, degree);
    }

    @Override
    public BufferedImage op(BufferedImage image) {
        BufferedImage rotatedImage = new BufferedImage(this.newWidth, this.newHeight, image.getType());
        Graphics2D g = (Graphics2D) rotatedImage.getGraphics();
//        g.setColor(Color.WHITE);
//        g.fillRect(0, 0, w, h);

        AffineTransform at = new AffineTransform();
        at.translate(this.newWidth / 2.0, this.newHeight / 2.0);
        at.rotate(this.radians); // 旋转图像
        at.translate(-image.getWidth() / 2.0, -image.getHeight() / 2.0);

        g.setTransform(at);
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return rotatedImage;
    }

    private void calculate(int originalWidth, int originalHeight, int degree) {
        // 将角度转换到0-360度之间
        degree = degree % 360;
        if (degree < 0) {
            degree = 360 + degree;
        }
        this.radians = Math.toRadians(degree);

        if (degree == 180) {
            newWidth = originalWidth;
            newHeight = originalHeight;
        } else if (degree == 90 || degree == 270) {
            newWidth = originalHeight;
            newHeight = originalWidth;
        } else {
            int d = originalWidth + originalHeight;
            newWidth = (int) (d * Math.abs(Math.cos(this.radians)));
            newHeight = (int) (d * Math.abs(Math.sin(this.radians)));
        }
    }
}