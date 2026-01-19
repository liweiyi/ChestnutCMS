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

    @XComment("{CMS.SITE.ID}")
    private Long siteId;

    @XComment("{CMS.SITE.PARENT_ID}")
    private Long parentId;

    @XComment("{CMS.SITE.NAME}")
    private String name;

    @XComment("{CMS.SITE.DESC}")
    private String description;

    @XComment("{CMS.SITE.LOGO}")
    private String logo;

    @XComment(value = "{CMS.SITE.LOGO_SRC}", deprecated = true, forRemoval = "1.6.0")
    private String logoSrc;

    @XComment("{CMS.SITE.PATH}")
    private String path;

    @XComment("{CMS.SITE.RESOURCE_URL}")
    private String resourceUrl;

    @XComment("{CMS.SITE.DEPT_CODE}")
    private String deptCode;

    @XComment("{CC.ENTITY.SORT}")
    private Long sortFlag;

    @XComment("{CMS.SITE.SEO_KEYWORDS}")
    private String seoKeywords;

    @XComment("{CMS.SITE.SEO_DESC}")
    private String seoDescription;

    @XComment("{CMS.SITE.SEO_TITLE}")
    private String seoTitle;

    @XComment("{CMS.SITE.CONFIG_PROPS}")
    private Map<String, String> configProps;

    @XComment("{CMS.SITE.LINK}")
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
