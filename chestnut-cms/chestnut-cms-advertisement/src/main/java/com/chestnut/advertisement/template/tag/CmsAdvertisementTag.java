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
package com.chestnut.advertisement.template.tag;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.advertisement.domain.CmsAdvertisement;
import com.chestnut.advertisement.pojo.vo.AdvertisementVO;
import com.chestnut.advertisement.service.IAdvertisementService;
import com.chestnut.common.staticize.FreeMarkerUtils;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.staticize.enums.TagAttrDataType;
import com.chestnut.common.staticize.tag.AbstractListTag;
import com.chestnut.common.staticize.tag.TagAttr;
import com.chestnut.common.staticize.tag.TagAttrOption;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsPageWidget;
import com.chestnut.contentcore.service.IPageWidgetService;
import com.chestnut.contentcore.util.TemplateUtils;
import com.chestnut.system.fixed.dict.EnableOrDisable;
import freemarker.core.Environment;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class CmsAdvertisementTag extends AbstractListTag {

	public final static String TAG_NAME = "cms_advertisement";
	public final static String NAME = "{FREEMARKER.TAG." + TAG_NAME + ".NAME}";
	public final static String DESC = "{FREEMARKER.TAG." + TAG_NAME + ".DESC}";
	public final static String ATTR_USAGE_CODE = "{FREEMARKER.TAG." + TAG_NAME + ".code}";
	public final static String ATTR_USAGE_TYPE = "{FREEMARKER.TAG." + TAG_NAME + ".type}";
	public final static String ATTR_OPTION_TYPE_NONE = "{FREEMARKER.TAG." + TAG_NAME + ".type.None}";
	public final static String ATTR_OPTION_TYPE_STAT = "{FREEMARKER.TAG." + TAG_NAME + ".type.Stat}";

	final static String ATTR_CODE = "code";
	final static String ATTR_TYPE = "type";

	private final IAdvertisementService advertisementService;

	private final IPageWidgetService pageWidgetService;

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = super.getTagAttrs();
		tagAttrs.add(new TagAttr(ATTR_CODE, true, TagAttrDataType.STRING, ATTR_USAGE_CODE));
		tagAttrs.add(new TagAttr(ATTR_TYPE, false, TagAttrDataType.STRING, ATTR_USAGE_TYPE,
				RedirectType.toTagAttrOptions(), RedirectType.None.name()));
		return tagAttrs;
	}

	@Override
	public TagPageData prepareData(Environment env, Map<String, String> attrs, boolean page, int size, int pageIndex) throws TemplateException {
		String code = MapUtils.getString(attrs, ATTR_CODE);
		String redirectType = MapUtils.getString(attrs, ATTR_TYPE, RedirectType.None.name());

		Long siteId = TemplateUtils.evalSiteId(env);
		CmsPageWidget adSpace = this.pageWidgetService.getOne(new LambdaQueryWrapper<CmsPageWidget>()
				.eq(CmsPageWidget::getSiteId, siteId)
				.eq(CmsPageWidget::getCode, code));
		if (adSpace == null) {
			throw new TemplateException(StringUtils.messageFormat("Advertising space `{0}` not exists.", code), env)  ;
		}
		String condition = MapUtils.getString(attrs, TagAttr.AttrName_Condition);

		LambdaQueryWrapper<CmsAdvertisement> q = new LambdaQueryWrapper<CmsAdvertisement>()
				.eq(CmsAdvertisement::getAdSpaceId, adSpace.getPageWidgetId())
				.eq(CmsAdvertisement::getState, EnableOrDisable.ENABLE);
		q.apply(StringUtils.isNotEmpty(condition), condition);
		Page<CmsAdvertisement> pageResult = this.advertisementService.page(new Page<>(pageIndex, size, page), q);
		TemplateContext context = FreeMarkerUtils.getTemplateContext(env);
		List<AdvertisementVO> list = pageResult.getRecords().stream().map(ad ->{
			AdvertisementVO vo = new AdvertisementVO(ad);
			if (RedirectType.isStat(redirectType)) {
				vo.setLink(this.advertisementService.getAdvertisementStatLink(ad, context.getPublishPipeCode()));
			} else {
				vo.setLink(vo.getRedirectUrl());
			}
			return vo;
		}).toList();
		return TagPageData.of(list, pageResult.getTotal());
	}

	@Override
	public Class<AdvertisementVO> getDataClass() {
		return AdvertisementVO.class;
	}

	@Override
	public String getTagName() {
		return TAG_NAME;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getDescription() {
		return DESC;
	}

	private enum RedirectType {
		None(ATTR_OPTION_TYPE_NONE),
		Stat(ATTR_OPTION_TYPE_STAT);

		private final String desc;

		RedirectType(String desc) {
			this.desc = desc;
		}

		static boolean isStat(String level) {
			return Stat.name().equalsIgnoreCase(level);
		}

		static List<TagAttrOption> toTagAttrOptions() {
			return List.of(
					new TagAttrOption(None.name(), None.desc),
					new TagAttrOption(Stat.name(), Stat.desc)
			);
		}
	}
}
