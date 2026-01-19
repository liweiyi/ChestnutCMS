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
package com.chestnut.link;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.contentcore.core.IResourceStat;
import com.chestnut.contentcore.core.InternalURL;
import com.chestnut.contentcore.util.InternalUrlUtils;
import com.chestnut.link.domain.CmsLink;
import com.chestnut.link.service.ILinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 图集内容资源引用统计
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IResourceStat.BEAN_PREFIX + FriendLinkResourceStat.TYPE)
public class FriendLinkResourceStat implements IResourceStat {

    public static final String TYPE = "FriendLink";

    private final ILinkService linkService;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void statQuotedResource(Long siteId, Map<Long, Long> quotedResources) throws InterruptedException {
        int pageSize = 1000;
        long lastId = 0L;
        int count = 0;
        long total = linkService.lambdaQuery().select(List.of()).eq(CmsLink::getSiteId, siteId).count();
        while (true) {
            LambdaQueryWrapper<CmsLink> q = new LambdaQueryWrapper<CmsLink>()
                    .select(List.of(CmsLink::getLogo))
                    .eq(CmsLink::getSiteId, siteId)
                    .gt(CmsLink::getLinkId, lastId)
                    .orderByAsc(CmsLink::getLinkId);
            Page<CmsLink> page = linkService.page(new Page<>(0, pageSize, false), q);
            for (CmsLink friendLink : page.getRecords()) {
                AsyncTaskManager.setTaskProgressInfo((int) (count * 100 / total),
                        "正在统计友情链接资源引用：" + count + " / " + total + "]");
                lastId = friendLink.getLinkId();
                InternalURL internalURL = InternalUrlUtils.parseInternalUrl(friendLink.getLogo());
                if (Objects.nonNull(internalURL)) {
                    quotedResources.compute(internalURL.getId(), (k, v) -> Objects.isNull(v) ? 1 : v + 1);
                }
                AsyncTaskManager.checkInterrupt();
                count++;
            }
            if (page.getRecords().size() < pageSize) {
                break;
            }
        }
    }
}
