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
package com.chestnut.article.controller.front;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.article.domain.CmsArticleDetail;
import com.chestnut.article.domain.vo.ArticleApiVO;
import com.chestnut.article.service.IArticleService;
import com.chestnut.common.domain.R;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.fixed.dict.ContentAttribute;
import com.chestnut.contentcore.fixed.dict.ContentStatus;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.contentcore.template.tag.CmsCatalogTag;
import com.chestnut.contentcore.template.tag.CmsContentTag;
import com.chestnut.contentcore.util.CatalogUtils;
import com.chestnut.contentcore.util.InternalUrlUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 内容数据API接口
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cms/article")
public class ArticleApiController extends BaseRestController {

    private final ICatalogService catalogService;

    private final IContentService contentService;

    private final IArticleService articleService;

    @GetMapping("/list")
    public R<List<ArticleApiVO>> getContentList(
            @RequestParam("sid") Long siteId,
            @RequestParam(value = "cid", required = false, defaultValue = "0") Long catalogId,
            @RequestParam(value = "lv", required = false, defaultValue = "Root") String level,
            @RequestParam(value = "attrs", required = false) String hasAttributes,
            @RequestParam(value = "no_attrs", required = false) String noAttributes,
            @RequestParam(value = "st", required = false, defaultValue = "Recent") String sortType,
            @RequestParam(value = "ps", required = false, defaultValue = "16") Integer pageSize,
            @RequestParam(value = "pn", required = false, defaultValue = "1") Long pageNumber,
            @RequestParam(value = "pp") String publishPipeCode,
            @RequestParam(value = "preview", required = false, defaultValue = "false") Boolean preview,
            @RequestParam(value = "text", required = false, defaultValue = "false") Boolean text
    ) {
        if (!CmsCatalogTag.CatalogTagLevel.isRoot(level) && !IdUtils.validate(catalogId)) {
            return R.fail("The parameter cid is required where lv is `Root`.");
        }
        LambdaQueryWrapper<CmsContent> q = new LambdaQueryWrapper<>();
        q.eq(CmsContent::getSiteId, siteId).eq(CmsContent::getStatus, ContentStatus.PUBLISHED);
        if (!CmsCatalogTag.CatalogTagLevel.isRoot(level)) {
            CmsCatalog catalog = this.catalogService.getCatalog(catalogId);
            if (Objects.isNull(catalog)) {
                return R.fail("Catalog not found: " + catalogId);
            }
            if (CmsCatalogTag.CatalogTagLevel.isCurrent(level)) {
                q.eq(CmsContent::getCatalogId, catalog.getCatalogId());
            } else if (CmsCatalogTag.CatalogTagLevel.isChild(level)) {
                q.likeRight(CmsContent::getCatalogAncestors, catalog.getAncestors() + CatalogUtils.ANCESTORS_SPLITER);
            } else if (CmsCatalogTag.CatalogTagLevel.isCurrentAndChild(level)) {
                q.likeRight(CmsContent::getCatalogAncestors, catalog.getAncestors());
            }
        }
        if (StringUtils.isNotEmpty(hasAttributes)) {
            int attrTotal = ContentAttribute.convertInt(hasAttributes.split(","));
            q.apply(attrTotal > 0, "attributes&{0}={1}", attrTotal, attrTotal);
        }
        if (StringUtils.isNotEmpty(noAttributes)) {
            String[] contentAttrs = noAttributes.split(",");
            int attrTotal = ContentAttribute.convertInt(contentAttrs);
            for (String attr : contentAttrs) {
                int bit = ContentAttribute.bit(attr);
                q.apply(bit > 0, "attributes&{0}<>{1}", attrTotal, bit);
            }
        }
        if (CmsContentTag.SortTagAttr.isRecent(sortType)) {
            q.orderByDesc(CmsContent::getPublishDate);
        } else {
            q.orderByDesc(Arrays.asList(CmsContent::getTopFlag, CmsContent::getSortFlag));
        }

        Page<CmsContent> pageResult = this.contentService.dao().page(new Page<>(pageNumber, pageSize, false), q);
        if (pageResult.getRecords().isEmpty()) {
            return R.ok(List.of());
        }
        List<Long> contentIds = pageResult.getRecords().stream().map(CmsContent::getContentId).toList();
        Map<Long, CmsArticleDetail> articleDetails = new HashMap<>();
        if (text) {
            articleDetails.putAll(this.articleService.dao().listByIds(contentIds)
                    .stream().collect(Collectors.toMap(CmsArticleDetail::getContentId, c -> c)));
        }

        List<ArticleApiVO> list = new ArrayList<>();
        pageResult.getRecords().forEach(c -> {
            ArticleApiVO vo = ArticleApiVO.newInstance(c, null);
            CmsCatalog catalog = this.catalogService.getCatalog(c.getCatalogId());
            vo.setCatalogName(catalog.getName());
            vo.setCatalogLink(catalogService.getCatalogLink(catalog, 1, publishPipeCode, preview));
            vo.setLink(this.contentService.getContentLink(c, 1, publishPipeCode, preview));
            vo.setLogoSrc(InternalUrlUtils.getActualUrl(c.getLogo(), publishPipeCode, preview));
            if (text && articleDetails.containsKey(c.getContentId())) {
                vo.setContentHtml(articleDetails.get(c.getContentId()).getContentHtml());
            }
            list.add(vo);
        });
        return R.ok(list);
    }
}
