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
package com.chestnut.article;

import com.chestnut.article.domain.CmsArticleDetail;
import com.chestnut.article.domain.dto.ArticleDTO;
import com.chestnut.article.domain.vo.ArticleVO;
import com.chestnut.article.mapper.CmsArticleDetailMapper;
import com.chestnut.article.properties.DownloadRemoteImage;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.IContent;
import com.chestnut.contentcore.core.IContentType;
import com.chestnut.contentcore.core.IPublishPipeProp.PublishPipePropUseType;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.CmsPublishPipe;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.dto.PublishPipeProp;
import com.chestnut.contentcore.domain.vo.ContentVO;
import com.chestnut.contentcore.enums.ContentOpType;
import com.chestnut.contentcore.fixed.dict.ContentAttribute;
import com.chestnut.contentcore.mapper.CmsContentMapper;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.IPublishPipeService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.util.InternalUrlUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(IContentType.BEAN_NAME_PREFIX + ArticleContentType.ID)
@RequiredArgsConstructor
public class ArticleContentType implements IContentType {

    public final static String ID = "article";

    private final static String NAME = "{CMS.CONTENTCORE.CONTENT_TYPE." + ID + "}";

    private final CmsContentMapper contentMapper;

    private final CmsArticleDetailMapper articleMapper;

    private final ISiteService siteService;

    private final ICatalogService catalogService;

    private final IPublishPipeService publishPipeService;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getComponent() {
        return "cms/article/editor";
    }

    @Override
    public IContent<?> newContent() {
        return new ArticleContent();
    }

    @Override
    public IContent<?> loadContent(CmsContent xContent) {
        ArticleContent articleContent = new ArticleContent();
        articleContent.setContentEntity(xContent);
        CmsArticleDetail articleDetail = this.articleMapper.selectById(xContent.getContentId());
        articleContent.setExtendEntity(articleDetail);
        return articleContent;
    }

    @Override
    public IContent<?> readRequest(HttpServletRequest request) throws IOException {
        ArticleDTO dto = JacksonUtils.from(request.getInputStream(), ArticleDTO.class);

        CmsContent contentEntity;
        if (dto.getOpType() == ContentOpType.UPDATE) {
            contentEntity = this.contentMapper.selectById(dto.getContentId());
            Assert.notNull(contentEntity,
                    () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("contentId", dto.getContentId()));
        } else {
            contentEntity = new CmsContent();
        }
        BeanUtils.copyProperties(dto, contentEntity);
        CmsCatalog catalog = this.catalogService.getCatalog(dto.getCatalogId());
        contentEntity.setSiteId(catalog.getSiteId());
        contentEntity.setAttributes(ContentAttribute.convertInt(dto.getAttributes()));
        // 发布通道配置
        Map<String, Map<String, Object>> publishPipProps = new HashMap<>();
        dto.getPublishPipeProps().forEach(prop -> {
            publishPipProps.put(prop.getPipeCode(), prop.getProps());
        });
        contentEntity.setPublishPipeProps(publishPipProps);

        CmsArticleDetail extendEntity = new CmsArticleDetail();
        BeanUtils.copyProperties(dto, extendEntity);

        ArticleContent content = new ArticleContent();
        content.setContentEntity(contentEntity);
        content.setExtendEntity(extendEntity);
        content.setParams(dto.getParams());
        if (content.hasExtendEntity() && StringUtils.isEmpty(extendEntity.getContentHtml())) {
            throw CommonErrorCode.NOT_EMPTY.exception("contentHtml");
        }
        return content;
    }

    @Override
    public ContentVO initEditor(Long catalogId, Long contentId) {
        CmsCatalog catalog = this.catalogService.getCatalog(catalogId);
        Assert.notNull(catalog, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("catalogId", catalogId));
        List<CmsPublishPipe> publishPipes = this.publishPipeService.getPublishPipes(catalog.getSiteId());
        ArticleVO vo;
        if (IdUtils.validate(contentId)) {
            CmsContent contentEntity = this.contentMapper.selectById(contentId);
            Assert.notNull(contentEntity, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("contentId", contentId));

            CmsArticleDetail extendEntity = this.articleMapper.selectById(contentId);
            vo = ArticleVO.newInstance(contentEntity, extendEntity);
            if (StringUtils.isNotEmpty(vo.getLogo())) {
                vo.setLogoSrc(InternalUrlUtils.getActualPreviewUrl(vo.getLogo()));
            }
            // 发布通道模板数据
            List<PublishPipeProp> publishPipeProps = this.publishPipeService.getPublishPipeProps(catalog.getSiteId(),
                    PublishPipePropUseType.Content, contentEntity.getPublishPipeProps());
            vo.setPublishPipeProps(publishPipeProps);
        } else {
            vo = new ArticleVO();
            vo.setContentId(IdUtils.getSnowflakeId());
            vo.setCatalogId(catalog.getCatalogId());
            vo.setContentType(ID);
            CmsSite site = siteService.getSite(catalog.getSiteId());
            vo.setDownloadRemoteImage(DownloadRemoteImage.getValue(site.getConfigProps()));
            // 发布通道初始数据
            vo.setPublishPipe(publishPipes.stream().map(CmsPublishPipe::getCode).toArray(String[]::new));
            // 发布通道模板数据
            List<PublishPipeProp> publishPipeProps = this.publishPipeService.getPublishPipeProps(catalog.getSiteId(),
                    PublishPipePropUseType.Content, null);
            vo.setPublishPipeProps(publishPipeProps);
        }
        vo.setCatalogName(catalog.getName());
        return vo;
    }

    @Override
    public void recover(Long contentId) {
        this.articleMapper.recoverById(contentId);
    }

    @Override
    public void deleteBackups(Long contentId) {
        this.articleMapper.deleteLogicDeletedById(contentId);
    }
}
