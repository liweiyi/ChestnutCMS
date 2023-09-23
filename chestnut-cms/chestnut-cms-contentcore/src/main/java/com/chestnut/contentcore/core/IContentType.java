package com.chestnut.contentcore.core;

import java.io.IOException;

import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.vo.ContentVO;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 内容类型
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IContentType extends Comparable<IContentType> {
	
	/**
	 * Bean名称前缀
	 */
	String BEAN_NAME_PREFIX = "ContentType_";

	/**
	 * 内容类型唯一标识ID
	 */
    String getId();

    /**
     * 内容类型名称
     */
    String getName();

    /**
     * 图标
     */
    default String getIcon() {
        return null;
    }

    /**
     * 内容扩展详情编辑组件
     */
    String getComponent();

    /**
     * 显示顺序
     */
    default int getOrder() {
        return 0;
    }

    @Override
    default int compareTo(IContentType o) {
        return this.getOrder() - o.getOrder();
    }

	IContent<?> newContent();

	/**
	 * 加载内容数据，根据cmsContent.contentId拉取内容扩展表数据
	 * 
	 * @param xContent
	 * @return
	 */
	IContent<?> loadContent(CmsContent xContent);

    /**
     * 从请求读取内容数据
     * 
     * @param request
     * @return
     * @throws IOException
     */
    IContent<?> readRequest(HttpServletRequest request) throws IOException;
    
    /**
     * 初始化内容编辑页面数据
     * 
     * @param catalogId
     * @param contentId
     * @return
     */
    ContentVO initEditor(Long catalogId, Long contentId);

    /**
     * 恢复内容扩展实体备份表数据
     * 
     * @param contentId
     */
	default void recover(Long contentId) {
		
	}

	/**
	 * 删除内容扩展实体备份表数据
	 * 
	 * @param contentId
	 */
	default void deleteBackups(Long contentId) {
		
	}
}
