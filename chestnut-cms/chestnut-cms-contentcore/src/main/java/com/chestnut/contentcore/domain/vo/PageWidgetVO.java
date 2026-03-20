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
    @XComment("{CMS.PAGE_WIDGET.ID}")
    private Long pageWidgetId;

    /*
     * 所属栏目ID
     */
    @XComment("{CMS.PAGE_WIDGET.CATALOG_ID}")
    private Long catalogId;

    /**
     * 栏目名称
     */
    @XComment("{CMS.PAGE_WIDGET.CATALOG_NAME}")
    private String catalogName;

    /*
     * 栏目类型
     */
    @XComment("{CMS.PAGE_WIDGET.TYPE}")
    private String type;

    /*
     * 名称
     */
    @XComment("{CMS.PAGE_WIDGET.NAME}")
    private String name;

    /*
     * 编码
     */
    @XComment("{CMS.PAGE_WIDGET.CODE}")
    private String code;

    /*
     * 状态
     */
    @XComment("{CMS.PAGE_WIDGET.STATE}")
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
    @XComment("{CMS.PAGE_WIDGET.PUBLISH_PIPE_TEMPLATES}")
    private List<PublishPipeTemplate> templates;

    /*
     * 静态化目录
     */
    @XComment("{CMS.PAGE_WIDGET.PATH}")
    private String path;
   
    /*
     * 编辑页面路由地址
     */
    @XComment("{CMS.PAGE_WIDGET.ROUTE}")
    private String route;

    /*
     * 备注
     */
    @XComment("{CC.ENTITY.REMARK}")
    private String remark;
	
	public static PageWidgetVO newInstance(CmsPageWidget cmsPageWidget) {
		PageWidgetVO vo = new PageWidgetVO();
		BeanUtils.copyProperties(cmsPageWidget, vo);
		return vo;
	}
}
