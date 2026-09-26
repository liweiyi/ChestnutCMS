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
package com.chestnut.seo.controller;

import com.chestnut.common.annotation.XComment;
import com.chestnut.common.domain.R;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.perms.CatalogPermissionType;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.contentcore.util.CmsRestController;
import com.chestnut.seo.service.BaiduPushService;
import com.chestnut.system.permission.PermissionUtils;
import com.chestnut.system.security.AdminUserType;


import com.chestnut.system.security.StpAdminUtil;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 百度收录推送前端控制器
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@XComment("{API.DOC.CMS.SEO.SITE_PUSH_MODULE}")
@RestController
@RequestMapping("/cms/seo")
@RequiredArgsConstructor
public class SitePushController extends CmsRestController {

	private final BaiduPushService baiduPushService;

	private final IContentService contentService;

	@XComment("{API.DOC.CMS.SEO.SITE_PUSH.BAIDU_PUSH}")
	@Priv(type = AdminUserType.TYPE)
	@PostMapping("/baidu_push")
	public R<List<BaiduPushService.BaiduPushResult>> generateSitemap(@RequestBody @NotEmpty @XComment("{API.DOC.CMS.SEO.SITE_PUSH.CONTENT_IDS}") List<Long> contentIds) {
		CmsContent content = contentService.dao().getById(contentIds.get(0));
		LoginUser loginUser = StpAdminUtil.getLoginUser();
		PermissionUtils.checkPermission(CatalogPermissionType.CatalogPrivItem.EditContent.getPermissionKey(content.getCatalogId()), loginUser);

		CmsSite site = getCurrentSite();
		List<BaiduPushService.BaiduPushResult> results = baiduPushService.pushContents(site, contentIds);
		return R.ok(results);
	}
}
