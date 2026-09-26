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
package com.chestnut.article;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chestnut.article.domain.BCmsArticleDetail;
import com.chestnut.article.domain.CmsArticleDetail;
import com.chestnut.article.domain.dto.ArticleDTO;
import com.chestnut.article.domain.vo.ArticleVO;
import com.chestnut.article.properties.DownloadRemoteImage;
import com.chestnut.article.service.IArticleService;
import com.chestnut.common.db.DBConstants;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.IContent;
import com.chestnut.contentcore.core.IContentType;
import com.chestnut.contentcore.domain.BCmsContent;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.dto.ContentDTO;
import com.chestnut.contentcore.domain.vo.ContentVO;
import com.chestnut.contentcore.exception.ContentCoreErrorCode;
import com.chestnut.contentcore.fixed.dict.ContentCopyType;
import com.chestnut.contentcore.fixed.dict.ContentOpType;
import com.chestnut.contentcore.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component(IContentType.BEAN_NAME_PREFIX + ArticleContentType.ID)
@RequiredArgsConstructor
public class ArticleContentType implements IContentType {

    public final static String ID = "article";

    private final static String NAME = "{CMS.CONTENTCORE.CONTENT_TYPE." + ID + "}";

    private final ISiteService siteService;

    private final ICatalogService catalogService;

    private final IPublishPipeService publishPipeService;

    private final IArticleService articleService;

    private final IContentService contentService;

    private final IResourceService resourceService;

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
        CmsArticleDetail articleDetail = this.articleService.dao().getById(xContent.getContentId());
        articleContent.setExtendEntity(articleDetail);
        return articleContent;
    }

    @Override
    public ContentDTO parseRequest(InputStream is) {
        return JacksonUtils.from(is, ArticleDTO.class);
    }

    @Override
    public IContent<?> dto2content(ContentDTO dto) {
        if (dto instanceof ArticleDTO _dto) {
            return readFrom0(_dto);
        }
        throw ContentCoreErrorCode.DTO_NOT_MATCH_CONTENT_TYPE.exception();
    }

    private ArticleContent readFrom0(ArticleDTO dto) {
        // 内容基础信息
        CmsContent contentEntity = dto.convertToContentEntity(this.catalogService, this.contentService);
        // 文章扩展信息
        CmsArticleDetail extendEntity = new CmsArticleDetail();
        if (ContentOpType.UPDATE.equals(dto.getOpType())) {
            Optional<CmsArticleDetail> opt = this.articleService.dao().getOptById(contentEntity.getContentId());
            if (opt.isPresent()) {
                extendEntity = opt.get();
            } else {
                extendEntity = new CmsArticleDetail();
                extendEntity.setContentId(contentEntity.getContentId());
                extendEntity.setSiteId(contentEntity.getSiteId());
            }
        }
        BeanUtils.copyProperties(dto, extendEntity);

        ArticleContent content = new ArticleContent();
        content.setContentEntity(contentEntity);
        content.setExtendEntity(extendEntity);
        content.setParams(dto.getParams());
        if (content.hasExtendEntity()) {
            if (StringUtils.isEmpty(extendEntity.getContentHtml())) {
                throw CommonErrorCode.NOT_EMPTY.exception("contentHtml");
            }
            IArticleBodyFormat format = articleService.getArticleBodyFormat(extendEntity.getFormat());
            if (Objects.nonNull(format)) {
                String contentHtml = format.onSave(extendEntity.getContentHtml());
                extendEntity.setContentHtml(contentHtml);
            }
        }
        return content;
    }

    @Override
    public ContentVO initEditor(CmsCatalog catalog, CmsContent contentEntity) {
        CmsSite site = siteService.getSite(catalog.getSiteId());
        ArticleVO vo;
        if (Objects.nonNull(contentEntity)) {
            CmsArticleDetail extendEntity = this.articleService.dao().getById(contentEntity.getContentId());
            vo = ArticleVO.newInstance(contentEntity, extendEntity);
        } else {
            vo = new ArticleVO();
            vo.setDownloadRemoteImage(DownloadRemoteImage.getValue(site.getConfigProps()));
        }
        // 文章正文内容处理
        IArticleBodyFormat articleBodyFormat = articleService.getArticleBodyFormat(vo.getFormat());
        if (Objects.nonNull(articleBodyFormat)) {
            vo.setContentHtml(articleBodyFormat.initEditor(vo.getContentHtml()));
        } else {
            log.warn("Unsupported article body format: " + vo.getFormat());
        }
        return vo;
    }

    @Override
    public void recover(BCmsContent backupContent) {
        this.contentService.dao().recover(backupContent);

        if (!ContentCopyType.isMapping(backupContent.getCopyType())) {
            BCmsArticleDetail backupArticle = this.articleService.dao().getOneBackup(new LambdaQueryWrapper<BCmsArticleDetail>()
                    .eq(BCmsArticleDetail::getContentId, backupContent.getContentId())
                    .eq(BCmsArticleDetail::getBackupRemark, DBConstants.BACKUP_REMARK_DELETE));
            if (Objects.nonNull(backupArticle)) {
                this.articleService.dao().recover(backupArticle);
            }
        }
    }

    @Override
    public void deleteBackups(Long contentId) {
        this.contentService.dao().deleteBackups(new LambdaQueryWrapper<BCmsContent>()
                .eq(BCmsContent::getContentId, contentId)
                .eq(BCmsContent::getBackupRemark, DBConstants.BACKUP_REMARK_DELETE));
        this.articleService.dao().deleteBackups(new LambdaQueryWrapper<BCmsArticleDetail>()
                .eq(BCmsArticleDetail::getContentId, contentId)
                .eq(BCmsArticleDetail::getBackupRemark, DBConstants.BACKUP_REMARK_DELETE));
    }
}
