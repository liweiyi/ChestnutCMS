package com.chestnut.advertisement.pojo.vo;

import com.chestnut.advertisement.pojo.AdSpaceProps;
import com.chestnut.contentcore.domain.vo.PageWidgetVO;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter 
@Setter
@Accessors(chain = true)
public class AdSpaceVO extends PageWidgetVO {

	/**
	 * 广告位自定义属性
	 */
    private AdSpaceProps content;
}
