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
package com.chestnut.article.format;

import com.chestnut.article.ArticleUtils;
import com.chestnut.article.IArticleBodyFormat;
import com.chestnut.contentcore.util.InternalUrlUtils;
import org.springframework.stereotype.Component;

/**
 * 文章正文文档格式：富文本
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IArticleBodyFormat.BEAN_PREFIX + ArticleBodyFormat_RichText.ID)
public class ArticleBodyFormat_RichText implements IArticleBodyFormat {

    public static final String ID = "RichText";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "{ArticleBodyFormat.RichText}";
    }

    @Override
    public String initEditor(String contentHtml) {
        return InternalUrlUtils.dealResourceInternalUrl(contentHtml);
    }

    @Override
    public String deal(String contentHtml, String publishPipeCode, boolean isPreview) {
        // 处理内容扩展模板占位符
        contentHtml = ArticleUtils.dealContentEx(contentHtml, publishPipeCode, isPreview);
        // 处理正文内部链接
        contentHtml = InternalUrlUtils.dealInternalUrl(contentHtml, publishPipeCode, isPreview);
        return contentHtml;
    }
}
