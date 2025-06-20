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

import com.chestnut.contentcore.domain.CmsPageWidget;
import com.chestnut.contentcore.domain.pojo.PublishPipeTemplate;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.List;

@Getter
@Setter
public class PageWidgetVO {

	/*
	 * 页面部件ID
	 */
    private Long pageWidgetId;

    /*
     * 所属栏目ID
     */
    private Long catalogId;

    /**
     * 栏目名称
     */
    private String catalogName;

    /*
     * 栏目类型
     */
    private String type;

    /*
     * 名称
     */
    private String name;

    /*
     * 编码
     */
    private String code;

    /*
     * 状态
     */
    private String state;

    /*
     * 发布通道编码
     */
    @Deprecated(since = "1.5.6", forRemoval = true)
    private String publishPipeCode;

    /*
     * 模板路径
     */
    @Deprecated(since = "1.5.6", forRemoval = true)
    private String template;

    /*
     * 发布通道模板配置
     */
    private List<PublishPipeTemplate> templates;

    /*
     * 静态化目录
     */
    private String path;
   
    /*
     * 编辑页面路由地址
     */
    private String route;
	
	public static PageWidgetVO newInstance(CmsPageWidget cmsPageWidget) {
		PageWidgetVO vo = new PageWidgetVO();
		BeanUtils.copyProperties(cmsPageWidget, vo);
		return vo;
	}
}
