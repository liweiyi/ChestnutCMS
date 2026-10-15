package com.chestnut.cms.member.impl;

import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.member.core.IMemberFavoriteDataType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component(IMemberFavoriteDataType.BEAN_PREFIX + CmsContentMemberFavoriteDataType.TYPE)
public class CmsContentMemberFavoriteDataType implements IMemberFavoriteDataType {

    public static final String TYPE = "cms_content";

    private final IContentService contentService;

    @Override
    public List<MemberFavoriteData> getDataList(List<String> dataIds) {
        return this.contentService.dao().lambdaQuery()
                .select(CmsContent::getContentId, CmsContent::getTitle, CmsContent::getSummary)
                .in(CmsContent::getContentId, dataIds)
                .list()
                .stream().map(content -> {
                    return new MemberFavoriteData(TYPE, content.getContentId().toString(), content.getTitle(), content.getSummary());
                }).toList();
    }
}
