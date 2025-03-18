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
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.image.op.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Objects;

/**
 * ImageHelper
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
public class ImageHelper {

    private ImageInputWrap<?> inputWrap;

    private final ArrayList<ImageOp> imageOps= new ArrayList<>();

    public static ImageHelper of(InputStream is) {
        Assert.notNull(is, () -> new ImageException("Input stream is null!"));

        ImageHelper imageHelper = new ImageHelper();
        imageHelper.inputWrap = new ImageInputStreamWrap(is);
        return imageHelper;
    }

    public static ImageHelper of(BufferedImage bi) {
        Assert.notNull(bi, () -> new ImageException("Input buffered image is null!"));

        ImageHelper imageHelper = new ImageHelper();
        imageHelper.inputWrap = new ImageInputBufferedImageWrap(bi);
        return imageHelper;
    }

    public static ImageHelper of(File imageFile) throws IOException {
        if (Objects.isNull(imageFile) || imageFile.exists()) {
            throw new ImageException("Input file is null or not exists.");
        }
        ImageHelper imageHelper = new ImageHelper();
        imageHelper.inputWrap = new ImageInputFileWrap(imageFile);
        imageHelper.inputWrap.imageFormat = FilenameUtils.getExtension(imageFile.getName());
        return imageHelper;
    }

    public void to(OutputStream os) throws IOException {
        if (StringUtils.isEmpty(this.inputWrap.imageFormat)) {
            throw new ImageException("Mission image format.");
        }
        ImageProcessor imageProcessor = ImageUtils.getImageProcessor(this.inputWrap.imageFormat);
        imageProcessor.process(inputWrap, imageOps, os);
    }

    public void toFile(File output) throws IOException {
        this.inputWrap.imageFormat = FilenameUtils.getExtension(output.getName());
        ImageProcessor imageProcessor = ImageUtils.getImageProcessor(this.inputWrap.imageFormat);
        try (FileOutputStream os = new FileOutputStream(output)) {
            imageProcessor.process(inputWrap, imageOps, os);
        }
    }

    public ImageHelper format(String imageFormat) {
        this.inputWrap.imageFormat = imageFormat;
        return this;
    }

    public ImageHelper resize(int width, int height) {
        if (width == 0 || height == 0) {
            return this;
        }
        ResizeOp op = new ResizeOp(width, height);
        op.validate();
        this.imageOps.add(op);
        return this;
    }

    public ImageHelper watermark(BufferedImage biWatermarkImage, float ratio, float opacity, WatermarkPosition position) {
        ImageWatermarkOp op = new ImageWatermarkOp(biWatermarkImage, ratio, opacity, position);
        op.validate();
        this.imageOps.add(op);
        return this;
    }

    public ImageHelper crop(int x, int y, int width, int height) {
        CropOp op = new CropOp(x, y, width, height);
        op.validate();
        this.imageOps.add(op);
        return this;
    }

    public ImageHelper centerCropAndResize(int width, int height) throws IOException {
        BufferedImage bufferedImage = this.inputWrap.readBufferedImage();
        this.inputWrap = new ImageInputBufferedImageWrap(bufferedImage);

        double ratio = bufferedImage.getWidth() * 1.0 / bufferedImage.getHeight();
        double toRatio = width * 1.0 / height;
        int x = 0, y = 0;
        int cropWidth = bufferedImage.getWidth();
        int cropHeight = bufferedImage.getHeight();
        if (ratio < toRatio) {
            cropHeight = (int) (cropWidth / toRatio);
            y = (bufferedImage.getHeight() - cropHeight) / 2;
        } else if (ratio > toRatio) {
            cropWidth = (int) (toRatio * cropHeight);
            x = (bufferedImage.getWidth() - cropWidth) / 2;
        }
        if (x > 0 || y > 0) {
            this.crop(x, y, cropWidth, cropHeight);
        }
        if (cropWidth <= width && cropHeight <= height) {
            this.resize(width, height);
        }
        return this;
    }

    public ImageHelper rotate(int degree) {
        if (degree == 0) {
            return this;
        }
        RotateOp op = new RotateOp(degree);
        op.validate();
        this.imageOps.add(op);
        return this;
    }

    public ImageHelper flip(boolean flipX, boolean flipY) {
        if (flipX) {
            FlipOp op = new FlipOp(true);
            op.validate();
            this.imageOps.add(op);
        }
        if (flipY) {
            FlipOp op = new FlipOp(false);
            op.validate();
            this.imageOps.add(op);
        }
        return this;
    }

    public ImageHelper flipX() {
        FlipOp op = new FlipOp(true);
        op.validate();
        this.imageOps.add(op);
        return this;
    }

    public ImageHelper flipY() {
        FlipOp op = new FlipOp(false);
        op.validate();
        this.imageOps.add(op);
        return this;
    }

    public ImageHelper watermark(String text, Font font, Color color) {
        TextWatermarkOp op = new TextWatermarkOp(text, font, color, 1, WatermarkPosition.BOTTOM_RIGHT);
        op.validate();
        this.imageOps.add(op);
        return this;
    }

    public ImageHelper watermark(String text, Font font, Color color, float opacity, WatermarkPosition position) {
        TextWatermarkOp op = new TextWatermarkOp(text, font, color, opacity, position);
        op.validate();
        this.imageOps.add(op);
        return this;
    }

    public abstract static class ImageInputWrap<T> {

        protected T input;

        protected String imageFormat;

        abstract BufferedImage readBufferedImage() throws IOException;

        abstract InputStream getInputStream() throws IOException;
    }

    private static class ImageInputStreamWrap extends ImageInputWrap<InputStream> {
        public ImageInputStreamWrap(InputStream is) {
            input = is;
        }

        @Override
        BufferedImage readBufferedImage() throws IOException {
            return ImageIO.read(input);
        }

        @Override
        InputStream getInputStream() {
            return this.input;
        }
    }

    private static class ImageInputBufferedImageWrap extends ImageInputWrap<BufferedImage> {
        public ImageInputBufferedImageWrap(BufferedImage bi) {
            input = bi;
        }

        @Override
        BufferedImage readBufferedImage() {
            return input;
        }

        @Override
        InputStream getInputStream() throws IOException {
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                ImageIO.write(this.input, this.imageFormat, os);
                return new ByteArrayInputStream(os.toByteArray());
            }
        }
    }

    private static class ImageInputFileWrap extends ImageInputWrap<File> {
        public ImageInputFileWrap(File is) {
            input = is;
        }

        @Override
        BufferedImage readBufferedImage() throws IOException {
            return ImageIO.read(input);
        }

        @Override
        InputStream getInputStream() throws IOException {
            return new FileInputStream(this.input);
        }
    }
}
