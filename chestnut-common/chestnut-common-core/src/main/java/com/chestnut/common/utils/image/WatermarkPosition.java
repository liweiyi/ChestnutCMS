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

import java.util.concurrent.ThreadLocalRandom;

/**
 * WatermarkPosition
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public enum WatermarkPosition {

    RANDOM(""), // 随机

    TOP_LEFT("NorthWest"), // 左上

    TOP_CENTER("North"), // 上

    TOP_RIGHT("NorthEast"), // 右上

    CENTER_LEFT("West"), // 左

    CENTER("Center"), // 中

    CENTER_RIGHT("East"), // 右

    BOTTOM_LEFT("SouthWest"), // 左下

    BOTTOM_CENTER("South"), // 下

    BOTTOM_RIGHT("SouthEast"); // 右下

    private final String gmPosition;

    WatermarkPosition(String gmPosition) {
        this.gmPosition = gmPosition;
    }

    public String getGraphicsMagicPosition() {
        return gmPosition;
    }

    public static WatermarkPosition str2Position(String str) {
        WatermarkPosition[] values = WatermarkPosition.values();
        for (WatermarkPosition v : values) {
            if (v.gmPosition.equalsIgnoreCase(str)) {
                return v;
            }
        }
        return WatermarkPosition.BOTTOM_RIGHT;
    }

    public static WatermarkPosition random() {
        return WatermarkPosition.values()[ThreadLocalRandom.current().nextInt(WatermarkPosition.values().length)];
    }
}
