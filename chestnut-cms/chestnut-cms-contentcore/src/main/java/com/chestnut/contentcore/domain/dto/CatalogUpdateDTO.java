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
package com.chestnut.contentcore.domain.dto;

import com.chestnut.common.annotation.XComment;
import com.chestnut.common.security.domain.BaseDTO;
import com.chestnut.common.validation.RegexConsts;
import com.chestnut.contentcore.domain.pojo.PublishPipeProps;
import com.chestnut.system.fixed.dict.YesOrNo;
import com.chestnut.system.validator.Dict;
import com.chestnut.system.validator.LongId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@XComment("{API.DOC.CMS.CATALOG.UPDATE_DTO}")
@Getter
@Setter
public class CatalogUpdateDTO extends BaseDTO {

	/*
	 * 栏目ID
	 */
	@XComment("{CMS.CATALOG.ID}")
	@LongId
    private Long catalogId;

    /*
     * 栏目名称 
     */
	@XComment("{CMS.CATALOG.NAME}")
    @NotBlank
    private String name;

    /*
     * 栏目logo 
     */
	@XComment("{CMS.CATALOG.LOGO}")
    private String logo;

    /**
     * 栏目别名
     */
	@XComment("{CMS.CATALOG.ALIAS}")
    @NotBlank
    @Pattern(regexp = RegexConsts.REGEX_CODE, message = "栏目别名只能使用大小写字母、数字、下划线组合")
    private String alias;

    /**
     * 栏目目录
     */
	@XComment("{CMS.CATALOG.PATH}")
    @NotBlank
    @Pattern(regexp = RegexConsts.REGEX_PATH, message = "栏目路径只能使用大小写字母、数字、下划线组合")
    private String path;

    /*
     * 栏目描述
     */
	@XComment("{CMS.CATALOG.DESC}")
    private String description;

    /*
     * 是否静态化
     */
	@XComment("{API.DOC.CMS.CATALOG.STATIC_FLAG}")
    @NotEmpty
    @Dict(YesOrNo.TYPE)
    private String staticFlag;

    /*
     * 栏目是否在标签中忽略
     */
	@XComment("{API.DOC.CMS.CATALOG.TAG_IGNORE}")
    private String tagIgnore;

    /*
     * 栏目类型
     */
	@XComment("{CMS.CATALOG.TYPE}")
    @NotBlank
    private String catalogType;
    
    /*
     * 标题栏目跳转地址
     */
	@XComment("{CMS.CATALOG.REDIRECT_URL}")
    private String redirectUrl;

    /*
     * 内容路径规则
     */
	@XComment("{API.DOC.CMS.CATALOG.DETAIL_NAME_RULE}")
    private String detailNameRule;

    /*
     * SEO关键词
     */
	@XComment("{CMS.CATALOG.SEO_KEYWORDS}")
    private String seoKeywords;

    /*
     * SEO描述
     */
	@XComment("{CMS.CATALOG.SEO_DESC}")
    private String seoDescription;

    /*
     * SEO标题
     */
	@XComment("{CMS.CATALOG.SEO_TITLE}")
    private String seoTitle;

    /*
     * 栏目发布通道数据
     */
	@XComment("{API.DOC.CMS.SITE.PUBLISH_PIPE_DATAS}")
    private List<PublishPipeProps> publishPipeDatas;
    
    /*
     * 自定义参数
     */
    private Map<String, Object> params;
}
