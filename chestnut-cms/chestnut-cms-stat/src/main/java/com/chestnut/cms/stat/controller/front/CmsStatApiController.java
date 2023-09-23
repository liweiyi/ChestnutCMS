package com.chestnut.cms.stat.controller.front;

import com.chestnut.cms.stat.core.CmsStat;
import com.chestnut.cms.stat.domain.CmsSiteVisitLog;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.system.security.AdminUserType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 统计数据
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequiredArgsConstructor
public class CmsStatApiController extends BaseRestController {

	private final List<CmsStat> statList;

	/**
	 * 网站访问统计
	 *
	 * @param siteId
	 * @param catalogId
	 * @param contentId
	 * @return
	 */
	@GetMapping("/api/stat/visit")
	public void visitSite(
			@RequestParam("sid") Long siteId,
			@RequestParam(value = "cid", required = false, defaultValue = "0") Long catalogId,
			@RequestParam(value = "id", required = false, defaultValue = "0") Long contentId) {
		try {
			CmsSiteVisitLog log = new CmsSiteVisitLog();
			log.fill(ServletUtils.getRequest());
			log.setSiteId(siteId);
			log.setCatalogId(catalogId);
			log.setContentId(contentId);
			log.setEvtTime(LocalDateTime.now());

			this.statList.forEach(stat -> stat.deal(log));
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
