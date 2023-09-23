package com.chestnut.exmodel.template.tag;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.staticize.enums.TagAttrDataType;
import com.chestnut.common.staticize.tag.AbstractListTag;
import com.chestnut.common.staticize.tag.TagAttr;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.xmodel.domain.XModel;
import com.chestnut.xmodel.domain.XModelField;
import com.chestnut.xmodel.service.IModelFieldService;
import com.chestnut.xmodel.service.IModelService;

import freemarker.core.Environment;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CmsXModelFieldTag extends AbstractListTag {

	public final static String TAG_NAME = "cms_xmodel_field";
	public final static String NAME = "{FREEMARKER.TAG.NAME." + TAG_NAME + "}";
	public final static String DESC = "{FREEMARKER.TAG.DESC." + TAG_NAME + "}";
	
	public final static String TagAttr_ModelId = "modelid";

	private final IModelService modelService;

	private final IModelFieldService modelFieldService;
	
	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = super.getTagAttrs();
		tagAttrs.add(new TagAttr(TagAttr_ModelId, true, TagAttrDataType.INTEGER, "模型ID"));
		return tagAttrs;
	}

	@Override
	public TagPageData prepareData(Environment env, Map<String, String> attrs, boolean page, int size, int pageIndex) throws TemplateException {
		long modelId = MapUtils.getLongValue(attrs, TagAttr_ModelId, 0);
		if (modelId <= 0) {
			throw new TemplateException("扩展模型数据ID错误：" + modelId, env);
		}
		XModel xmodel = this.modelService.getById(modelId);
		if (xmodel == null) {
			throw new TemplateException("扩展模型数据未找到：" + modelId, env);
		}
		
		LambdaQueryWrapper<XModelField> q = new LambdaQueryWrapper<>();
		q.eq(XModelField::getModelId, modelId);
		String condition = MapUtils.getString(attrs, TagAttr.AttrName_Condition);
		q.apply(StringUtils.isNotEmpty(condition), condition);
		Page<XModelField> pageResult = this.modelFieldService.page(new Page<>(pageIndex, size, page), q);
		if (pageIndex > 1 & pageResult.getRecords().size() == 0) {
			throw new TemplateException("内容列表页码超出上限：" + pageIndex, env);
		}
		return TagPageData.of(pageResult.getRecords(), pageResult.getTotal());
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
		return DESC; // 获取模型字段列表数据，内嵌<#list DataList as field>${field.name}</#list>遍历数据，可内嵌于<@cms_xmodel_data>使用${ModelData[field.fieldName]}获取字段数据
	}
}
