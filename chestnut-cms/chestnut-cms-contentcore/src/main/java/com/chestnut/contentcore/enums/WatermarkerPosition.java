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
