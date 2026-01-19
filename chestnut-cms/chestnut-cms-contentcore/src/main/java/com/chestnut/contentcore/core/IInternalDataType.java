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
package com.chestnut.contentcore.core;

import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.util.TemplateUtils;
import freemarker.template.TemplateException;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * 内部数据类型
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IInternalDataType {

	/**
	 * Bean名称前缀
	 */
	String BEAN_NAME_PREFIX = "InternalDataType_";

	/**
	 * 获取内部数据预览地址
	 * 预览路径规则：cms/preview/{内部数据类型}/{数据id}?pp={发布通道编码}&pi={页码}
	 *
	 * @param type 内部数据类型
	 * @param id 数据ID
	 * @param publishPipeCode 发布通道编码
	 * @param pageIndex 页码
	 * @return 预览地址
	 */
	static String getPreviewPath(String type, Long id, String publishPipeCode, int pageIndex) {
		String path = "cms/preview/" + type + "/" + id + "?pp=" + publishPipeCode;
		if (pageIndex > 1) {
			path += "&pi=" + pageIndex;
		}
		return TemplateUtils.appendTokenParameter(path);
	}

	static String getPreviewPath(String type, Long id, String publishPipeCode) {
		return getPreviewPath(type, id, publishPipeCode, 1);
	}

	/**
	 * 获取内部数据动态浏览地址
	 * 动态路径规则：cms/view/{内部数据类型}/{数据id}?pp={发布通道编码}&pi={页码}
	 *
	 * @param type 内部数据类型
	 * @param id 数据ID
	 * @param publishPipeCode 发布通道编码
	 * @param pageIndex 页码
	 * @return 动态浏览地址
	 */
	static String getViewPath(String type, Long id, String publishPipeCode, int pageIndex) {
		String path = "cms/view/" + type + "/" + id + "?pp=" + publishPipeCode;
		if (pageIndex > 1) {
			path += "&pi=" + pageIndex;
		}
		return path;
	}

    /**
	 * 获取内部数据动态浏览地址
	 *
     * @param type 内部数据类型
     * @param id 数据ID
     * @param publishPipeCode 发布通道编码
	 * @return 动态浏览地址
	 */
	static String getViewPath(String type, Long id, String publishPipeCode) {
		return getViewPath(type, id, publishPipeCode, 1);
	}

	/**
	 * 类型ID
	 */
	String getId();

    /**
	 * 是否支持CmsSlot标签
	 */
    default boolean supportSlot() {
        return false;
    }

	/**
	 * 获取模板解析页面内容
	 */
	default String getPageData(RequestData requestData) throws IOException, TemplateException {
		return StringUtils.EMPTY;
	}

    /**
     * 处理模板页面内容，写入输出流
     * @param requestData 请求数据
     * @param writer 输出流
     * @throws TemplateException 模板异常
     * @throws IOException IO异常
     */
	default void processPageData(RequestData requestData, Writer writer) throws TemplateException, IOException {}

    /**
     * 静态文件路径，相对发布通道路径，不带域名
     */
    default String getStaticPath(Long dataId, String publishPipeCode) {
        return StringUtils.EMPTY;
    }

	/**
	 * 访问链接
	 *
	 * @param internalUrl 内部连接对象
	 * @param pageIndex 页码
	 * @param publishPipeCode 发布通道编码
	 * @param preview 是否预览模式
	 */
	default String getLink(InternalURL internalUrl, int pageIndex, String publishPipeCode, boolean preview) {
		return StringUtils.EMPTY;
	}

    default boolean isLinkData(Long id) {
		return false;
	}

    @Getter
	@Setter
	class RequestData {

		/**
		 * 数据ID
		 */
		Long dataId;

		/**
		 * 分页标识
		 */
		int pageIndex;

		/**
		 * 发布通道编码
		 */
		String publishPipeCode;

		/**
		 * 是否预览模式
		 */
		boolean preview;

		/**
		 * 其他扩展参数
		 */
		Map<String, String> params;

		public RequestData(Long dataId, Integer pageIndex, String publishPipeCode, boolean preview, Map<String, String> params) {
			this.dataId = dataId;
			this.pageIndex = pageIndex;
			this.publishPipeCode = publishPipeCode;
			this.preview = preview;
			this.params = params;
		}
	}
}
