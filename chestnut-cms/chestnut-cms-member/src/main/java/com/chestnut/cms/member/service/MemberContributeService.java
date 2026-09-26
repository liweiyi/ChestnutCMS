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
package com.chestnut.cms.member.service;

import com.chestnut.article.ArticleContentType;
import com.chestnut.article.domain.dto.ArticleDTO;
import com.chestnut.cms.member.domain.dto.ArticleContributeDTO;
import com.chestnut.cms.member.exception.CmsMemberErrorCode;
import com.chestnut.common.security.domain.Operator;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.ContentCoreConsts;
import com.chestnut.contentcore.core.IContent;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.exception.ContentCoreErrorCode;
import com.chestnut.contentcore.fixed.dict.ContentStatus;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.system.fixed.dict.YesOrNo;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Objects;

/**
 * 会员投稿服务类
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Service
@RequiredArgsConstructor
public class MemberContributeService {

    private final IContentService contentService;
    private final ArticleContentType articleContentType;
    private final RedissonClient redissonClient;
    private final TransactionTemplate transactionTemplate;

    /**
     * 投稿
     */
    public void contribute(ArticleContributeDTO dto, Operator operator) {
        if (IdUtils.validate(dto.getContentId())) {
            this.contributeUpdate(dto, operator);
        } else {
            this.contributeAdd(dto, operator);
        }
    }

    private void contributeAdd(ArticleContributeDTO dto, Operator operator) {
        List<String> images = StringUtils.isNotEmpty(dto.getLogo()) ? List.of(dto.getLogo()) : List.of();
        String[] tags = Objects.nonNull(dto.getTags()) ? dto.getTags().toArray(String[]::new) : new String[0];

        ArticleDTO articleDTO = new ArticleDTO();
        BeanUtils.copyProperties(dto, articleDTO);
        articleDTO.setContentId(IdUtils.getSnowflakeId());
        articleDTO.setContentType(ArticleContentType.ID);
        articleDTO.setTags(tags);
        articleDTO.setImages(images);
        articleDTO.setDownloadRemoteImage(YesOrNo.NO);
        articleDTO.setPublishPipeProps(List.of());

        IContent<?> content = articleContentType.dto2content(articleDTO);
        content.setOperator(operator);
        content.getContentEntity().setContributorId(operator.getUserId());
        transactionTemplate.executeWithoutResult(status -> {
            content.add();
        });
    }

    private void contributeUpdate(ArticleContributeDTO dto, Operator operator) {
        RLock lock = redissonClient.getLock(ContentCoreConsts.lockContent(dto.getContentId()));
        if (!lock.tryLock()) {
            throw ContentCoreErrorCode.CONTENT_EDITING.exception();
        }
        try {
            List<String> images = StringUtils.isNotEmpty(dto.getLogo()) ? List.of(dto.getLogo()) : List.of();
            String[] tags = Objects.nonNull(dto.getTags()) ? dto.getTags().toArray(String[]::new) : new String[0];

            CmsContent cmsContent = this.contentService.dao().getById(dto.getContentId());
            if (!operator.getUserId().equals(cmsContent.getContributorId())) {
                throw CmsMemberErrorCode.CONTENT_ACCESS_DENY.exception();
            }
            if (!ContentStatus.isDraft(cmsContent.getStatus())) {
                throw CmsMemberErrorCode.NOT_DRAFT_CONTENT.exception();
            }

            ArticleDTO articleDTO = new ArticleDTO();
            BeanUtils.copyProperties(dto, articleDTO);
            articleDTO.setTags(tags);
            articleDTO.setImages(images);
            articleDTO.setPublishPipeProps(List.of());

            IContent<?> content = this.articleContentType.loadContent(cmsContent);
            content.setParams(dto.getParams());
            content.setOperator(operator);
            transactionTemplate.executeWithoutResult(status -> {
                content.save(articleDTO);
            });
        } finally {
            lock.unlock();
        }
    }
}
