package com.chestnut.contentcore.domain.vo;

import org.springframework.beans.BeanUtils;

import com.chestnut.contentcore.domain.CmsPageWidget;

import lombok.Getter;
import lombok.Setter;

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
    private String publishPipeCode;

    /*
     * 模板路径
     */
    private String template;

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
