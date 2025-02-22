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

import com.chestnut.common.staticize.StaticizeConstants;
import com.chestnut.common.staticize.enums.TagAttrDataType;
import com.chestnut.common.staticize.exception.InvalidTagAttrValueException;
import com.chestnut.common.staticize.tag.AbstractTag;
import com.chestnut.common.staticize.tag.TagAttr;
import com.chestnut.common.staticize.tag.TagAttrOption;
import com.chestnut.common.utils.ConvertUtils;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.exmodel.CmsExtendMetaModelType;
import com.chestnut.xmodel.core.IMetaControlType;
import com.chestnut.xmodel.core.MetaModel;
import com.chestnut.xmodel.service.IModelDataService;
import com.chestnut.xmodel.service.IModelService;
import freemarker.core.Environment;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CmsXModelDataTag extends AbstractTag {

	public final static String TAG_NAME = "cms_xmodel_data";
	public final static String NAME = "{FREEMARKER.TAG." + TAG_NAME + ".NAME}";
	public final static String DESC = "{FREEMARKER.TAG." + TAG_NAME + ".DESC}";
	public final static String ATTR_USAGE_MODEL_ID = "{FREEMARKER.TAG." + TAG_NAME + ".modelId}";
	public final static String ATTR_USAGE_DATA_TYPE = "{FREEMARKER.TAG." + TAG_NAME + ".dataType}";
	public final static String ATTR_USAGE_DATA_ID = "{FREEMARKER.TAG." + TAG_NAME + ".dataId}";
	public final static String ATTR_OPTION_DATA_TYPE_SITE = "{FREEMARKER.TAG." + TAG_NAME + ".dataType.site}";
	public final static String ATTR_OPTION_DATA_TYPE_CATALOG = "{FREEMARKER.TAG." + TAG_NAME + ".dataType.catalog}";
	public final static String ATTR_OPTION_DATA_TYPE_CONTENT = "{FREEMARKER.TAG." + TAG_NAME + ".dataType.content}";

	public final static String ATTR_MODEL_ID = "modelId";
	public final static String ATTR_DATA_TYPE = "dataType";
	public final static String ATTR_DATA_ID = "dataId";

	private final IModelService modelService;
	private final IModelDataService modelDataService;
	private final Map<String, IMetaControlType> controlTypeMap;

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = new ArrayList<>();
		tagAttrs.add(new TagAttr(ATTR_MODEL_ID, true, TagAttrDataType.INTEGER, ATTR_USAGE_MODEL_ID));
		tagAttrs.add(new TagAttr(ATTR_DATA_TYPE, true, TagAttrDataType.STRING, ATTR_USAGE_DATA_TYPE, XModelDataTagType.toTagAttrOptions()));
		tagAttrs.add(new TagAttr(ATTR_DATA_ID, true, TagAttrDataType.STRING, ATTR_USAGE_DATA_ID));
		return tagAttrs;
	}

	@Override
	public Map<String, TemplateModel> execute0(Environment env, Map<String, String> attrs)
			throws TemplateException {
		long modelId = MapUtils.getLongValue(attrs, ATTR_MODEL_ID);
		if (!IdUtils.validate(modelId)) {
			throw new InvalidTagAttrValueException(getTagName(), ATTR_MODEL_ID, String.valueOf(modelId), env);
		}
		String dataType = MapUtils.getString(attrs, ATTR_DATA_TYPE);
		String dataId = MapUtils.getString(attrs, ATTR_DATA_ID);
		Map<String, Object> modelData = this.modelDataService.getModelDataByPkValue(modelId,
				Map.of(
						CmsExtendMetaModelType.FIELD_MODEL_ID.getCode(), modelId,
						CmsExtendMetaModelType.FIELD_DATA_TYPE.getCode(), dataType,
						CmsExtendMetaModelType.FIELD_DATA_ID.getCode(), dataId
				));
		MetaModel model = this.modelService.getMetaModel(modelId);
		modelData.entrySet().forEach(entry -> {
			model.getFields().stream().filter(field -> field.getCode().equals(entry.getKey()))
					.findFirst().ifPresent(field -> {
						IMetaControlType controlType = controlTypeMap.get(IMetaControlType.BEAN_PREFIX + field.getControlType());
						if (controlType != null) {
							Object v = controlType.stringAsValue(ConvertUtils.toStr(entry.getValue()));
							entry.setValue(v);
						}
					});
		});
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
		site(ATTR_OPTION_DATA_TYPE_SITE),
		// 当前站点
		catalog(ATTR_OPTION_DATA_TYPE_CATALOG),
		// 子站点
		content(ATTR_OPTION_DATA_TYPE_CONTENT);

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
