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
