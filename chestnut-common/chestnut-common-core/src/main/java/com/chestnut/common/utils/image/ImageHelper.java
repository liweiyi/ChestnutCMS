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
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.SystemProperties;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

/**
 * ImageHelper
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
public class ImageHelper {

    /**
     * 临时文件目录
     */
    @Getter
    private final Path TempDirPath = Path.of(SystemProperties.getJavaIoTmpdir(), ".chestnut", "image");

    /**
     * 源图临时文件
     */
    private Path sourceTempPath;

    /**
     * 输入源图片格式
     */
    private String imageFormat;

    /**
     * 输出图片格式
     */
    private String toImageFormat;

    /**
     * 源图尺寸
     */
    private Dimension dimension;

    /**
     * 图片操作列表
     */
    private final ArrayList<ImageOp> imageOps= new ArrayList<>();

    public static ImageHelper of(InputStream is, String imageFormat) throws IOException {
        Assert.notNull(is, ImageErrorCode.SOURCE_INPUT_NOT_NULL::exception);

        ImageHelper imageHelper = new ImageHelper();
        // 创建临时文件目录
        Files.createDirectories(imageHelper.TempDirPath);
        // 输入源写入临时文件
        Path sourceTempPath = imageHelper.TempDirPath.resolve(IdUtils.simpleUUID() + "." + imageFormat);
        Files.copy(is, sourceTempPath, StandardCopyOption.REPLACE_EXISTING);

        imageHelper.sourceTempPath = sourceTempPath;
        imageHelper.imageFormat = imageFormat;
        imageHelper.dimension = ImageUtils.getImageProcessor().getDimension(sourceTempPath.toFile());
        return imageHelper;
    }

    public static ImageHelper of(File imageFile) throws IOException {
        Assert.notNull(imageFile, ImageErrorCode.SOURCE_INPUT_NOT_NULL::exception);
        Assert.isTrue(imageFile.exists(), ImageErrorCode.SOURCE_FILE_NOT_EXISTS::exception);

        ImageHelper imageHelper = new ImageHelper();
        // 创建临时文件目录
        Files.createDirectories(imageHelper.TempDirPath);
        // 输入源写入临时文件
        String ext = FilenameUtils.getExtension(imageFile.getName());
        Path sourceTempPath = imageHelper.TempDirPath.resolve(IdUtils.simpleUUID() + "." + ext);
        Files.copy(imageFile.toPath(), sourceTempPath, StandardCopyOption.REPLACE_EXISTING);

        imageHelper.sourceTempPath = sourceTempPath;
        imageHelper.imageFormat = ext;
        imageHelper.dimension = ImageUtils.getImageProcessor().getDimension(sourceTempPath.toFile());
        return imageHelper;
    }

    public void to(OutputStream os) throws Exception {
        if (this.imageOps.isEmpty()) {
            Files.copy(this.sourceTempPath, os);
            return;
        }
        String ext = StringUtils.isEmpty(this.toImageFormat) ? imageFormat : toImageFormat;
        Path destFilePath = this.sourceTempPath.resolveSibling(IdUtils.simpleUUID() + "." + ext);
        try {
            ImageUtils.getImageProcessor().process(this.sourceTempPath.toFile(), this.imageFormat, imageOps, destFilePath.toFile());
            Files.copy(destFilePath, os);
        } finally {
            Files.deleteIfExists(this.sourceTempPath);
            Files.deleteIfExists(destFilePath);
        }
    }

    public void toFile(File output) throws Exception {
        if (this.imageOps.isEmpty()) {
            Files.copy(this.sourceTempPath, output.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return;
        }
        try {
            ImageUtils.getImageProcessor().process(this.sourceTempPath.toFile(), this.imageFormat, imageOps, output);
        } finally {
            Files.deleteIfExists(this.sourceTempPath);
        }
    }

    /**
     * 等比生成缩略图
     *
     * @param width 宽度
     * @param height 高度
     */
    public ImageHelper resize(int width, int height) {
        if (width == 0 || height == 0) {
            return this;
        }
        ImageOp op = ImageUtils.getImageProcessor().resize(width, height);
        op.setDimension(this.dimension);
        op.validate();
        this.imageOps.add(op);
        return this;
    }

    /**
     * 图片水印
     *
     * @param watermarkImageFile 水印图片文件
     * @param ratio 水印相对图片占比
     * @param opacity 水印透明度
     * @param position 水印位置
     */
    public ImageHelper watermark(File watermarkImageFile, float ratio, float opacity, WatermarkPosition position) {
        ImageOp op = ImageUtils.getImageProcessor().imageWatermark(watermarkImageFile, ratio, opacity, position);
        op.setDimension(this.dimension);
        op.validate();
        this.imageOps.add(op);
        return this;
    }

    /**
     * 裁剪，偏移原点为图片左上角
     *
     * @param x X轴偏移像素
     * @param y Y轴偏移像素
     * @param width 宽度
     * @param height 高度
     */
    public ImageHelper crop(int x, int y, int width, int height) {
        ImageOp op = ImageUtils.getImageProcessor().crop(x, y, width, height);
        op.setDimension(this.dimension);
        op.validate();
        this.imageOps.add(op);
        return this;
    }

    /**
     * 居中裁剪并缩放
     *
     * @param width 裁剪/缩放宽度
     * @param height 裁剪/缩放高度
     */
    public ImageHelper centerCropAndResize(int width, int height) {
        double ratio = dimension.getWidth() / dimension.getHeight();
        double toRatio = width * 1.0 / height;
        int x = 0, y = 0;
        int cropWidth = dimension.width;
        int cropHeight = dimension.height;
        if (ratio < toRatio) {
            cropHeight = (int) (cropWidth / toRatio);
            y = (dimension.height - cropHeight) / 2;
        } else if (ratio > toRatio) {
            cropWidth = (int) (toRatio * cropHeight);
            x = (dimension.width - cropWidth) / 2;
        }
        if (x > 0 || y > 0) {
            this.crop(x, y, cropWidth, cropHeight);
        }
        if (cropWidth <= width && cropHeight <= height) {
            this.resize(width, height);
        }
        return this;
    }

    /**
     * 旋转
     *
     * @param degree 旋转角度
     */
    public ImageHelper rotate(int degree) {
        if (degree == 0) {
            return this;
        }
        ImageOp op = ImageUtils.getImageProcessor().rotate(degree);
        op.setDimension(this.dimension);
        op.validate();
        this.imageOps.add(op);
        return this;
    }

    /**
     * 翻转
     *
     * @param flipX 是否水平翻转
     * @param flipY 是否垂直翻转
     */
    public ImageHelper flip(boolean flipX, boolean flipY) {
        if (flipX) {
            ImageOp op = ImageUtils.getImageProcessor().flip(true);
            op.setDimension(this.dimension);
            op.validate();
            this.imageOps.add(op);
        }
        if (flipY) {
            ImageOp op = ImageUtils.getImageProcessor().flip(false);
            op.setDimension(this.dimension);
            op.validate();
            this.imageOps.add(op);
        }
        return this;
    }

    /**
     * 水平翻转
     */
    public ImageHelper flipX() {
        ImageOp op = ImageUtils.getImageProcessor().flip(true);
        op.setDimension(this.dimension);
        op.validate();
        this.imageOps.add(op);
        return this;
    }

    /**
     * 垂直翻转
     */
    public ImageHelper flipY() {
        ImageOp op = ImageUtils.getImageProcessor().flip(false);
        op.setDimension(this.dimension);
        op.validate();
        this.imageOps.add(op);
        return this;
    }

    /**
     * 文字水印
     *
     * @param text 文本字符
     * @param font 字体
     * @param color 颜色
     */
    public ImageHelper textWatermark(String text, Font font, Color color) {
        ImageOp op = ImageUtils.getImageProcessor().textWatermark(text, font, color, 1, WatermarkPosition.BOTTOM_RIGHT);
        op.setDimension(this.dimension);
        op.validate();
        this.imageOps.add(op);
        return this;
    }

    /**
     * 文字水印
     *
     * @param text 文本字符
     * @param font 字体
     * @param color 颜色
     * @param opacity 透明度，取值范围：[0, 1]
     * @param position 水印位置
     */
    public ImageHelper textWatermark(String text, Font font, Color color, float opacity, WatermarkPosition position) {
        ImageOp op = ImageUtils.getImageProcessor().textWatermark(text, font, color, opacity, position);
//        TextWatermarkOp op = new TextWatermarkOp(text, font, color, opacity, position);
        op.validate();
        this.imageOps.add(op);
        return this;
    }

    /**
     * 转换图片格式
     *
     * @param format 转换的图片格式
     * @param quality 图片质量，取值范围：[0, 100]
     * @param strip 是否移除图片元信息
     */
    public ImageHelper convert(String format, double quality, boolean strip) {
        if (this.imageFormat.equals(format)) {
            return this;
        }
        this.toImageFormat = format;
        ImageOp op = ImageUtils.getImageProcessor().convert(format, quality, strip);
        op.validate();
        this.imageOps.add(op);
        return this;
    }

    public ImageHelper convert(String format, double quality) {
        return convert(format, quality, true);
    }

    public ImageHelper convert(String format) {
        return convert(format, 100, true);
    }

    public abstract static class ImageInputWrap<T> {

        protected T input;

        protected String imageFormat;

        abstract protected void writeTo(Path path) throws IOException;

        abstract protected void writeTo(OutputStream os) throws IOException;
    }

    private static class ImageInputStreamWrap extends ImageInputWrap<InputStream> {
        public ImageInputStreamWrap(InputStream is, String imageFormat) {
            this.input = is;
            this.imageFormat = imageFormat;
        }

        @Override
        protected void writeTo(Path path) throws IOException {
            Files.copy(this.input, path, StandardCopyOption.REPLACE_EXISTING);
        }

        @Override
        protected void writeTo(OutputStream os) throws IOException {
            this.input.transferTo(os);
        }
    }

    private static class ImageInputFileWrap extends ImageInputWrap<File> {
        public ImageInputFileWrap(File file) throws IOException {
            this.input = file;
            this.imageFormat = FilenameUtils.getExtension(file.getName());
        }

        @Override
        protected void writeTo(Path path) throws IOException {
            Files.copy(this.input.toPath(), path, StandardCopyOption.REPLACE_EXISTING);
        }

        @Override
        protected void writeTo(OutputStream os) throws IOException {
            Files.copy(this.input.toPath(), os);
        }
    }
}
