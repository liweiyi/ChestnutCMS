package com.chestnut.common.utils.image;

import java.awt.*;

/**
 * AbstractImageOp
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public abstract class AbstractImageOp implements ImageOp {

    /**
     * 源图尺寸
     */
    private Dimension dimension;

    @Override
    public Dimension getDimension() {
        return dimension;
    }

    @Override
    public void setDimension(Dimension dimension) {
        this.dimension = dimension;
    }
}
