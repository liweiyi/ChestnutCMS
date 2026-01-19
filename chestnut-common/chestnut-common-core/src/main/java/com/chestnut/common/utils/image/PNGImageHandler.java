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
package com.chestnut.common.utils.image;

import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.image.op.JDKImageOp;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * PNGImageProcessor
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class PNGImageHandler implements ImageHandler {

    public static final String ENCODE = "B0EB1BE5047EA16C7AC1C62FC6D4C12C";

    @Override
    public boolean isSupport(String imageFormat) {
        return "png".equalsIgnoreCase(imageFormat);
    }

    @Override
    public void handle(File imageFile, OutputStream os, java.util.List<JDKImageOp> imageOps) throws IOException {
        BufferedImage image = ImageIO.read(imageFile);
        for (JDKImageOp op : imageOps) {
            op.prepare(image.getWidth(), image.getHeight());
            image = op.op(image);
            encode(image);
        }
        ImageIO.write(image, "png", os);
    }

    private void encode(BufferedImage image) {
        String binary = StringUtils.toBinary(ENCODE.length() + "." + ENCODE);
        int index = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (index < binary.length()) {
                    int pixel = image.getRGB(x, y);
                    Color color = new Color(pixel);
                    int replace = binary.charAt(index) == '0' ? 0 : 1;
                    int newRed = color.getRed() & 0b11111110 | (replace & 1);
                    Color newColor = new Color(newRed, color.getGreen(), color.getBlue(), color.getAlpha());
                    image.setRGB(x, y, newColor.getRGB());
                    index++;
                } else {
                    return;
                }
            }
        }
    }
}
