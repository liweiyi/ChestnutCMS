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
package com.chestnut.contentcore.util;

import com.chestnut.common.storage.local.LocalFileStorageType;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.IResourceType;
import com.chestnut.contentcore.core.impl.ResourceType_File;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.properties.FileStorageArgsProperty;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ResourceUtils {

    public static final Pattern ImgHtmlTagPattern = Pattern.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);

    private static final Pattern IUrlTagPattern = Pattern.compile("<[^>]+iurl\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);

    private static final Pattern TagAttrSrcPattern = Pattern.compile("src\\s*=\\s*['\"]([^'\"]+)['\"]",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);

    private static final Pattern TagAttrHrefPattern = Pattern.compile("href\\s*=\\s*['\"]([^'\"]+)['\"]",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);

    private static final Pattern TagAttrIUrlPattern = Pattern.compile("iurl\\s*=\\s*['\"]([^'\"]+)['\"]",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);

    /**
     * 通过文件后缀名获取对应资源类型，由于文件资源包含所有文件类型，再获取到超过1个资源类型时将文件资源类型移除再获取。
     */
    public static IResourceType getResourceTypeBySuffix(String suffix) {
        List<IResourceType> list = ContentCoreUtils.getResourceTypes().stream().filter(rt -> rt.check(suffix)).toList();
        if (list.size() > 1) {
            for (IResourceType rt : list) {
                if (!ResourceType_File.ID.equals(rt.getId())) {
                    return rt;
                }
            }
        }
        return list.get(0);
    }

    public static String getResourcePrefix(String storageType, CmsSite site, String publishPipeCode, boolean isPreview) {
        if (LocalFileStorageType.TYPE.equals(storageType)) {
            return SiteUtils.getResourcePrefix(site, publishPipeCode, isPreview);
        }
        FileStorageArgsProperty.FileStorageArgs fileStorageArgs = FileStorageArgsProperty.getValue(site.getConfigProps());
        if (fileStorageArgs != null && StringUtils.isNotEmpty(fileStorageArgs.getDomain())) {
            String domain = fileStorageArgs.getDomain();
            if (!domain.endsWith("/")) {
                domain += "/";
            }
            return domain;
        }
        // 无法获取到对象存储访问地址时默认使用站点资源域名
        return site.getResourceUrl();
    }

    /**
     * 处理html中包含iurl属性的标签，移除iurl属性，如果标签中包含src或者href属性则替换成iurl属性值
     */
    public static String dealHtmlInternalUrl(String content) {
        if (StringUtils.isBlank(content)) {
            return content;
        }
        Matcher matcher = IUrlTagPattern.matcher(content);
        int lastEndIndex = 0;
        StringBuilder sb = new StringBuilder();
        while (matcher.find(lastEndIndex)) {
            int s = matcher.start();
            sb.append(content, lastEndIndex, s);

            String tagStr = matcher.group();
            String iurl = matcher.group(1);
            // begin
            try {
                // 移除iurl属性
                tagStr = TagAttrIUrlPattern.matcher(tagStr).replaceAll("");
                // 查找src属性，替换成iurl
                Matcher matcherSrc = TagAttrSrcPattern.matcher(tagStr);
                if (matcherSrc.find()) {
                    tagStr = tagStr.substring(0, matcherSrc.start()) + "src=\"" + iurl + "\""
                            + tagStr.substring(matcherSrc.end());
                } else {
                    // 无src属性则继续查找href属性
                    Matcher matcherHref = TagAttrHrefPattern.matcher(tagStr);
                    if (matcherHref.find()) {
                        tagStr = tagStr.substring(0, matcherHref.start()) + "href=\"" + iurl + "\""
                                + tagStr.substring(matcherHref.end());
                    }
                }
            } catch (Exception e) {
                log.warn("InternalUrl parse failed: " + iurl, e);
            }
            // end
            sb.append(tagStr.replace("\\s+", " "));

            lastEndIndex = matcher.end();
        }
        sb.append(content.substring(lastEndIndex));
        return sb.toString();
    }
}
