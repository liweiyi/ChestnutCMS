package com.chestnut.cms.stat.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.cms.stat.domain.CmsCatalogContentStat;
import com.chestnut.cms.stat.domain.CmsSiteVisitLog;
import com.chestnut.cms.stat.domain.CmsUserContentStat;
import com.chestnut.cms.stat.domain.vo.ContentDynamicStatDataVO;
import com.chestnut.cms.stat.domain.vo.ContentStatByCatalogVO;
import com.chestnut.cms.stat.mapper.CmsCatalogContentStatMapper;
import com.chestnut.cms.stat.mapper.CmsSiteVisitLogMapper;
import com.chestnut.cms.stat.mapper.CmsUserContentStatMapper;
import com.chestnut.common.domain.R;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.system.security.AdminUserType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 统计数据
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Priv(type = AdminUserType.TYPE)
@RestController
@RequiredArgsConstructor
@RequestMapping("/cms/stat")
public class CmsStatController extends BaseRestController {

	private final ISiteService siteService;

	private final ICatalogService catalogService;

	private final IContentService contentService;

	private final CmsSiteVisitLogMapper siteVisitLogMapper;

	private final CmsCatalogContentStatMapper contentStatByCatalogMapper;

	private final CmsUserContentStatMapper contentStatByUserMapper;

	@GetMapping
	public R<?> getSiteVisitLogList() {
		PageRequest pr = this.getPageRequest();
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		Page<CmsSiteVisitLog> page = new LambdaQueryChainWrapper<CmsSiteVisitLog>(this.siteVisitLogMapper)
				.eq(CmsSiteVisitLog::getSiteId, site.getSiteId()).orderByDesc(CmsSiteVisitLog::getEvtTime)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

	@GetMapping("/contentDynamicData")
	public R<?> getContentDynamicData(@RequestParam(name = "query", required = false, defaultValue = "") String title) {
		PageRequest pr = getPageRequest();
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());

		LambdaQueryChainWrapper<CmsContent> q = this.contentService.lambdaQuery()
				.eq(CmsContent::getSiteId, site.getSiteId())
				.like(StringUtils.isNotEmpty(title), CmsContent::getTitle, title);
		if (!pr.getSorts().isEmpty()) {
			pr.getSorts().forEach(order -> {
				SFunction<CmsContent, ?> sfunc = CmsContent.getSFunction(order.getColumn());
				if (Objects.nonNull(sfunc)) {
					q.orderBy(true, order.getDirection() == Sort.Direction.ASC, sfunc);
				}
			});
		} else {
			q.orderByDesc(CmsContent::getViewCount);
		}
		Page<CmsContent> page = q.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		List<ContentDynamicStatDataVO> list = page.getRecords().stream().map(ContentDynamicStatDataVO::newInstance).toList();
		return this.bindDataTable(list, page.getTotal());
	}

	@GetMapping("/contentStatByCatalog")
	public R<?> getContentStatByCatalog() {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		List<CmsCatalogContentStat> stats = this.contentStatByCatalogMapper
				.selectList(new LambdaQueryWrapper<CmsCatalogContentStat>().eq(CmsCatalogContentStat::getSiteId, site.getSiteId()));
		Map<Long, CmsCatalogContentStat> dataMap = stats.stream().collect(Collectors.toMap(CmsCatalogContentStat::getCatalogId, stat -> stat));
		List<CmsCatalog> catalogs = this.catalogService.lambdaQuery().eq(CmsCatalog::getSiteId, site.getSiteId()).list();
		List<ContentStatByCatalogVO> list = catalogs.stream().map(catalog ->
					ContentStatByCatalogVO.newInstance(catalog, dataMap.get(catalog.getCatalogId()))
				).toList();
		return R.ok(list);
	}

	@GetMapping("/contentStatByUser")
	public R<?> getContentStatByUser() {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		List<CmsUserContentStat> list = contentStatByUserMapper
				.selectList(new LambdaQueryWrapper<CmsUserContentStat>().eq(CmsUserContentStat::getSiteId, site.getSiteId()));
		return R.ok(list);
	}
}
