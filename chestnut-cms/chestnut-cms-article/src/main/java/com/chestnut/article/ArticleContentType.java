/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.IContent;
import com.chestnut.contentcore.core.IContentType;
import com.chestnut.contentcore.core.IPublishPipeProp.PublishPipePropUseType;
import com.chestnut.contentcore.domain.*;
import com.chestnut.contentcore.domain.dto.PublishPipeProp;
import com.chestnut.contentcore.domain.vo.ContentVO;
import com.chestnut.contentcore.enums.ContentCopyType;
import com.chestnut.contentcore.fixed.dict.ContentOpType;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.contentcore.service.IPublishPipeService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.system.fixed.dict.YesOrNo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

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
    public IContent<?> readFrom(InputStream is) {
        ArticleDTO dto = JacksonUtils.from(is, ArticleDTO.class);
        return readFrom0(dto);
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
            }
        }
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
            CmsContent contentEntity = this.contentService.dao().getById(contentId);
            Assert.notNull(contentEntity, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("contentId", contentId));

            CmsArticleDetail extendEntity = this.articleService.dao().getById(contentId);
            vo = ArticleVO.newInstance(contentEntity, extendEntity);
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
    public void recover(BCmsContent backupContent) {
        this.contentService.dao().recover(backupContent);

        if (!YesOrNo.isYes(backupContent.getLinkFlag()) && !ContentCopyType.isMapping(backupContent.getCopyType())) {
            BCmsArticleDetail backupArticle = this.articleService.dao().getOneBackup(new LambdaQueryWrapper<BCmsArticleDetail>()
                    .eq(BCmsArticleDetail::getContentId, backupContent.getContentId())
                    .eq(BCmsArticleDetail::getBackupRemark, DBConstants.BACKUP_REMARK_DELETE));
            this.articleService.dao().recover(backupArticle);
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
