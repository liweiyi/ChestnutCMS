package com.chestnut.common.utils.image;

import com.chestnut.common.utils.image.op.ImageOp;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * ImageProcessor
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface ImageProcessor {

    boolean check(String imageFormat);

    void process(ImageHelper.ImageInputWrap<?> inputWrap, ArrayList<ImageOp> imageOps, OutputStream os) throws IOException;
}
