package com.chestnut.seo.controller;

import com.chestnut.common.async.AsyncTask;
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.Assert;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.seo.service.BaiduSitemapService;
import com.chestnut.system.security.AdminUserType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 站点地图前端控制器
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequestMapping("/cms/seo")
@RequiredArgsConstructor
public class SitemapController extends BaseRestController {

	private final ISiteService siteService;

	private final BaiduSitemapService sitemapService;

	@Priv(type = AdminUserType.TYPE)
	@PostMapping("/sitemap")
	public R<?> generateSitemap(@RequestParam Long siteId) {
		CmsSite site = siteService.getSite(siteId);
		Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", siteId));

		AsyncTask asyncTask = this.sitemapService.asyncGenerateSitemapXml(site);
		return R.ok(asyncTask.getTaskId());
	}
}
