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
package com.chestnut.article.controller;


import com.chestnut.article.PublishPipeProp_UEditorCss;
import com.chestnut.common.domain.R;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.config.CMSConfig;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.IPublishPipeService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.system.security.AdminUserType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *  文章前端控制器
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Priv(type = AdminUserType.TYPE)
@RestController
@RequiredArgsConstructor
@RequestMapping("/cms/article")
public class ArticleController extends BaseRestController {

    private final ISiteService siteService;

    private final ICatalogService catalogService;

    private final IPublishPipeService publishPipeService;

    @GetMapping("/ueditor_css")
    public R<?> getUEditorCss(@RequestParam Long catalogId) {
        CmsCatalog catalog = catalogService.getCatalog(catalogId);
        CmsSite site = siteService.getSite(catalog.getSiteId());

        Map<String, String> data = new HashMap<>();
        publishPipeService.getPublishPipes(site.getSiteId()).forEach(pp -> {
            String value = PublishPipeProp_UEditorCss.getValue(pp.getCode(),
                    catalog.getPublishPipeProps(), site.getPublishPipeProps());
            if (StringUtils.isNotEmpty(value)) {
                value = CMSConfig.getResourcePreviewPrefix() + value;
            }
            data.put(pp.getCode(), value);
        });
        return R.ok(data);
    }
}

