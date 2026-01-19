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
import com.chestnut.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StreamUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;
import java.util.Objects;

/**
 * 图片处理工具类
 */
@Slf4j
public class ImageUtils {

    private static ImageProcessor IMAGE_PROCESSOR = null;

    public static ImageProcessor getImageProcessor() {
        Assert.notNull(IMAGE_PROCESSOR, () -> new ImageException("No available image processors."));
        return IMAGE_PROCESSOR;
    }

    public static void setImageProcessor(ImageProcessor imageProcessor) {
        IMAGE_PROCESSOR = imageProcessor;
    }

	public static Dimension getDimension(File file) throws IOException {
        return IMAGE_PROCESSOR.getDimension(file);
    }

    /**
     * 获取缩略图文件名
     *
     * @param fileName
     * @param width
     * @param height
     * @return
     */
    public static String getThumbnailFileName(String fileName, int width, int height) {
        String prefix = StringUtils.substringBeforeLast(fileName, ".");
        String suffix = StringUtils.substringAfterLast(fileName, ".");
        return prefix + "_" + width + "x" + height + "." + suffix;
    }

    public static BufferedImage text2Image(String text, Font font, Color color) throws IOException {
        return text2Image(text, font, color, new Color(0, 0, 0, 0)); // 透明背景
    }

    public static BufferedImage text2Image(String text, Font font, Color color, Color backgroundColor) throws IOException {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int width = fm.stringWidth(text);
        int height = fm.getHeight();

        g2d.dispose();

        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g2d = img.createGraphics();

        g2d.setPaint(backgroundColor);
        g2d.fillRect(0, 0, width, height);

        g2d.setColor(color);
        g2d.setFont(font);
        g2d.drawString(text, 0, fm.getAscent());
        g2d.dispose();
        ImageIO.write(img, "png", new File("e:/image_test/test.png"));
        return img;
    }

    public static boolean isBase64Image(String base64) {
        if (!StringUtils.startsWithIgnoreCase(base64, "data:image/")) {
            return false;
        }
        String encode = StringUtils.substringAfter(StringUtils.substringBefore(base64, ","), ";");
        return "base64".equalsIgnoreCase(encode);
    }

    public static boolean isBase64Image(Object v) {
        if (Objects.isNull(v)) {
            return false;
        }
        return isBase64Image(v.toString());
    }

    public static BufferedImage base64ToImage(String base64Str) throws IOException {
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] bytes = decoder.decode(base64Str);
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            return ImageIO.read(inputStream);
        }
    }

    public static String imageToBase64(BufferedImage templateImage, String imageFormat) throws IOException {
        Base64.Encoder encoder = Base64.getEncoder();
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ImageIO.write(templateImage, imageFormat, os);
            return encoder.encodeToString(os.toByteArray()).trim();
        }
    }

    public static String streamToBase64(InputStream is) throws IOException {
        byte[] bytes = StreamUtils.copyToByteArray(is);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
