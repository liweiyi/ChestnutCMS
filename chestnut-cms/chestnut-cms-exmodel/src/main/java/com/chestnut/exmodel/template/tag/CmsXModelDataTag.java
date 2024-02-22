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
package com.chestnut.exmodel.template.tag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.chestnut.common.staticize.StaticizeConstants;
import com.chestnut.common.staticize.tag.TagAttrOption;
import com.chestnut.exmodel.CmsExtendMetaModelType;
import com.chestnut.exmodel.domain.CmsExtendModelData;
import com.chestnut.exmodel.fixed.dict.ExtendModelDataType;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import com.chestnut.common.staticize.enums.TagAttrDataType;
import com.chestnut.common.staticize.tag.AbstractTag;
import com.chestnut.common.staticize.tag.TagAttr;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.xmodel.domain.XModel;
import com.chestnut.xmodel.service.IModelDataService;
import com.chestnut.xmodel.service.IModelService;

import freemarker.core.Environment;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CmsXModelDataTag extends AbstractTag {

	public final static String TAG_NAME = "cms_xmodel_data";
	public final static String NAME = "{FREEMARKER.TAG.NAME." + TAG_NAME + "}";
	public final static String DESC = "{FREEMARKER.TAG.DESC." + TAG_NAME + "}";

	public final static String TagAttr_ModelId = "modelId";

	public final static String TagAttr_Data_Type = "dataType";

	public final static String TagAttr_Data_ID = "dataId";

	private final IModelDataService modelDataService;

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = new ArrayList<>();
		tagAttrs.add(new TagAttr(TagAttr_ModelId, true, TagAttrDataType.INTEGER, "模型ID") );
		tagAttrs.add(new TagAttr(TagAttr_Data_Type, true, TagAttrDataType.STRING, "模型数据类型", XModelDataTagType.toTagAttrOptions(), null));
		tagAttrs.add(new TagAttr(TagAttr_Data_ID, true, TagAttrDataType.STRING, "模型数据ID") );
		return tagAttrs;
	}

	@Override
	public Map<String, TemplateModel> execute0(Environment env, Map<String, String> attrs)
			throws TemplateException, IOException {
		long modelId = MapUtils.getLongValue(attrs, TagAttr_ModelId, 0);
		if (modelId <= 0) {
			throw new TemplateException("扩展模型数据ID错误：" + modelId, env);
		}
		String dataType = MapUtils.getString(attrs, TagAttr_Data_Type);
		if (StringUtils.isEmpty(dataType)) {
			throw new TemplateException("扩展模型数据类型不能为空：" + dataType, env);
		}
		String dataId = MapUtils.getString(attrs, TagAttr_Data_ID);
		if (StringUtils.isEmpty(dataId)) {
			throw new TemplateException("扩展模型数据ID不能为空：" + dataId, env);
		}
		Map<String, Object> modelData = this.modelDataService.getModelDataByPkValue(modelId,
				Map.of(
						CmsExtendMetaModelType.FIELD_MODEL_ID.getCode(), modelId,
						CmsExtendMetaModelType.FIELD_DATA_TYPE.getCode(), dataType,
						CmsExtendMetaModelType.FIELD_DATA_ID.getCode(), dataId
				));
		return Map.of(StaticizeConstants.TemplateVariable_Data, this.wrap(env, modelData));
	}

	@Override
	public String getTagName() {
		return TAG_NAME;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getDescription() {
		return DESC;
	}

	private enum XModelDataTagType {
		// 所有站点
		site("站点"),
		// 当前站点
		catalog("栏目"),
		// 子站点
		content("内容");

		private final String desc;

		XModelDataTagType(String desc) {
			this.desc = desc;
		}

		static List<TagAttrOption> toTagAttrOptions() {
			return List.of(
					new TagAttrOption(site.name(), site.desc),
					new TagAttrOption(catalog.name(), catalog.desc),
					new TagAttrOption(content.name(), content.desc)
			);
		}
	}
}
