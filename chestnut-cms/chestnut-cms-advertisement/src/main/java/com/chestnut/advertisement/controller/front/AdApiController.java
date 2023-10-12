package com.chestnut.advertisement.controller.front;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import com.chestnut.common.utils.StringUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chestnut.advertisement.domain.CmsAdClickLog;
import com.chestnut.advertisement.domain.CmsAdViewLog;
import com.chestnut.advertisement.service.IAdvertisementStatService;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.ServletUtils;

import lombok.RequiredArgsConstructor;

/**
 * 广告统计数据收集
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/adv")
public class AdApiController extends BaseRestController {

	private final IAdvertisementStatService adStatService;

	@GetMapping("/redirect")
	public void statAndRedirect(@RequestParam("sid") Long siteId,
								@RequestParam("aid") Long advertisementId,
								@RequestParam("url") String redirectUrl,
								HttpServletResponse response) throws IOException {
		this.adClick(siteId, advertisementId);
		response.sendRedirect(URLDecoder.decode(redirectUrl, StandardCharsets.UTF_8));
	}

	@GetMapping("/click")
	public void adClick(@RequestParam("sid") Long siteId, @RequestParam("aid") Long advertisementId) {
		try {
			CmsAdClickLog log = new CmsAdClickLog();
			log.fill(ServletUtils.getRequest());
			log.setSiteId(siteId);
			log.setAdId(advertisementId);
			log.setEvtTime(LocalDateTime.now());
			this.adStatService.adClick(log);
		} catch (Exception e) {
			log.error("Advertisement click stat failed: " + advertisementId, e);
		}
	}

	@GetMapping("/view")
	public void adView(@RequestParam("sid") Long siteId, @RequestParam("aid") Long advertisementId) {
		try {
			CmsAdViewLog log = new CmsAdViewLog();
			log.fill(ServletUtils.getRequest());
			log.setSiteId(siteId);
			log.setAdId(advertisementId);
			log.setEvtTime(LocalDateTime.now());
			this.adStatService.adView(log);
		} catch (Exception e) {
			log.error("Advertisement view stat failed: " + advertisementId, e);
		}
	}
}
