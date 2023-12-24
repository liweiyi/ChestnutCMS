package com.chestnut.cms.search.impl;

import com.chestnut.cms.search.publishpipe.PublishPipeProp_SearchTemplate;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.IDynamicPageType;
import com.chestnut.contentcore.util.TemplateUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 搜索结果页
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IDynamicPageType.BEAN_PREFIX + SearchDynamicPageType.TYPE)
public class SearchDynamicPageType implements IDynamicPageType {

    public static final String TYPE = "Search";

    public static final String REQUEST_PATH = "_search";

    public static final List<RequestArg> REQUEST_ARGS =  List.of(
            REQUEST_ARG_SITE_ID,
            REQUEST_ARG_PUBLISHPIPE_CODE,
            REQUEST_ARG_PREVIEW,
            new RequestArg("q", "搜索词", RequestArgType.Parameter, false, null),
            new RequestArg("cid", "栏目ID", RequestArgType.Parameter, false, null),
            new RequestArg("ot", "是否只搜索标题", RequestArgType.Parameter, false, "false"),
            new RequestArg("ct", "内容类型", RequestArgType.Parameter, false, null),
            new RequestArg("page", "当前页码", RequestArgType.Parameter, false, "1")
        );

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getName() {
        return "搜索结果页";
    }

    @Override
    public String getRequestPath() {
        return REQUEST_PATH;
    }

    @Override
    public List<RequestArg> getRequestArgs() {
        return REQUEST_ARGS;
    }

    @Override
    public String getPublishPipeKey() {
        return PublishPipeProp_SearchTemplate.KEY;
    }

    @Override
    public void initTemplateData(Map<String, String> parameters, TemplateContext templateContext) {
        templateContext.getVariables().put("Request", ServletUtils.getParameters());
        String link = "_search?q=" + parameters.get("q");
        if (templateContext.isPreview()) {
            link += "&sid=" + parameters.get("sid") + "&pp=" + templateContext.getPublishPipeCode() + "&preview=true";
        }
        boolean onlyTitle = MapUtils.getBooleanValue(parameters, "ot", false);
        if (onlyTitle) {
            link += "&ot=true";
        }
        String contentType = parameters.get("ct");
        if (StringUtils.isNotEmpty(contentType)) {
            link += "&ct=" + contentType;
        }
        String catalogId = parameters.get("cid");
        if (StringUtils.isNotEmpty(catalogId)) {
            link += "&cid=" + catalogId;
        }
        if (templateContext.isPreview()) {
            link = TemplateUtils.appendTokenParameter(link);
        }
        templateContext.setPageIndex(MapUtils.getIntValue(parameters, "page", 1));
        templateContext.setFirstFileName(link);
        templateContext.setOtherFileName(link + "&page=" + TemplateContext.PlaceHolder_PageNo);
    }
}
