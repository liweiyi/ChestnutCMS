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
package com.chestnut.cms.search.controller.front;

import com.chestnut.cms.search.impl.SearchDynamicPageType;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.contentcore.service.impl.DynamicPageService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

/**
 * 搜索动态页面
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class CmsSearchController extends BaseRestController {

    private final DynamicPageService dynamicPageService;

    @GetMapping(SearchDynamicPageType.REQUEST_PATH)
    public void accountCentre(@RequestParam(value ="q", required = false, defaultValue = "") @Length(max = 50) String query,
                              @RequestParam("sid") Long siteId,
                              @RequestParam("pp") String publishPipeCode,
                              @RequestParam(value = "ot", required = false ,defaultValue = "false") Boolean onlyTitle,
                              @RequestParam(value = "ct", required = false) String contentType,
                              @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                              @RequestParam(required = false, defaultValue = "false") Boolean preview,
                              HttpServletResponse response)
            throws IOException {

        Map<String, String> parameters = ServletUtils.getParameters();

        this.dynamicPageService.generateDynamicPage(SearchDynamicPageType.TYPE,
                siteId, publishPipeCode, preview, parameters, response);
    }
}
