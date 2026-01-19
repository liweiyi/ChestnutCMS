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
package com.chestnut.cms.stat.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chestnut.cms.stat.domain.CmsUserContentStat;
import com.chestnut.cms.stat.mapper.CmsUserContentStatMapper;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.fixed.dict.ContentStatus;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.system.domain.SysUser;
import com.chestnut.system.service.ISysUserService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 后台用户内容数量状态分组统计，5分钟定时更新有变更的栏目数据到数据库
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserContentCountByStatus {

    private final IContentService contentService;

    private final ISysUserService userService;

    private final CmsUserContentStatMapper userContentStatMapper;

    /**
     * <userName, List<siteId>>
     */
    private static final Map<String, Set<Long>> changes = new HashMap<>();

    public synchronized void triggerChange(String userName, Long siteId) {
        if (changes.containsKey(userName)) {
            changes.get(userName).add(siteId);
        } else {
            Set<Long> set = new HashSet<>();
            set.add(siteId);
            changes.put(userName, set);
        }
    }

    private synchronized Map<String, Set<Long>> getChangeUserIdsAndClear() {
        if (changes.isEmpty()) {
            return null;
        }
        Map<String, Set<Long>> collect = changes.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        changes.clear();
        return collect;
    }

    protected void update() {
        Map<String, Set<Long>> map = getChangeUserIdsAndClear();
        if (Objects.nonNull(map) && !map.isEmpty()) {
            long s = System.currentTimeMillis();
            map.forEach((uname, siteIds) -> {
                Optional<SysUser> opt = this.userService.lambdaQuery().eq(SysUser::getUserName, uname).oneOpt();
                if (opt.isPresent()) {
                    SysUser user = opt.get();
                    siteIds.forEach(siteId -> {
                        boolean insert = false;
                        List<CmsUserContentStat> stats = this.userContentStatMapper
                                .selectList(new LambdaQueryWrapper<CmsUserContentStat>()
                                .eq(CmsUserContentStat::getUserId, user.getUserId())
                                .eq(CmsUserContentStat::getSiteId, siteId));
                        if (stats.size() > 1) {
                            // 兼容下历史错误数据，直接干掉~
                            this.userContentStatMapper.deleteByIds(stats.stream().map(CmsUserContentStat::getId).toList());
                        }
                        CmsUserContentStat stat;
                        if (StringUtils.isEmpty(stats)) {
                            stat = new CmsUserContentStat();
                            stat.setId(IdUtils.getSnowflakeIdStr());
                            stat.setUserId(user.getUserId());
                            stat.setUserName(user.getUserName());
                            stat.setSiteId(siteId);
                            insert = true;
                        } else {
                            stat = stats.get(0);
                        }
                        List<String> allStatus = ContentStatus.all();
                        for (String status : allStatus) {
                            Long total = contentService.dao().lambdaQuery()
                                    .eq(CmsContent::getContributorId, 0)
                                    .eq(CmsContent::getSiteId, siteId)
                                    .eq(CmsContent::getCreateBy, stat.getUserName())
                                    .eq(CmsContent::getStatus, status)
                                    .count();
                            stat.changeStatusTotal(status, total.intValue());
                        }
                        if (insert) {
                            this.userContentStatMapper.insert(stat);
                        } else {
                            this.userContentStatMapper.updateById(stat);
                        }
                    });
                }
            });
            log.info("Stat user content by status cost: " + (System.currentTimeMillis() - s) + " ms");
        }
    }

    @PreDestroy
    public void preDestroy() {
        this.update();
        log.info("Stat user content by status complete. ");
    }
}
