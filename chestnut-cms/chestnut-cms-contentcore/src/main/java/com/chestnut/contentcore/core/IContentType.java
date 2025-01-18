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
package com.chestnut.contentcore.core;

import com.chestnut.contentcore.domain.BCmsContent;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.vo.ContentVO;

import java.io.InputStream;

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
	 */
	IContent<?> loadContent(CmsContent xContent);

    /**
     * 从输入流读取内容数据
     * 
     * @param is 输入流
     */
    IContent<?> readFrom(InputStream is);
    
    /**
     * 初始化内容编辑页面数据
     * 
     * @param catalogId 栏目ID
     * @param contentId 内容ID
     */
    ContentVO initEditor(Long catalogId, Long contentId);

    /**
     * 恢复内容扩展实体备份表数据
     * 
     * @param backupContent 备份内容
     */
	default void recover(BCmsContent backupContent) {
		
	}

	/**
	 * 删除内容扩展实体备份表数据
	 * 
	 * @param contentId 内容ID
	 */
	default void deleteBackups(Long contentId) {
		
	}
}
