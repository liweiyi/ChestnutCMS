/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.staticize.enums.TagAttrDataType;
import com.chestnut.common.staticize.exception.InvalidTagAttrValueException;
import com.chestnut.common.staticize.tag.AbstractListTag;
import com.chestnut.common.staticize.tag.TagAttr;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.exmodel.exception.XModelNotFoundTemplateException;
import com.chestnut.xmodel.domain.XModel;
import com.chestnut.xmodel.domain.XModelField;
import com.chestnut.xmodel.service.IModelFieldService;
import com.chestnut.xmodel.service.IModelService;
import freemarker.core.Environment;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CmsXModelFieldTag extends AbstractListTag {

	public final static String TAG_NAME = "cms_xmodel_field";
	public final static String NAME = "{FREEMARKER.TAG." + TAG_NAME + ".NAME}";
	public final static String DESC = "{FREEMARKER.TAG." + TAG_NAME + ".DESC}";
	public final static String ATTR_USAGE_MODEL_ID = "{FREEMARKER.TAG." + TAG_NAME + ".modelId}";
	
	public final static String ATTR_MODEL_ID = "modelId";

	private final IModelService modelService;

	private final IModelFieldService modelFieldService;
	
	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = super.getTagAttrs();
		tagAttrs.add(new TagAttr(ATTR_MODEL_ID, true, TagAttrDataType.INTEGER, ATTR_USAGE_MODEL_ID));
		return tagAttrs;
	}

	@Override
	public TagPageData prepareData(Environment env, Map<String, String> attrs, boolean page, int size, int pageIndex) throws TemplateException {
		long modelId = MapUtils.getLongValue(attrs, ATTR_MODEL_ID);
		if (!IdUtils.validate(modelId)) {
			throw new InvalidTagAttrValueException(getTagName(), ATTR_MODEL_ID, String.valueOf(modelId), env);
		}
		XModel xmodel = this.modelService.getById(modelId);
		Assert.notNull(xmodel, () -> new XModelNotFoundTemplateException(modelId, env));

		LambdaQueryWrapper<XModelField> q = new LambdaQueryWrapper<>();
		q.eq(XModelField::getModelId, modelId);
		String condition = MapUtils.getString(attrs, TagAttr.AttrName_Condition);
		q.apply(StringUtils.isNotEmpty(condition), condition);
		Page<XModelField> pageResult = this.modelFieldService.page(new Page<>(pageIndex, size, page), q);
		return TagPageData.of(pageResult.getRecords(), pageResult.getTotal());
	}

	@Override
	public Class<XModelField> getDataClass() {
		return XModelField.class;
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
}
