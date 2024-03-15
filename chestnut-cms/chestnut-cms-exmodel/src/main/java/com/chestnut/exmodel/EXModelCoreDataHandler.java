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
package com.chestnut.exmodel;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.utils.ConvertUtils;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.ICoreDataHandler;
import com.chestnut.contentcore.core.SiteExportContext;
import com.chestnut.contentcore.core.SiteImportContext;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.util.InternalUrlUtils;
import com.chestnut.exmodel.domain.CmsExtendModelData;
import com.chestnut.exmodel.fixed.dict.ExtendModelDataType;
import com.chestnut.exmodel.mapper.ExtendModelMapper;
import com.chestnut.exmodel.properties.CatalogExtendModelProperty;
import com.chestnut.exmodel.properties.ContentExtendModelProperty;
import com.chestnut.exmodel.properties.SiteExtendModelProperty;
import com.chestnut.xmodel.core.MetaModel;
import com.chestnut.xmodel.domain.XModel;
import com.chestnut.xmodel.domain.XModelField;
import com.chestnut.xmodel.service.IModelFieldService;
import com.chestnut.xmodel.service.IModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;

/**
 * 扩展模型内容核心数据处理器
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EXModelCoreDataHandler implements ICoreDataHandler {

    private static final String XMODEL_TABLE_SUFFIX = "_cms_exmodel";

    private final ExtendModelMapper modelDataMapper;

    private final IModelService modelService;

    private final IModelFieldService modelFieldService;

    private final ISiteService siteService;

    private final ICatalogService catalogService;

    @Override
    public void onSiteExport(SiteExportContext context) {
        AsyncTaskManager.setTaskTenPercentProgressInfo("正在导出扩展模型数据");
        // 扩展模型数据导出
        Set<String> modelIdStrings = new HashSet<>();
        String siteModelIdStr = SiteExtendModelProperty.getValue(context.getSite().getConfigProps());
        modelIdStrings.add(siteModelIdStr);

        List<CmsCatalog> catalogs = catalogService.lambdaQuery()
                .eq(CmsCatalog::getSiteId, context.getSite().getSiteId()).list();
        catalogs.forEach(catalog -> {
            String catalogModelIdStr = CatalogExtendModelProperty.getValue(catalog.getConfigProps());
            modelIdStrings.add(catalogModelIdStr);
            String contentModelIdStr = ContentExtendModelProperty.getValue(catalog.getConfigProps());
            modelIdStrings.add(contentModelIdStr);
        });

        // 扩展模型
        List<XModel> modelList = this.modelService.lambdaQuery().in(XModel::getModelId, modelIdStrings).list();
        context.saveData(XModel.TABLE_NAME + XMODEL_TABLE_SUFFIX, JacksonUtils.to(modelList));
        // 扩展模型字段
        List<XModelField> fieldList = this.modelFieldService.lambdaQuery()
                .in(XModelField::getModelId, modelIdStrings).list();
        context.saveData(XModelField.TABLE_NAME + XMODEL_TABLE_SUFFIX, JacksonUtils.to(fieldList));
        // 扩展模型数据
        int pageSize = 200;
        int fileIndex = 1;
        for (XModel model : modelList) {
            int pageIndex = 1;
            while (true) {
                LambdaQueryWrapper<CmsExtendModelData> q = new LambdaQueryWrapper<CmsExtendModelData>()
                        .eq(CmsExtendModelData::getModelId, model.getModelId());
                Page<CmsExtendModelData> page = modelDataMapper.selectPage(new Page<>(pageIndex, pageSize, false), q);
                if (page.getRecords().isEmpty()) {
                    break;
                }
                pageIndex++;
                context.saveData(CmsExtendModelData.TABLE_NAME, JacksonUtils.to(page.getRecords()), fileIndex);
                fileIndex++;
            }
        }
    }

    @Override
    public void onSiteImport(SiteImportContext context) {
        AsyncTaskManager.setTaskTenPercentProgressInfo("正在导入扩展模型");
        // XModel
        Map<Long, Long> modelIdMapping = new HashMap<>();
        List<File> files = context.readDataFiles(XModel.TABLE_NAME + XMODEL_TABLE_SUFFIX);
        files.forEach(f -> {
            List<XModel> list = JacksonUtils.fromList(f, XModel.class);
            for (XModel data : list) {
                try {
                    Long oldModelId = data.getModelId();
                    data.setModelId(IdUtils.getSnowflakeId());
                    Long count = modelService.lambdaQuery().eq(XModel::getCode, data.getCode()).count();
                    if (count > 0) {
                        data.setCode(context.getSite().getPath() + "_" + data.getCode());
                    }
                    data.setOwnerId(context.getSite().getSiteId().toString());
                    data.createBy(context.getOperator());
                    modelService.save(data);
                    modelIdMapping.put(oldModelId, data.getModelId());
                } catch (Exception e) {
                    AsyncTaskManager.addErrMessage("导入扩展模型失败：" + data.getName()
                            + "[" + data.getModelId() + "]");
                    log.error("Import xmodel failed: {}", data.getCode(), e);
                }
            }
        });
        // XModelField
        AsyncTaskManager.setTaskTenPercentProgressInfo("正在导入扩展模型字段");
        files = context.readDataFiles(XModelField.TABLE_NAME + XMODEL_TABLE_SUFFIX);
        files.forEach(f -> {
            List<XModelField> list = JacksonUtils.fromList(f, XModelField.class);
            for (XModelField data : list) {
                try {
                    data.setFieldId(IdUtils.getSnowflakeId());
                    data.setModelId(modelIdMapping.get(data.getModelId()));
                    data.createBy(context.getOperator());
                    modelFieldService.save(data);
                } catch (Exception e) {
                    AsyncTaskManager.addErrMessage("导入扩展模型字段失败：" + data.getName()
                            + "[" + data.getFieldId() + "]");
                    log.error("Import xmodel field failed: {}", data.getCode(), e);
                }
            }
        });
        // CmsExtendModelData
        AsyncTaskManager.setTaskTenPercentProgressInfo("正在导入扩展模型数据");
        files = context.readDataFiles(CmsExtendModelData.TABLE_NAME);
        files.forEach(f -> {
            List<CmsExtendModelData> list = JacksonUtils.fromList(f, CmsExtendModelData.class);
            for (CmsExtendModelData data : list) {
                try {
                    switch (data.getDataType()) {
                        case ExtendModelDataType.SITE -> data.setDataId(context.getSite().getSiteId());
                        case ExtendModelDataType.CATALOG -> data.setDataId(context.getCatalogIdMap().get(data.getDataId()));
                        case ExtendModelDataType.CONTENT -> data.setDataId(context.getContentIdMap().get(data.getDataId()));
                    }
                    data.setModelId(modelIdMapping.get(data.getModelId()));
                    // 处理图片和富文本内部链接
                    MetaModel model = modelService.getMetaModel(data.getModelId());
                    model.getFields().forEach(field -> {
                        if (MetaControlType_CmsImage.TYPE.equals(field.getControlType())) {
                            String fieldValue = data.getStringFieldValue(field.getFieldName());
                            data.setFieldValue(field.getFieldName(), context.dealInternalUrl(fieldValue));
                        } else if (MetaControlType_UEditor.TYPE.equals(field.getControlType())) {
                            String fieldValue = data.getStringFieldValue(field.getFieldName());
                            if (StringUtils.isNotEmpty(fieldValue)) {
                                // 替换正文内部资源地址
                                StringBuilder html = new StringBuilder();
                                int index = 0;
                                Matcher matcher = InternalUrlUtils.InternalUrlTagPattern.matcher(fieldValue);
                                while (matcher.find()) {
                                    String tagStr = matcher.group();
                                    String iurl = matcher.group(1);
                                    String newIurl = context.dealInternalUrl(iurl);
                                    tagStr = StringUtils.replaceEx(tagStr, iurl, newIurl);
                                    html.append(fieldValue, index, matcher.start()).append(tagStr);
                                    index = matcher.end();
                                }
                                html.append(fieldValue.substring(index));
                                data.setFieldValue(field.getFieldName(), html.toString());
                            }
                        }
                    });
                    modelDataMapper.insert(data);
                } catch (Exception e) {
                    AsyncTaskManager.addErrMessage("导入扩展模型数据失败：" + data.getModelId()
                            + data.getDataType() + "-" + data.getDataId());
                    log.error("Import xmodel data failed: {} - {}", data.getModelId(), data.getDataId(), e);
                }
            }
        });
        // 更新站点及栏目扩展模型配置
        try {
            Long siteModelId = ConvertUtils.toLong(SiteExtendModelProperty.getValue(context.getSite().getConfigProps()));
            if (IdUtils.validate(siteModelId) && modelIdMapping.containsKey(siteModelId)) {
                context.getSite().getConfigProps().put(SiteExtendModelProperty.ID,
                        modelIdMapping.get(siteModelId).toString());
                siteService.updateById(context.getSite());
                siteService.clearCache(context.getSite().getSiteId());
            }
            List<CmsCatalog> catalogs = catalogService.lambdaQuery().eq(CmsCatalog::getSiteId, context.getSite().getSiteId()).list();
            catalogs.forEach(catalog -> {
                boolean changed = false;
                Long catalogModelId = ConvertUtils.toLong(CatalogExtendModelProperty.getValue(catalog.getConfigProps()));
                if (IdUtils.validate(catalogModelId) && modelIdMapping.containsKey(catalogModelId)) {
                    catalog.getConfigProps().put(CatalogExtendModelProperty.ID,
                            modelIdMapping.get(catalogModelId).toString());
                    changed = true;
                }
                Long contentModelId = ConvertUtils.toLong(ContentExtendModelProperty.getValue(catalog.getConfigProps()));
                if (IdUtils.validate(contentModelId) && modelIdMapping.containsKey(contentModelId)) {
                    catalog.getConfigProps().put(ContentExtendModelProperty.ID,
                            modelIdMapping.get(contentModelId).toString());
                    changed = true;
                }
                if (changed) {
                    catalogService.updateById(catalog);
                    catalogService.clearCache(catalog);
                }
            });
        } catch (Exception e) {
            AsyncTaskManager.addErrMessage("更新站点及栏目扩展模型配置失败");
            log.error("Import site/catalog exmodel config failed: {}", context.getSite().getSiteId(), e);
        }
    }
}
