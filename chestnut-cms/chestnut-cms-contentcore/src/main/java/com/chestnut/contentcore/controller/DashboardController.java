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
package com.chestnut.contentcore.controller;

import com.chestnut.common.domain.R;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.contentcore.config.properties.CMSProperties;
import com.chestnut.contentcore.domain.vo.CmsConfigurationDashboardVO;
import com.chestnut.contentcore.publish.IPublishStrategy;
import com.chestnut.system.security.AdminUserType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页看板数据
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Priv(type = AdminUserType.TYPE)
@RestController
@RequiredArgsConstructor
@RequestMapping("/cms/dashboard/")
public class DashboardController extends BaseRestController {

	private final IPublishStrategy publishStrategy;

	private final CMSProperties properties;

	@GetMapping("/config")
	public R<?> getCmsConfiguration() {
		CmsConfigurationDashboardVO vo = CmsConfigurationDashboardVO.builder()
				.publishStrategy(publishStrategy.getId())
				.resourceRoot(properties.getResourceRoot())
				.build();
		return R.ok(vo);
	}
}
