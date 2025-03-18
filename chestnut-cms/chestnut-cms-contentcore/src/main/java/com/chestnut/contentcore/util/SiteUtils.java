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
package com.chestnut.contentcore.util;

import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.ContentCoreConsts;
import com.chestnut.contentcore.config.CMSConfig;
import com.chestnut.contentcore.core.IInternalDataType;
import com.chestnut.contentcore.core.impl.InternalDataType_Site;
import com.chestnut.contentcore.core.impl.PublishPipeProp_PrefixMode;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.enums.SitePrefixMode;
import com.chestnut.system.fixed.config.BackendContext;

public class SiteUtils {

    /**
     * 获取站点指定发布通道根目录路径，静态化文件目录
     *
     * @param site 站点
     * @param publishPipeCode 发布通道编码
     * @return 站点发布通道绝对路径
     */
    public static String getSiteRoot(CmsSite site, String publishPipeCode) {
        return CMSConfig.getResourceRoot() + getSitePublishPipePath(site.getPath(), publishPipeCode);
    }

    /**
     * 获取站点发布通道访问链接前缀
     */
    public static String getPublishPipePrefix(CmsSite site, String publishPipeCode, boolean isPreview) {
        if (isPreview) {
            return CMSConfig.getResourcePreviewPrefix()
                    + getSitePublishPipePath(site.getPath(), publishPipeCode);
        }
        String pathMode = PublishPipeProp_PrefixMode.getValue(publishPipeCode, site.getPublishPipeProps());
        if (SitePrefixMode.isRelative(pathMode)) {
            return "/";
        }
        return site.getUrl(publishPipeCode);
    }

    /**
     * 站点发布通道相对资源根目录路径 = 站点目录名 + "_" + 发布通道编码 + "/"
     *
     * @param sitePath 站点目录名
     * @param publishPipeCode 发布通道编码
     */
    public static String getSitePublishPipePath(String sitePath, String publishPipeCode) {
        return sitePath + "_" + publishPipeCode + StringUtils.SLASH;
    }

    /**
     * 获取站点资源文件根目录
     *
     * @param sitePath 站点目录名
     */
    public static String getSiteResourceRoot(String sitePath) {
        return CMSConfig.getResourceRoot() + getSiteResourcePath(sitePath);
    }

    public static String getSiteResourceRoot(CmsSite site) {
        return getSiteResourceRoot(site.getPath());
    }

    public static String getResourcePreviewPrefix(CmsSite site) {
        return CMSConfig.getResourcePreviewPrefix() + getSiteResourcePath(site.getPath());
    }

    /**
     * 获取站点资源文件访问链接前缀
     *
     * @param site 站点
     * @param publishPipeCode 发布通道编码
     * @param isPreview 是否预览模式
     */
    public static String getResourcePrefix(CmsSite site, String publishPipeCode, boolean isPreview) {
        if (isPreview) {
            return getResourcePreviewPrefix(site);
        }
        if (StringUtils.isEmpty(site.getResourceUrl())) {
            return getPublishPipePrefix(site, publishPipeCode, false);
        }
        return site.getResourceUrl();
    }

    /**
     * 获取站点资源相对资源根目录路径
     *
     * @param sitePath 站点目录名
     */
    public static String getSiteResourcePath(String sitePath) {
        return sitePath + StringUtils.SLASH;
    }

    /**
     * 获取站点访问链接
     *
     * @param site 站点
     * @param publishPipeCode 发布通道编码
     * @param isPreview 是否预览模式
     */
    public static String getSiteLink(CmsSite site, String publishPipeCode, boolean isPreview) {
        if (isPreview) {
			String previewPath = IInternalDataType.getPreviewPath(InternalDataType_Site.ID,
					site.getSiteId(), publishPipeCode);
			return BackendContext.getValue() + previewPath;
        }
        return site.getUrl(publishPipeCode);
    }
    
    /**
	 * 获取模板相对项目资源根目录（ResourceRoot）的路径
	 * 
	 * @param site 站点
	 * @param publishPipeCode 发布通道编码
     * @param template 模板文件路径，相对于站点模板目录路径
	 */
    public static String getTemplateKey(CmsSite site, String publishPipeCode, String template) {
    	return site.getPath() + "_" + publishPipeCode + StringUtils.SLASH + ContentCoreConsts.TemplateDirectory + template;
    }
    
    public static String getSiteTemplatePath(String sitePath, String publishPipeCode) {
    	return sitePath + "_" + publishPipeCode + StringUtils.SLASH + ContentCoreConsts.TemplateDirectory; 
    }
}
