/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
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

import com.chestnut.common.security.domain.BaseDTO;
import com.chestnut.system.fixed.dict.YesOrNo;
import com.chestnut.system.validator.Dict;
import com.chestnut.system.validator.LongId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class CatalogUpdateDTO extends BaseDTO {

	/*
	 * 栏目ID
	 */
	@LongId
    private Long catalogId;

    /*
     * 栏目名称 
     */
    @NotBlank
    private String name;

    /*
     * 栏目logo 
     */
    private String logo;

    /**
     * 栏目别名
     */
    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "栏目别名只能使用大小写字母、数字、下划线组合")
    private String alias;

    /**
     * 栏目目录
     */
    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9_\\/]+$", message = "栏目路径只能使用大小写字母、数字、下划线组合")
    private String path;

    /*
     * 栏目描述
     */
    private String description;

    /*
     * 是否静态化
     */
    @NotEmpty
    @Dict(YesOrNo.TYPE)
    private String staticFlag;

    /*
     * 栏目是否在标签中忽略
     */
    private String tagIgnore;

    /*
     * 栏目类型
     */
    @NotBlank
    private String catalogType;
    
    /*
     * 标题栏目跳转地址
     */
    private String redirectUrl;

    /*
     * SEO关键词
     */
    private String seoKeywords;

    /*
     * SEO描述
     */
    private String seoDescription;

    /*
     * SEO标题
     */
    private String seoTitle;

    /*
     * 栏目发布通道数据
     */
    private List<PublishPipeProp> publishPipeDatas;
    
    /*
     * 自定义参数
     */
    private Map<String, Object> params;
}
