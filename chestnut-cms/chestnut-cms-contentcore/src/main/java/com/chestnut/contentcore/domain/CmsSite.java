package com.chestnut.contentcore.domain;

import java.util.HashMap;
import java.util.Map;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.chestnut.common.db.domain.BaseEntity;
import com.chestnut.contentcore.core.impl.PublishPipeProp_IndexTemplate;
import com.chestnut.contentcore.core.impl.PublishPipeProp_SiteUrl;
import com.chestnut.contentcore.core.impl.PublishPipeProp_StaticSuffix;

import lombok.Getter;
import lombok.Setter;

/**
 * 站点表对象 [cms_site]
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(value = CmsSite.TABLE_NAME, autoResultMap = true)
public class CmsSite extends BaseEntity {

    private static final long serialVersionUID = 1L;
    
    public static final String TABLE_NAME = "cms_site";

    /**
     * 站点ID
     */
    @TableId(value = "site_id", type = IdType.INPUT)
    private Long siteId;

    /**
     * 父级站点ID
     */
    private Long parentId;

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * logo
     */
    private String logo;

    /**
     * logo预览地址
     */
    @TableField(exist = false)
    private String logoSrc;

    /**
     * 目录（唯一）
     */
    private String path;

    @TableField(exist = false)
    private String link;

    /**
     * 站点资源访问域名
     */
    private String resourceUrl;

    /**
     * 所属部门编码
     */
    private String deptCode;

    /**
     * 排序标识
     */
    private Long sortFlag;

    /**
     * SEO关键词
     */
    private String seoKeywords;

    /**
     * SEO描述
     */
    private String seoDescription;

    /**
     * SEO标题
     */
    private String seoTitle;

    /**
     * 发布通道配置
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Map<String, Object>> publishPipeProps;

    /**
     * 扩展属性配置
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, String> configProps;

	public Map<String, String> getConfigProps() {
		if (this.configProps == null) {
			this.configProps = new HashMap<>();
		}
		return configProps;
	}
    
    public Map<String, Object> getPublishPipeProps(String publishPipeCode) {
    	if (this.publishPipeProps == null) {
    		this.publishPipeProps = new HashMap<>();
    	}
    	Map<String, Object> map = this.publishPipeProps.get(publishPipeCode);
    	if (map == null) {
    		map = new HashMap<>();
    		this.publishPipeProps.put(publishPipeCode, map);
    	}
    	return map;
    }
    
    public String getIndexTemplate(String publishPipeCode) {
		return PublishPipeProp_IndexTemplate.getValue(publishPipeCode, this.publishPipeProps);
    }
    
    public String getStaticSuffix(String publishPipeCode) {
		return PublishPipeProp_StaticSuffix.getValue(publishPipeCode, this.publishPipeProps);
    }
    
    public String getUrl(String publishPipeCode) {
		String ppUrl = PublishPipeProp_SiteUrl.getValue(publishPipeCode, this.publishPipeProps);
		if (ppUrl != null && !ppUrl.endsWith("/")) {
			ppUrl += "/";
		}
		return ppUrl;
    }
}
