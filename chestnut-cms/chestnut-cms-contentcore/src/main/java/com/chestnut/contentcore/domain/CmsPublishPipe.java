package com.chestnut.contentcore.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.db.domain.BaseEntity;
import com.chestnut.system.fixed.dict.EnableOrDisable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * 发布通道表对象 [cms_publishpipe]
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(CmsPublishPipe.TABLE_NAME)
public class CmsPublishPipe extends BaseEntity {

    private static final long serialVersionUID=1L;
    
    public static final String TABLE_NAME = "cms_publishpipe";

	public static final String SitePropery_Url = "url";

	/**
	 * 发布通道ID
	 */
    @TableId(value = "publishpipe_id", type = IdType.INPUT)
    private Long publishpipeId;

    /**
     * 站点ID
     */
    private Long siteId;

    /**
     * 名称
     */
    @NotBlank
    private String name;

    /**
     * 编码
     */
    @Pattern(regexp = "[A-Za-z0-9_]+")
    private String code;

    /**
     * 发布通道状态
     */
    @NotEmpty
    private String state;

    /**
     * 排序
     */
    private Long sort;
    
    public boolean isEnable() {
    	return EnableOrDisable.isEnable(this.state);
    }
}
