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
