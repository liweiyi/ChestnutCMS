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
package com.chestnut.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.article.domain.CmsArticleDetail;
import com.chestnut.contentcore.domain.CmsSite;

/**
 * 文章服务类
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IArticleService extends IService<CmsArticleDetail> {

	/**
	 * 保存内部链接
	 * 
	 * 查找内容中所有带iurl属性的标签，如果标签内含有src/href属性则替换成iurl地址，并移除标签iurl属性
	 * 
	 * @param content
	 * @return
	 */
	String saveInternalUrl(String content);

	/**
	 * 下载远程图片
	 * 
	 * @param content
	 * @param site
	 * @param operator
	 * @return
	 */
	String downloadRemoteImages(String content, CmsSite site, String operator);
}
