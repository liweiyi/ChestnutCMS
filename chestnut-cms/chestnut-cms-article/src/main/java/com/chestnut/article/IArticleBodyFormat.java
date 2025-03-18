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
package com.chestnut.article;

/**
 * 文正正文文档格式
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IArticleBodyFormat {

    String BEAN_PREFIX = "ArticleBodyFormat_";

    /**
     * ID
     */
    String getId();

    /**
     * 名称
     */
    String getName();

    /**
     * 编辑器内容初始化处理
     */
    default String initEditor(String contentHtml) {
        return contentHtml;
    }

    /**
     * 文章正文内容发布预览处理
     */
    String deal(String contentHtml, String publishPipeCode, boolean isPreview);
}
