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
package com.chestnut.contentcore.template.tag;

import com.chestnut.common.staticize.FreeMarkerUtils;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.staticize.enums.TagAttrDataType;
import com.chestnut.common.staticize.tag.AbstractTag;
import com.chestnut.common.staticize.tag.TagAttr;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.IInternalDataType;
import com.chestnut.contentcore.core.InternalURL;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.properties.EnableSSIProperty;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.service.ITemplateService;
import com.chestnut.contentcore.util.ContentCoreUtils;
import com.chestnut.contentcore.util.PageWidgetUtils;
import com.chestnut.contentcore.util.SiteUtils;
import com.chestnut.contentcore.util.TemplateUtils;
import freemarker.core.Environment;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
@Component
public class CmsSlotTag extends AbstractTag {

	public final static String TAG_NAME = "cms_slot";
	public final static String NAME = "{FREEMARKER.TAG." + TAG_NAME + ".NAME}";
	public final static String DESC = "{FREEMARKER.TAG." + TAG_NAME + ".DESC}";
	public final static String ATTR_USAGE_CODE = "{FREEMARKER.TAG." + TAG_NAME + ".type}";
    public final static String ATTR_USAGE_ID = "{FREEMARKER.TAG." + TAG_NAME + ".id}";
	public final static String ATTR_USAGE_SSI = "{FREEMARKER.TAG." + TAG_NAME + ".ssi}";

	final static String ATTR_TYPE = "type";

    final static String ATTR_ID = "id";

	final static String ATTR_SSI = "ssi";
	
	private final ISiteService siteService;

	private final ITemplateService templateService;

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

    @Override
    public String supportVersion() {
        return "v1.5.10+";
    }

    @Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = new ArrayList<>();
		tagAttrs.add(new TagAttr(ATTR_TYPE, true, TagAttrDataType.STRING, ATTR_USAGE_CODE));
        tagAttrs.add(new TagAttr(ATTR_ID, true, TagAttrDataType.STRING, ATTR_USAGE_ID));
		tagAttrs.add(new TagAttr(ATTR_SSI, false, TagAttrDataType.BOOLEAN, ATTR_USAGE_SSI, Boolean.TRUE.toString()));
		return tagAttrs;
	}

	@Override
	public Map<String, TemplateModel> execute0(Environment env, Map<String, String> attrs)
			throws TemplateException, IOException {
		TemplateContext context = FreeMarkerUtils.getTemplateContext(env);

		String type = attrs.get(ATTR_TYPE);
        IInternalDataType idt = ContentCoreUtils.getInternalDataType(type);
        Assert.notNull(idt, () -> new TemplateException(StringUtils.messageFormat("Unsupported data type: type={0}.", type), env));

        if (!idt.supportSlot()) {
            throw new TemplateException(StringUtils.messageFormat("Tag attr[type={0}] not support slot.", type), env);
        }

        Long dataId = MapUtils.getLong(attrs, ATTR_ID);
        Assert.isTrue(IdUtils.validate(dataId), () -> new TemplateException("Invalid data id.", env));

        long siteId = TemplateUtils.evalSiteId(env);
		CmsSite site = this.siteService.getSite(siteId);

        IInternalDataType.RequestData data = new IInternalDataType.RequestData(dataId, 1, context.getPublishPipeCode(),
                context.isPreview(), Map.of());

		boolean ssi = MapUtils.getBoolean(attrs, ATTR_ID, EnableSSIProperty.getValue(site.getConfigProps()));
		if (context.isPreview()) {
			env.getOut().write(this.processTemplate(env, idt, data));
		} else {
			String siteRoot = SiteUtils.getSiteRoot(site, context.getPublishPipeCode());
			String staticFilePath = idt.getStaticPath(dataId, context.getPublishPipeCode());

            String cacheKey = TemplateUtils.getStaticCacheKey(site.getSiteId()
                + ":" + context.getPublishPipeCode()  + ":" + type + ":" + dataId);
			String staticContent = templateService.getTemplateStaticContentCache(cacheKey);
			if (Objects.isNull(staticContent) || !new File(siteRoot + staticFilePath).exists()) {
				staticContent = this.processTemplate(env, idt, data);
				this.templateService.setTemplateStaticContentCache(cacheKey, staticContent);
				if (ssi) {
					FileUtils.writeStringToFile(new File(siteRoot + staticFilePath), staticContent, StandardCharsets.UTF_8);
				}
			}
			if (ssi) {
				String prefix = CmsIncludeTag.getIncludePathPrefix(context.getPublishPipeCode(), site);
				env.getOut().write(StringUtils.messageFormat(CmsIncludeTag.SSI_INCLUDE_TAG, prefix + staticFilePath));
			} else {
				env.getOut().write(staticContent);
			}
		}
		return null;
	}
	
	private String processTemplate(Environment env, IInternalDataType idt, IInternalDataType.RequestData data)
			throws TemplateException {
		Writer out = env.getOut();
		try (StringWriter writer = new StringWriter()) {
			env.setOut(writer);
            idt.processPageData(data, writer);
			return writer.toString();
		} catch(Exception e) {
            throw new TemplateException(e, env);
        } finally {
			env.setOut(out);
		}
	}
}
