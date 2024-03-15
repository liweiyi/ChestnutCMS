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
package com.chestnut.customform;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.ICoreDataHandler;
import com.chestnut.contentcore.core.SiteExportContext;
import com.chestnut.contentcore.core.SiteImportContext;
import com.chestnut.contentcore.util.InternalUrlUtils;
import com.chestnut.customform.domain.CmsCustomForm;
import com.chestnut.customform.domain.CmsCustomFormData;
import com.chestnut.customform.mapper.CustomFormDataMapper;
import com.chestnut.customform.service.ICustomFormService;
import com.chestnut.exmodel.MetaControlType_CmsImage;
import com.chestnut.exmodel.MetaControlType_UEditor;
import com.chestnut.xmodel.core.MetaModel;
import com.chestnut.xmodel.domain.XModel;
import com.chestnut.xmodel.domain.XModelField;
import com.chestnut.xmodel.service.IModelFieldService;
import com.chestnut.xmodel.service.IModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * 自定义表单内容核心数据处理器
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomFormCoreDataHandler implements ICoreDataHandler {

    private static final String XMODEL_TABLE_SUFFIX = "_cms_custom_form";

    private final ICustomFormService customFormService;

    private final CustomFormDataMapper customFormDataMapper;

    private final IModelService modelService;

    private final IModelFieldService modelFieldService;

    @Override
    public void onSiteExport(SiteExportContext context) {
        AsyncTaskManager.setTaskTenPercentProgressInfo("正在导出自定义表单数据");
        // 自定义表单数据导出
        List<CmsCustomForm> customForms = customFormService.lambdaQuery()
                .eq(CmsCustomForm::getSiteId, context.getSite().getSiteId())
                .list();
        context.saveData(CmsCustomForm.TABLE_NAME, JacksonUtils.to(customForms));

        // 自定义表单关联扩展模型
        List<Long> modelIds = customForms.stream().map(CmsCustomForm::getModelId).toList();
        if (modelIds.isEmpty()) {
            return;
        }
        List<XModel> modelList = this.modelService.lambdaQuery().in(XModel::getModelId, modelIds).list();
        context.saveData(XModel.TABLE_NAME + XMODEL_TABLE_SUFFIX, JacksonUtils.to(modelList));
        // 扩展模型字段
        List<XModelField> fieldList = this.modelFieldService.lambdaQuery()
                .in(XModelField::getModelId, modelIds).list();
        context.saveData(XModelField.TABLE_NAME + XMODEL_TABLE_SUFFIX, JacksonUtils.to(fieldList));
        // 扩展模型数据
        int pageSize = 200;
        int fileIndex = 1;
        for (XModel model : modelList) {
            int pageIndex = 1;
            while (true) {
                LambdaQueryWrapper<CmsCustomFormData> q = new LambdaQueryWrapper<CmsCustomFormData>()
                        .eq(CmsCustomFormData::getModelId, model.getModelId());
                Page<CmsCustomFormData> page = customFormDataMapper.selectPage(new Page<>(pageIndex, pageSize, false), q);
                if (page.getRecords().isEmpty()) {
                    break;
                }
                pageIndex++;
                context.saveData(CmsCustomFormData.TABLE_NAME, JacksonUtils.to(page.getRecords()), fileIndex);
                fileIndex++;
            }
        }
    }

    @Override
    public void onSiteImport(SiteImportContext context) {
        // CmsCustomForm
        AsyncTaskManager.setTaskTenPercentProgressInfo("正在导入自定义表单");
        Map<Long, Long> customFormIdMapping = new HashMap<>();
        List<File> files = context.readDataFiles(CmsCustomForm.TABLE_NAME);
        files.forEach(f -> {
            List<CmsCustomForm> list = JacksonUtils.fromList(f, CmsCustomForm.class);
            for (CmsCustomForm data : list) {
                try {
                    Long oldFormId = data.getFormId();
                    data.setFormId(IdUtils.getSnowflakeId());
                    data.setModelId(data.getFormId());
                    data.setSiteId(context.getSite().getSiteId());
                    data.createBy(context.getOperator());
                    customFormService.save(data);
                    customFormIdMapping.put(oldFormId, data.getFormId());
                } catch (Exception e) {
                    AsyncTaskManager.addErrMessage("导入自定义表单失败：" + data.getName()
                            + "[" + data.getModelId() + "]");
                    log.error("Import custom form failed: {}", data.getCode(), e);
                }
            }
        });
        if (files.isEmpty()) {
            return;
        }
        // XModel
        AsyncTaskManager.setTaskTenPercentProgressInfo("正在导入自定义表单元数据模型");
        files = context.readDataFiles(XModel.TABLE_NAME + XMODEL_TABLE_SUFFIX);
        files.forEach(f -> {
            List<XModel> list = JacksonUtils.fromList(f, XModel.class);
            for (XModel data : list) {
                try {
                    data.setModelId(customFormIdMapping.get(data.getModelId()));
                    data.setCode(data.getModelId().toString());
                    data.setOwnerId(context.getSite().getSiteId().toString());
                    data.createBy(context.getOperator());
                    modelService.save(data);
                } catch (Exception e) {
                    AsyncTaskManager.addErrMessage("导入自定义表单元数据模型失败：" + data.getName()
                            + "[" + data.getModelId() + "]");
                    log.error("Import custom form xmodel failed: {}", data.getCode(), e);
                }
            }
        });
        // XModelField
        AsyncTaskManager.setTaskTenPercentProgressInfo("正在导入自定义表单模型字段");
        files = context.readDataFiles(XModelField.TABLE_NAME + XMODEL_TABLE_SUFFIX);
        files.forEach(f -> {
            List<XModelField> list = JacksonUtils.fromList(f, XModelField.class);
            for (XModelField data : list) {
                try {
                    data.setFieldId(IdUtils.getSnowflakeId());
                    data.setModelId(customFormIdMapping.get(data.getModelId()));
                    data.createBy(context.getOperator());
                    modelFieldService.save(data);
                } catch (Exception e) {
                    AsyncTaskManager.addErrMessage("导入自定义表单模型字段失败：" + data.getName()
                            + "[" + data.getFieldId() + "]");
                    log.error("Import custom form xmodel field failed: {}", data.getCode(), e);
                }
            }
        });
        // CmsCustomFormData
        AsyncTaskManager.setTaskTenPercentProgressInfo("正在导入自定义表单数据");
        files = context.readDataFiles(CmsCustomFormData.TABLE_NAME);
        files.forEach(f -> {
            List<CmsCustomFormData> list = JacksonUtils.fromList(f, CmsCustomFormData.class);
            for (CmsCustomFormData data : list) {
                try {
                    data.setDataId(IdUtils.getSnowflakeId());
                    data.setModelId(customFormIdMapping.get(data.getModelId()));
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
                    customFormDataMapper.insert(data);
                } catch (Exception e) {
                    AsyncTaskManager.addErrMessage("导入自定义及表单数据失败：" + data.getModelId() + "-" + data.getDataId());
                    log.error("Import custom form data failed: {} - {}", data.getModelId(), data.getDataId(), e);
                }
            }
        });
    }
}
