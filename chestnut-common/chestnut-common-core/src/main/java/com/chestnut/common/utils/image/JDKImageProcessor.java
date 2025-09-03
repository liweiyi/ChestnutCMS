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

import com.chestnut.common.exception.ImageErrorCode;
import com.chestnut.common.exception.ImageException;
import com.chestnut.common.utils.image.op.*;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDKImageConverter
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(ImageProcessor.BEAN_PREFIX + JDKImageProcessor.ID)
public class JDKImageProcessor implements ImageProcessor {

    public static final String ID = "jdk";

    private static final List<ImageHandler> IMAGE_HANDLERS = new ArrayList<>();

    public JDKImageProcessor() {
        IMAGE_HANDLERS.add(new JPGImageHandler());
        IMAGE_HANDLERS.add(new PNGImageHandler());
    }

    @Override
    public Dimension getDimension(File imageFile) {
        try {
            BufferedImage bufferedImage = ImageIO.read(imageFile);
            return new Dimension(bufferedImage.getWidth(), bufferedImage.getHeight());
        } catch (IOException e) {
            throw ImageErrorCode.IMAGE_PROCESS_FAIL.exception(e);
        }
    }

    @Override
    public void process(File sourceFile, String imageFormat, ArrayList<ImageOp> imageOps, File out) throws Exception {
        try (FileOutputStream os = new FileOutputStream(out)) {
            process(sourceFile, imageFormat, imageOps, os);
        }
    }

    private void process(File sourceFile, String imageFormat, ArrayList<ImageOp> imageOps, OutputStream out) throws Exception {
        Optional<ImageHandler> first = IMAGE_HANDLERS.stream().filter(handler -> handler.isSupport(imageFormat)).findFirst();
        if (first.isEmpty()) {
            throw new ImageException("Unsupported image format: " + imageFormat + ".");
        }
        List<JDKImageOp> ops = imageOps.stream().filter(op -> op instanceof JDKImageOp).map(op -> (JDKImageOp) op).toList();
        first.get().handle(sourceFile, out, ops);
    }

    @Override
    public ImageOp crop(int x, int y, int width, int height) {
        return new CropOp(x, y, width, height);
    }

    @Override
    public ImageOp resize(int width, int height) {
        return new ResizeOp(width, height);
    }

    @Override
    public ImageOp rotate(int degree) {
        return new RotateOp(degree);
    }

    @Override
    public ImageOp flip(boolean horizontal) {
        return new FlipOp(horizontal);
    }

    @Override
    public ImageOp textWatermark(String text, Font font, Color color, float opacity, WatermarkPosition position) {
        return new TextWatermarkOp(text, font, color, opacity, position);
    }

    @Override
    public ImageOp textWatermark(String text, Font font, Color color, float opacity, TextWatermarkProperties point) {
        return new TextWatermarkOp(text, font, color, opacity, point);
    }

    @Override
    public ImageOp imageWatermark(File watermarkImageFile, float ratio, float opacity, WatermarkPosition position) {
        try {
            BufferedImage bufferedImage = ImageIO.read(watermarkImageFile);
            return new ImageWatermarkOp(bufferedImage, ratio, opacity, position);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ImageOp convert(String format, double quality, boolean strip) {
        throw ImageErrorCode.UNSUPPORTED_OP.exception("convert");
    }
}