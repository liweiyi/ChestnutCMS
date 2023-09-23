package com.chestnut.contentcore.controller;

import com.chestnut.common.domain.R;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.vo.SiteStatVO;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.service.ISiteStatService;
import com.chestnut.system.security.AdminUserType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 站点相关资源统计数据控制器
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Priv(type = AdminUserType.TYPE)
@RestController
@RequiredArgsConstructor
@RequestMapping("/cms/dashboard")
public class SiteStatController extends BaseRestController {

	private final ISiteService siteService;
	
	private final ISiteStatService siteStatService;
	
	@GetMapping("/stat")
	public R<?> getSiteStat() {
		CmsSite site = siteService.getCurrentSite(ServletUtils.getRequest());
		SiteStatVO siteStat = siteStatService.getSiteStat(site);
		return R.ok(siteStat);
	}
}
