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
package com.chestnut.contentcore.util;

import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.*;
import com.chestnut.contentcore.ContentCoreConsts;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.exception.ContentCoreErrorCode;
import com.chestnut.contentcore.perms.SitePermissionType;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.system.security.StpAdminUtil;

/**
 * CmsRestController
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class CmsRestController extends BaseRestController {

    private static final ISiteService siteService = SpringUtils.getBean(ISiteService.class);

    public CmsSite getCurrentSite() {
        Long siteId = ConvertUtils.toLong(ServletUtils.getHeader(ServletUtils.getRequest(), ContentCoreConsts.Header_CurrentSite));
        if (!IdUtils.validate(siteId)) {
            throw ContentCoreErrorCode.MISSING_CURRENT_SITE_ID.exception();
        }
        LoginUser loginUser = StpAdminUtil.getLoginUser();
        boolean hasPriv = loginUser.hasPermission(SitePermissionType.SitePrivItem.View.getPermissionKey(siteId));
        if (!hasPriv) {
            throw ContentCoreErrorCode.NO_CURRENT_SITE_PRIV.exception(siteId);
        }
        CmsSite site = siteService.getSite(siteId);
        Assert.notNull(site, ContentCoreErrorCode.NO_SITE::exception);
        return site;
    }
}
