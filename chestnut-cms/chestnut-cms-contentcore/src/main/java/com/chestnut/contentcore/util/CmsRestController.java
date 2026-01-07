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
