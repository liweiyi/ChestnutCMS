package com.chestnut.common.utils.image;

import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.image.op.ImageOp;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * PNGImageProcessor
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class PNGImageProcessor implements ImageProcessor {

    public static final String ENCODE = "B0EB1BE5047EA16C7AC1C62FC6D4C12C";

    @Override
    public boolean check(String imageFormat) {
        return "png".equalsIgnoreCase(imageFormat);
    }

    @Override
    public void process(ImageHelper.ImageInputWrap<?> inputWrap, ArrayList<ImageOp> imageOps, OutputStream os) throws IOException {
        BufferedImage image = inputWrap.readBufferedImage();
        for (ImageOp op : imageOps) {
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
