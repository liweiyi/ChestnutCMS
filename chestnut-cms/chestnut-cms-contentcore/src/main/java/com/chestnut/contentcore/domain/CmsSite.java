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
package com.chestnut.contentcore.domain;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.db.domain.BaseEntity;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.impl.PublishPipeProp_IndexTemplate;
import com.chestnut.contentcore.core.impl.PublishPipeProp_SiteUrl;
import com.chestnut.contentcore.core.impl.PublishPipeProp_StaticSuffix;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.Strings;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

    @Serial
    private static final long serialVersionUID = 1L;
    
    public static final String TABLE_NAME = "cms_site";

    public static final int MAX_ANCESTORS_LENGTH = 500;

    /**
     * 站点ID
     */
    @TableId(value = "site_id", type = IdType.INPUT)
    private Long siteId;

    /**
     * 父级站点ID
     */
    @XComment("{CMS.SITE.PARENT_ID}")
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private Long parentId;

    /**
     * 从一级站点到本站的 ID 路径，以逗号分隔
     */
    @XComment("{CMS.SITE.ANCESTORS}")
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private String ancestors;

    /**
     * 站点层级，一级站点为 1
     */
    @XComment("{CMS.SITE.TREE_LEVEL}")
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private Integer treeLevel;

    /**
     * 直接子站点数量
     */
    @XComment("{CMS.SITE.CHILD_COUNT}")
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private Integer childCount;

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
    @XComment("{CC.ENTITY.SORT}")
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private Long sortFlag;

    /**
     * 置顶时间戳，未置顶时为 0
     */
    @XComment("{CMS.CONTENT.TOP_FLAG}")
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private Long topFlag;

    /**
     * 置顶结束时间，空值表示持续置顶
     */
    @XComment("{API.DOC.CMS.CONTENT.TOP_END_TIME}")
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime topDate;

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
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private Map<String, Map<String, Object>> publishPipeProps;

    /**
     * 扩展属性配置
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private Map<String, String> configProps;

    public boolean isTopAt(LocalDateTime now) {
        return topFlag != null && topFlag > 0 && (topDate == null || topDate.isAfter(now));
    }

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
        return this.publishPipeProps.computeIfAbsent(publishPipeCode, k -> new HashMap<>());
    }
    
    public String getIndexTemplate(String publishPipeCode) {
		return PublishPipeProp_IndexTemplate.getValue(publishPipeCode, this.publishPipeProps);
    }
    
    public String getStaticSuffix(String publishPipeCode) {
		return PublishPipeProp_StaticSuffix.getValue(publishPipeCode, this.publishPipeProps);
    }
    
    public String getUrl(String publishPipeCode) {
		String ppUrl = PublishPipeProp_SiteUrl.getValue(publishPipeCode, this.publishPipeProps);
        if (StringUtils.isNotBlank(ppUrl)) {
            ppUrl = Strings.CS.appendIfMissing(ppUrl, "/");
        }
		return Objects.requireNonNullElse(ppUrl, StringUtils.EMPTY);
    }
}
