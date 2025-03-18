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

import com.chestnut.common.utils.image.op.ImageOp;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * JPGImageProcessor
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component
public class JPGImageProcessor implements ImageProcessor {

    @Override
    public boolean check(String imageFormat) {
        return "jpg".equalsIgnoreCase(imageFormat) || "jpeg".equalsIgnoreCase(imageFormat) || "webp".equalsIgnoreCase(imageFormat);
    }

    @Override
    public void process(ImageHelper.ImageInputWrap<?> inputWrap, ArrayList<ImageOp> imageOps, OutputStream os) throws IOException {
        BufferedImage image = inputWrap.readBufferedImage();
        for (ImageOp op : imageOps) {
            op.prepare(image.getWidth(), image.getHeight());
            image = op.op(image);
        }
        ImageIO.write(image, inputWrap.imageFormat, os);
    }
}
