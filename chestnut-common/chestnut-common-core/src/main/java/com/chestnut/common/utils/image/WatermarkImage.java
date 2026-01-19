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

import com.chestnut.common.exception.ImageException;
import com.chestnut.common.utils.Assert;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FilenameUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * WatermarkImage
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class WatermarkImage extends Watermark {

    /**
     * 水印图
     */
    private BufferedImage watermarkImage;

    /**
     * 水印图占比大小上限，默认：30%
     */
    private float ratio = 0.3f;

    private WatermarkImage() {}

    public static WatermarkImage of(String absolutePath) throws IOException {
        return of(absolutePath, 0.3f, WatermarkPosition.BOTTOM_RIGHT, 1f);
    }

    public static WatermarkImage of(String absolutePath, float ratio, WatermarkPosition position, float opacity) throws IOException {
        File f = new File(absolutePath);
        Assert.isTrue(f.exists(), () -> new FileNotFoundException("Watermark image not found."));

        String ext = FilenameUtils.getExtension(f.getName());
        if (ImageFormat.GIF.ext().equalsIgnoreCase(ext)) {
            throw new ImageException("Unsupported gif watermark image");
        }
        if (ratio <= 0 || ratio > 1) {
            throw new ImageException("Invalid watermark image ratio: " + ratio);
        }
        BufferedImage bufferedImage = ImageIO.read(f);
        return of(bufferedImage, ratio, position, opacity);
    }

    public static WatermarkImage of(BufferedImage bi, float ratio, WatermarkPosition position, float opacity) {
        Assert.notNull(bi, () -> new ImageException("Watermark image is null!"));
        if (ratio <= 0 || ratio > 1) {
            throw new ImageException("Invalid watermark image ratio: " + ratio);
        }
        if (position == WatermarkPosition.RANDOM) {
            position = WatermarkPosition.random();
        }

        WatermarkImage watermarkImage = new WatermarkImage();
        watermarkImage.setWatermarkImage(bi);
        watermarkImage.setRatio(ratio);
        watermarkImage.setPosition(position);
        watermarkImage.setOpacity(opacity);
        return watermarkImage;
    }
}
