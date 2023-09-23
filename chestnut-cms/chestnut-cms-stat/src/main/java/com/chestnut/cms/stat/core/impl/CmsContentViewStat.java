package com.chestnut.cms.stat.core.impl;

import com.chestnut.cms.stat.core.CmsStat;
import com.chestnut.cms.stat.domain.CmsSiteVisitLog;
import com.chestnut.contentcore.service.impl.ContentDynamicDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 内容动态浏览量统计
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component
@RequiredArgsConstructor
public class CmsContentViewStat implements CmsStat {

    private final ContentDynamicDataService contentDynamicDataService;

    @Override
    public void deal(final CmsSiteVisitLog log) {
        this.contentDynamicDataService.increaseViewCount(log.getContentId());
    }
}
