package com.chestnut.advertisement.controller.front;

import java.time.LocalDateTime;

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
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/adv")
public class AdApiController extends BaseRestController {
	
	private final IAdvertisementStatService adStatService;
	
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
			// TODO: handle exception
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
			// TODO: handle exception
		}
	}
}
