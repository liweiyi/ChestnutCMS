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
package com.chestnut.cms.word;

import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.contentcore.core.ICoreDataHandler;
import com.chestnut.contentcore.core.SiteExportContext;
import com.chestnut.contentcore.core.SiteImportContext;
import com.chestnut.word.domain.HotWord;
import com.chestnut.word.domain.HotWordGroup;
import com.chestnut.word.domain.TagWord;
import com.chestnut.word.domain.TagWordGroup;
import com.chestnut.word.service.IHotWordGroupService;
import com.chestnut.word.service.IHotWordService;
import com.chestnut.word.service.ITagWordGroupService;
import com.chestnut.word.service.ITagWordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 词汇管理内容核心数据处理器
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WordContentCoreHandler implements ICoreDataHandler {

    private final ITagWordGroupService tagWordGroupService;

    private final ITagWordService tagWordService;

    private final IHotWordGroupService hotWordGroupService;

    private final IHotWordService hotWordService;

    @Override
    public void onSiteExport(SiteExportContext context) {
        {
            AsyncTaskManager.setTaskTenPercentProgressInfo(CmsWordTips.EXPORTING_TAG_GROUP);
            List<TagWordGroup> list = tagWordGroupService.lambdaQuery()
                    .eq(TagWordGroup::getOwner, context.getSite().getSiteId())
                    .list();
            context.saveData(TagWordGroup.TABLE_NAME, JacksonUtils.to(list));
        }
        {
            AsyncTaskManager.setTaskTenPercentProgressInfo(CmsWordTips.EXPORTING_TAG);
            List<TagWord> list = tagWordService.lambdaQuery()
                    .eq(TagWord::getOwner, context.getSite().getSiteId())
                    .list();
            context.saveData(TagWord.TABLE_NAME, JacksonUtils.to(list));
        }
        {
            AsyncTaskManager.setTaskTenPercentProgressInfo(CmsWordTips.EXPORTING_HOT_WORD_GROUP);
            List<HotWordGroup> list = hotWordGroupService.lambdaQuery()
                    .eq(HotWordGroup::getOwner, context.getSite().getSiteId())
                    .list();
            context.saveData(HotWordGroup.TABLE_NAME, JacksonUtils.to(list));
        }
        {
            AsyncTaskManager.setTaskTenPercentProgressInfo(CmsWordTips.EXPORTING_HOT_WORD);
            List<HotWord> list = hotWordService.lambdaQuery()
                    .eq(HotWord::getOwner, context.getSite().getSiteId())
                    .list();
            context.saveData(HotWord.TABLE_NAME, JacksonUtils.to(list));
        }
    }

    @Override
    public void onSiteImport(SiteImportContext context) {
        AsyncTaskManager.setTaskTenPercentProgressInfo(CmsWordTips.IMPORTING_TAG_GROUP);
        Map<Long, TagWordGroup> tagGroupIdMap = new HashMap<>();

        List<File> files = context.readDataFiles(TagWordGroup.TABLE_NAME);
        files.forEach(f -> {
            List<TagWordGroup> list = JacksonUtils.fromList(f, TagWordGroup.class);
            for (TagWordGroup data : list) {
                Long oldGroupId = data.getGroupId();
                try {
                    data.setGroupId(IdUtils.getSnowflakeId());
                    data.setOwner(context.getSite().getSiteId().toString());
                    tagWordGroupService.checkUnique(data.getOwner(), data.getGroupId(), data.getCode());

                    data.createBy(context.getOperator());
                    tagWordGroupService.save(data);
                    tagGroupIdMap.put(oldGroupId, data);
                } catch (Exception e) {
                    AsyncTaskManager.addErrMessage(CmsWordTips.IMPORT_TAG_GROUP_FAIL, oldGroupId, e.getMessage());
                    log.error("Import tag word group failed", e);
                }
            }
        });
        tagGroupIdMap.values().forEach(group -> {
            if (group.getParentId() > 0) {
                TagWordGroup parent = tagGroupIdMap.get(group.getParentId());
                group.setParentId(parent.getGroupId());
                tagWordGroupService.updateById(group);
            }
        });

        AsyncTaskManager.setTaskTenPercentProgressInfo(CmsWordTips.IMPORTING_TAG);
        files = context.readDataFiles(TagWord.TABLE_NAME);
        files.forEach(f -> {
            List<TagWord> list = JacksonUtils.fromList(f, TagWord.class);
            for (TagWord data : list) {
                Long oldTagId = data.getWordId();
                try {
                    data.setWordId(IdUtils.getSnowflakeId());
                    data.setGroupId(tagGroupIdMap.get(data.getGroupId()).getGroupId());
                    data.setOwner(context.getSite().getSiteId().toString());
                    data.createBy(context.getOperator());
                    tagWordService.save(data);
                } catch (Exception e) {
                    AsyncTaskManager.addErrMessage(CmsWordTips.IMPORT_TAG_FAIL, oldTagId, e.getMessage());
                    log.error("Import tag word failed", e);
                }
            }
        });

        AsyncTaskManager.setTaskTenPercentProgressInfo(CmsWordTips.IMPORTING_HOT_WORD_GROUP);
        Map<Long, Long> hotGroupIdMap = new HashMap<>();
        files = context.readDataFiles(HotWordGroup.TABLE_NAME);
        files.forEach(f -> {
            List<HotWordGroup> list = JacksonUtils.fromList(f, HotWordGroup.class);
            for (HotWordGroup data : list) {
                Long oldGroupId = data.getGroupId();
                try {
                    data.setGroupId(IdUtils.getSnowflakeId());
                    data.setOwner(context.getSite().getSiteId().toString());
                    hotWordGroupService.checkUnique(data.getOwner(), data.getGroupId(), data.getCode());

                    data.createBy(context.getOperator());
                    hotWordGroupService.save(data);
                    hotGroupIdMap.put(oldGroupId, data.getGroupId());
                } catch (Exception e) {
                    AsyncTaskManager.addErrMessage(CmsWordTips.IMPORT_HOT_WORD_GROUP_FAIL, oldGroupId, e.getMessage());
                    log.error("Import hot word group failed", e);
                }
            }
        });

        AsyncTaskManager.setTaskTenPercentProgressInfo(CmsWordTips.IMPORTING_HOT_WORD);
        files = context.readDataFiles(HotWord.TABLE_NAME);
        files.forEach(f -> {
            List<HotWord> list = JacksonUtils.fromList(f, HotWord.class);
            for (HotWord data : list) {
                Long oldWordId = data.getWordId();
                try {
                    data.setWordId(IdUtils.getSnowflakeId());
                    data.setGroupId(hotGroupIdMap.get(data.getGroupId()));
                    data.setOwner(context.getSite().getSiteId().toString());
                    data.createBy(context.getOperator());
                    hotWordService.save(data);
                } catch (Exception e) {
                    AsyncTaskManager.addErrMessage(CmsWordTips.IMPORT_HOT_WORD_FAIL, oldWordId, e.getMessage());
                    log.error("Import hot word failed", e);
                }
            }
        });
    }
}
