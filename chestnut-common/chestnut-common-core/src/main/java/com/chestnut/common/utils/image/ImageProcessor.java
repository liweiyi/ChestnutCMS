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

import java.awt.*;
import java.io.File;
import java.util.ArrayList;

/**
 * GifImageProcessor
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface ImageProcessor {

    String BEAN_PREFIX = "ImageProcessor_";

    Dimension getDimension(File imageFile);

    void process(File sourcePath, String imageFormat, ArrayList<ImageOp> imageOps, File out) throws Exception;

    ImageOp crop(int x, int y, int width, int height);

    ImageOp resize(int width, int height);

    ImageOp rotate(int degree);

    ImageOp flip(boolean horizontal);

    ImageOp textWatermark(String text, Font font, Color color, float opacity, WatermarkPosition position);

    ImageOp textWatermark(String text, Font font, Color color, float opacity, TextWatermarkProperties point);

    ImageOp imageWatermark(File watermarkImageFile, float ratio, float opacity, WatermarkPosition position);

    ImageOp convert(String format, double quality, boolean strip);
}
