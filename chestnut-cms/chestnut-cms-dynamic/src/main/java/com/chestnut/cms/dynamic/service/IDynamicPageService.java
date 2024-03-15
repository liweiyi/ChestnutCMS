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
package com.chestnut.cms.dynamic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.cms.dynamic.domain.CmsDynamicPage;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IDynamicPageService extends IService<CmsDynamicPage> {

	void addDynamicPage(CmsDynamicPage dynamicPage);

	void saveDynamicPage(CmsDynamicPage dynamicPage);

	void deleteDynamicPage(List<Long> dynamicPageIds);

    void generateDynamicPage(String uri, Long siteId, String publishPipeCode, Boolean preview, Map<String, String> parameters, HttpServletResponse response) throws IOException;
}
