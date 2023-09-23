package com.chestnut.cms.member.utils;

import com.chestnut.common.staticize.FreeMarkerUtils;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.utils.StringUtils;
import freemarker.core.Environment;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateModelException;

/**
 * <TODO description class purpose>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class CmsMemberUtils {

    public static String getAccountUrl(Long memberId, String type, Environment env, boolean includeBaseArgs) throws TemplateModelException {
        String url;
        Long siteId = FreeMarkerUtils.evalLongVariable(env, "Site.siteId");
        TemplateContext context = FreeMarkerUtils.getTemplateContext(env);
        if (context.isPreview()) {
            url = FreeMarkerUtils.evalStringVariable(env, "ApiPrefix")
                    + "account/" + memberId + "?sid=" + siteId + "&pp=" + context.getPublishPipeCode() + "&preview=true";
        } else {
            url = FreeMarkerUtils.evalStringVariable(env, "Prefix") + "account/" + memberId;
            if (includeBaseArgs) {
                url += "?sid=" + siteId + "&pp=" + context.getPublishPipeCode();
            }
        }
        if (StringUtils.isNotEmpty(type)) {
            url += (url.indexOf("?") > -1 ? "&" : "?") + "type=" + type;
        }
        return url;
    }
}
