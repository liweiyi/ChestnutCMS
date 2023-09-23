package com.chestnut.article.service.impl;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.chestnut.contentcore.domain.*;
import com.chestnut.contentcore.service.*;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.article.domain.CmsArticleDetail;
import com.chestnut.article.mapper.CmsArticleDetailMapper;
import com.chestnut.article.service.IArticleService;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.util.InternalUrlUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl extends ServiceImpl<CmsArticleDetailMapper, CmsArticleDetail>
		implements IArticleService {

	/**
	 * 带src属性的图片标签匹配
	 */
	public static final Pattern ImgHtmlTagPattern = Pattern.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);

    /**
     * 带iurl属性的任意标签匹配
     */
	private static final Pattern IUrlTagPattern = Pattern.compile("<[^>]+iurl\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);

	private static final Pattern TagAttrSrcPattern = Pattern.compile("src\\s*=\\s*['\"]([^'\"]+)['\"]",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);

	private static final Pattern TagAttrHrefPattern = Pattern.compile("href\\s*=\\s*['\"]([^'\"]+)['\"]",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);

	private static final Pattern TagAttrIUrlPattern = Pattern.compile("iurl\\s*=\\s*['\"]([^'\"]+)['\"]",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);

	private final IResourceService resourceService;

	private final IPageWidgetService pageWidgetService;

	private final ISiteService siteService;

	private final ICatalogService catalogService;

	private final IContentService contentService;

	@Override
	public String saveInternalUrl(String content) {
		if (StringUtils.isBlank(content)) {
			return content;
		}
		Matcher matcher = IUrlTagPattern.matcher(content);
		int lastEndIndex = 0;
		StringBuilder sb = new StringBuilder();
		while (matcher.find(lastEndIndex)) {
			int s = matcher.start();
			sb.append(content.substring(lastEndIndex, s));

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

	/**
	 * 下载远程图片前要先调用saveInternalUrl方法处理所有标签的iurl
	 */
	@Override
	public String downloadRemoteImages(String content, CmsSite site, String operator) {
		if (StringUtils.isBlank(content)) {
			return content;
		}
		Matcher matcher = ImgHtmlTagPattern.matcher(content);
		int lastEndIndex = 0;
		StringBuilder sb = new StringBuilder();
		while (matcher.find(lastEndIndex)) {
			int s = matcher.start();
			sb.append(content.substring(lastEndIndex, s));

			String imgTag = matcher.group();
			String src = matcher.group(1);
			try {
				if (StringUtils.startsWithIgnoreCase(src, "data:image/")) {
					// base64图片保存到资源库
					CmsResource resource = resourceService.addBase64Image(site, operator, src);
					if (Objects.nonNull(resource)) {
						imgTag = imgTag.replaceFirst("data:image/([^'\"]+)", src);
					}
				} else if (!InternalUrlUtils.isInternalUrl(src) && ServletUtils.isHttpUrl(src)) {
					// 非iurl的http链接则下载图片
					CmsResource resource = resourceService.downloadImageFromUrl(src, site.getSiteId(), operator);
					if (Objects.nonNull(resource)) {
						imgTag = StringUtils.replaceEx(imgTag, src, resource.getInternalUrl());
					}
				}
			} catch (Exception e1) {
				String imgSrc = (src.startsWith("data:image/") ? src.substring(0, 20) : src);
				log.warn("Save image failed: " + imgSrc);
				AsyncTaskManager.addErrMessage("Download remote image failed: " + imgSrc);
			}
			sb.append(imgTag);
			lastEndIndex = matcher.end();
		}
		sb.append(content.substring(lastEndIndex));
		return sb.toString();
	}
}
