package com.chestnut.advertisement.domain;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Getter;
import lombok.Setter;

/**
 * 广告小时点击/展现统计数据
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(CmsAdHourStat.TABLE_NAME)
public class CmsAdHourStat implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public final static String TABLE_NAME = "cms_ad_hour_stat";

	@TableId(value = "stat_id", type = IdType.AUTO)
	private Long statId;
	
	/**
	 * 所属站点ID
	 */
	private Long siteId;
	
	/**
	 * 统计周期，格式：yyyyMMddHH
	 */
	private String hour;
	
	/**
	 * 广告ID
	 */
	private Long advertisementId;
	
	/**
	 * 点击数
	 */
	private Integer click;
	
	/**
	 * 展现数
	 */
	private Integer view;
	
	/**
	 * 广告名称
	 */
	@TableField(exist = false)
	private String adName;
}
