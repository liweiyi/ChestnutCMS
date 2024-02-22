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
package com.chestnut.exmodel.listener;

import com.chestnut.common.utils.NumberUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.IContent;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.vo.ContentVO;
import com.chestnut.contentcore.listener.event.*;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.exmodel.CmsExtendMetaModelType;
import com.chestnut.exmodel.domain.CmsExtendModelData;
import com.chestnut.exmodel.fixed.dict.ExtendModelDataType;
import com.chestnut.exmodel.properties.CatalogExtendModelProperty;
import com.chestnut.exmodel.properties.ContentExtendModelProperty;
import com.chestnut.exmodel.properties.SiteExtendModelProperty;
import com.chestnut.xmodel.service.IModelDataService;
import com.chestnut.xmodel.util.XModelUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class EXModelEventListener {

	private final IModelDataService modelDataService;

	private final ICatalogService catalogService;

	@EventListener
	public void afterContentEditorInit(AfterContentEditorInitEvent event) {
		ContentVO vo = event.getContentVO();
		CmsCatalog catalog = this.catalogService.getCatalog(vo.getCatalogId());
		String modelId = ContentExtendModelProperty.getValue(catalog.getConfigProps());
		if (NumberUtils.isDigits(modelId)) {
			vo.getCatalogConfigProps().put(ContentExtendModelProperty.ID, modelId);
		}
	}

	@EventListener
	public void afterSiteSave(AfterSiteSaveEvent event) {
		String modelId = SiteExtendModelProperty.getValue(event.getSite().getConfigProps());
		if (NumberUtils.isDigits(modelId)) {
			String dataId = event.getSite().getSiteId().toString();
			this.saveModelData(Long.valueOf(modelId), ExtendModelDataType.SITE, dataId, event.getSiteDTO().getParams());
		}
	}

	@EventListener
	public void afterSiteDelete(AfterSiteDeleteEvent event) {
		String modelId = SiteExtendModelProperty.getValue(event.getSite().getConfigProps());
		if (NumberUtils.isDigits(modelId)) {
			String dataId = event.getSite().getSiteId().toString();
			this.modelDataService.deleteModelDataByPkValue(Long.valueOf(modelId),
					List.of(Map.of(
							CmsExtendMetaModelType.FIELD_MODEL_ID.getCode(), modelId,
							CmsExtendMetaModelType.FIELD_DATA_TYPE.getCode(), ExtendModelDataType.SITE,
							CmsExtendMetaModelType.FIELD_DATA_ID.getCode(), dataId
					)));
		}
	}

	@EventListener
	public void afterCatalogSave(AfterCatalogSaveEvent event) {
		String modelId = CatalogExtendModelProperty.getValue(event.getCatalog().getConfigProps());
		if (NumberUtils.isDigits(modelId)) {
			String dataId = event.getCatalog().getCatalogId().toString();
			this.saveModelData(Long.valueOf(modelId), ExtendModelDataType.CATALOG, dataId, event.getExtendParams());
		}
	}

	@EventListener
	public void afterCatalogDelete(AfterCatalogDeleteEvent event) {
		String modelId = CatalogExtendModelProperty.getValue(event.getCatalog().getConfigProps());
		if (NumberUtils.isDigits(modelId)) {
			String dataId = event.getCatalog().getCatalogId().toString();
			this.modelDataService.deleteModelDataByPkValue(Long.valueOf(modelId),
					List.of(Map.of(
							CmsExtendMetaModelType.FIELD_MODEL_ID.getCode(), modelId,
							CmsExtendMetaModelType.FIELD_DATA_TYPE.getCode(), ExtendModelDataType.CATALOG,
							CmsExtendMetaModelType.FIELD_DATA_ID.getCode(), dataId
					)));
		}
	}

	@EventListener
	public void afterContentSave(AfterContentSaveEvent event) {
		IContent<?> content = event.getContent();
		String modelId = ContentExtendModelProperty.getValue(content.getCatalog().getConfigProps());
		if (NumberUtils.isDigits(modelId)) {
			String dataId = String.valueOf(content.getContentEntity().getContentId());
			this.saveModelData(Long.valueOf(modelId), ExtendModelDataType.CONTENT, dataId, content.getParams());
		}
	}

	@EventListener
	public void afterContentDelete(AfterContentDeleteEvent event) {
		IContent<?> content = event.getContent();
		String modelId = ContentExtendModelProperty.getValue(content.getCatalog().getConfigProps());
		if (NumberUtils.isDigits(modelId)) {
			String dataId = String.valueOf(content.getContentEntity().getContentId());
			this.modelDataService.deleteModelDataByPkValue(Long.valueOf(modelId),
					List.of(Map.of(
							CmsExtendMetaModelType.FIELD_MODEL_ID.getCode(), modelId,
							CmsExtendMetaModelType.FIELD_DATA_TYPE.getCode(), ExtendModelDataType.CONTENT,
							CmsExtendMetaModelType.FIELD_DATA_ID.getCode(), dataId
					)));
		}
	}

	private void saveModelData(Long modelId, String dataType, String dataId, Map<String, Object> params) {
		Map<String, Object> dataMap = new HashMap<>();
		params.entrySet().forEach(e -> {
			String fieldCode = e.getKey();
			if (fieldCode.startsWith(CmsExtendMetaModelType.DATA_FIELD_PREFIX)) {
				fieldCode = StringUtils.substringAfter(e.getKey(), CmsExtendMetaModelType.DATA_FIELD_PREFIX);
			}
			dataMap.put(fieldCode, e.getValue());
		});
		dataMap.put(CmsExtendMetaModelType.FIELD_MODEL_ID.getCode(), modelId);
		dataMap.put(CmsExtendMetaModelType.FIELD_DATA_TYPE.getCode(), dataType);
		dataMap.put(CmsExtendMetaModelType.FIELD_DATA_ID.getCode(), dataId);
		this.modelDataService.saveModelData(modelId, dataMap);
	}
}
