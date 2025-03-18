/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
package com.chestnut.contentcore.domain.vo;

import com.chestnut.common.annotation.XComment;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.util.InternalUrlUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 站点标签数据对象
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class TagSiteVO extends TagBaseVO {

    @XComment("站点ID")
    private Long siteId;

    @XComment("父级站点ID")
    private Long parentId;

    @XComment("站点名称")
    private String name;

    @XComment("站点描述")
    private String description;

    @XComment("站点LOGO")
    private String logo;

    @XComment(value = "站点LOGO访问地址", deprecated = true, forRemoval = "1.6.0")
    private String logoSrc;

    @XComment("站点目录")
    private String path;

    @XComment("站点资源访问域名")
    private String resourceUrl;

    @XComment("所属部门编码")
    private String deptCode;

    @XComment("排序值")
    private Long sortFlag;

    @XComment("SEO关键词")
    private String seoKeywords;

    @XComment("SEO描述")
    private String seoDescription;

    @XComment("SEO标题")
    private String seoTitle;

    @XComment("扩展属性配置")
    private Map<String, String> configProps;

    @XComment("站点访问地址")
    private String link;

    public static TagSiteVO newInstance(CmsSite site, String publishPipeCode, boolean preview) {
        TagSiteVO vo = new TagSiteVO();
        BeanUtils.copyProperties(site, vo);
        if (StringUtils.isNotEmpty(site.getLogo())) {
            // 兼容历史版本
            vo.setLogoSrc(InternalUrlUtils.getActualUrl(site.getLogo(), publishPipeCode, preview));
        }
        return vo;
    }

    public Map<String, String> getConfigProps() {
		if (this.configProps == null) {
			this.configProps = new HashMap<>();
		}
		return configProps;
	}
}
