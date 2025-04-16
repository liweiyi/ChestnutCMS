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
package com.chestnut.common.utils.image;

import com.chestnut.common.exception.ImageException;
import com.chestnut.common.utils.SpringUtils;
import com.chestnut.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ResourceUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.*;

/**
 * 图片处理工具类
 */
@Slf4j
public class ImageUtils {

    public static final String FORMAT_GIF = "gif";

    private static final List<ImageProcessor> imageProcessors = new ArrayList<>();

    static {
        GraphicsEnvironment localGraphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        loadUserFonts().values().forEach(localGraphicsEnvironment::registerFont);
        registerImageProcessor(new JPGImageProcessor());
        registerImageProcessor(new PNGImageProcessor());
    }

    /**
     * 获取系统可用字体系列名称
     */
    public static String[] getSupportFonts() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    }

    /**
     * 获取用户字体
     *
     * @return 用户字体(key=字体名称, value=文件名称)
     */
    public static Map<String, Font> loadUserFonts() {
        try {
            ResourceLoader resourceLoader = SpringUtils.getBean(ResourceLoader.class);
            Resource resource = resourceLoader.getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "fonts/");
            if (!resource.exists() || !resource.getFile().isDirectory()) {
                return Map.of();
            }
            File[] fontFiles = resource.getFile().listFiles(f -> f.getName().matches("(?i).+\\.(ttf|ttc)"));
            if (Objects.isNull(fontFiles) || fontFiles.length == 0) {
                return Map.of();
            }
            Map<String, Font> fontsMap = new HashMap<>();
            for (File fontFile : fontFiles) {
                try {
                    Font f = Font.createFont(Font.TRUETYPE_FONT, fontFile);
                    fontsMap.put(f.getFamily().toLowerCase(), f);
                } catch (Exception e) {
                    log.warn("Load user fonts failed: " + fontFile.getName(), e);
                }
            }
            return fontsMap;
        } catch (IOException e) {
            log.warn("Load user fonts directory failed.", e);
            return Map.of();
        }
    }

	public static Dimension getDimension(File file) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(file);
        return new Dimension(bufferedImage.getWidth(), bufferedImage.getHeight());
    }

    public static Dimension getDimension(InputStream is) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(is);
        return new Dimension(bufferedImage.getWidth(), bufferedImage.getHeight());
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

    public static void registerImageProcessor(ImageProcessor imageProcessor) {
        imageProcessors.add(imageProcessor);
    }

    public static ImageProcessor getImageProcessor(String imageFormat) {
        Optional<ImageProcessor> first = imageProcessors.stream()
                .filter(processor -> processor.check(imageFormat))
                .findFirst();
        if (first.isPresent()) {
            return first.get();
        }
        throw new ImageException("Unsupported image format: " + imageFormat);
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
}