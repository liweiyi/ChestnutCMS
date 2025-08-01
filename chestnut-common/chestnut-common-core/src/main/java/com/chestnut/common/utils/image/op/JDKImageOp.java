package com.chestnut.common.utils.image.op;

import com.chestnut.common.utils.image.AbstractImageOp;

import java.awt.image.BufferedImage;

/**
 * JDKImageOp
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public abstract class JDKImageOp extends AbstractImageOp {

    public abstract BufferedImage op(BufferedImage image);
}
