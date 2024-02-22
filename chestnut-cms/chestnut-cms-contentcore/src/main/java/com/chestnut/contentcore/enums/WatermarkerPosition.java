/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
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
package com.chestnut.contentcore.enums;

import net.coobird.thumbnailator.geometry.Position;
import net.coobird.thumbnailator.geometry.Positions;

public enum WatermarkerPosition {

	TOP_LEFT(Positions.TOP_LEFT), // 左上

	TOP_CENTER(Positions.TOP_CENTER), // 上

	TOP_RIGHT(Positions.TOP_RIGHT), // 右上

	CENTER_LEFT(Positions.CENTER_LEFT), // 左

	CENTER(Positions.CENTER), // 中

	CENTER_RIGHT(Positions.CENTER_RIGHT), // 右

	BOTTOM_LEFT(Positions.BOTTOM_LEFT), // 左下

	BOTTOM_CENTER(Positions.BOTTOM_CENTER), // 下

	BOTTOM_RIGHT(Positions.BOTTOM_RIGHT); // 右下
	
	private Position position;
	
	WatermarkerPosition(Position position) {
		this.position = position;
	}
	
	public Position position() {
		return position;
	}
}
