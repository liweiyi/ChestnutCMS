package com.chestnut.cms.stat.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 栏目内容统计数据
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(CmsCatalogContentStat.TABLE_NAME)
public class CmsCatalogContentStat implements Serializable {
	
	@Serial
	private static final long serialVersionUID = 1L;

	public final static String TABLE_NAME = "cms_catalog_content_stat";

	@TableId(value = "catalog_id", type = IdType.INPUT)
	private Long catalogId;

	/**
	 * 站点ID
	 */
	private Long siteId;

	@TableField(exist = false)
	private Long parentId;

	@TableField(exist = false)
	private String name;

	/**
	 * 初稿内容数量
	 */
	private Integer draftTotal;

	/**
	 * 待发布内容数量
	 */
	private Integer toPublishTotal;

	/**
	 * 已发布内容数量
	 */
	private Integer publishedTotal;

	/**
	 * 已下线内容数量
	 */
	private Integer offlineTotal;

	/**
	 * 重新编辑内容数量
	 */
	private Integer editingTotal;
}
