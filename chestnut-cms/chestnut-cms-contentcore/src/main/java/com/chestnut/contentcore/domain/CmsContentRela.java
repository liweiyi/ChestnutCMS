package com.chestnut.contentcore.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.db.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 关联内容表对象 [cms_content_rela]
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName(CmsContentRela.TABLE_NAME)
public class CmsContentRela extends BaseEntity {

	public static final String TABLE_NAME = "cms_content_rela";

	@TableId(value = "rela_id", type = IdType.INPUT)
	private Long relaId;

	/**
	 * 站点ID
	 */
	private Long siteId;
	
	/**
	 * 内容ID
	 */
	private Long contentId;

	/**
	 * 关联内容ID
	 */
	private Long relaContentId;
}
