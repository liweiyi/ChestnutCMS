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
package com.chestnut.contentcore.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.file.FileExUtils;
import com.chestnut.contentcore.ContentCoreConsts;
import com.chestnut.contentcore.config.CMSConfig;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.CmsTemplate;
import com.chestnut.contentcore.domain.dto.TemplateAddDTO;
import com.chestnut.contentcore.domain.dto.TemplateUpdateDTO;
import com.chestnut.contentcore.exception.ContentCoreErrorCode;
import com.chestnut.contentcore.fixed.config.TemplateSuffix;
import com.chestnut.contentcore.mapper.CmsTemplateMapper;
import com.chestnut.contentcore.service.IPublishPipeService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.service.ITemplateService;
import com.chestnut.contentcore.template.ITemplateType;
import com.chestnut.contentcore.util.SiteUtils;
import com.chestnut.system.SysConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateServiceImpl extends ServiceImpl<CmsTemplateMapper, CmsTemplate> implements ITemplateService {

	private final static String TEMPLATE_STATIC_CONTENT_CACHE_KEY_PREFIX = CMSConfig.CachePrefix + "template:";

	private final Map<String, ITemplateType> templateTypes;

	private final IPublishPipeService publishPipeService;

	private final RedisCache redisCache;

	private final ISiteService siteService;

	@Override
	public String getTemplateStaticContentCache(String templateKey) {
		return this.redisCache.getCacheObject(TEMPLATE_STATIC_CONTENT_CACHE_KEY_PREFIX + templateKey);
	}

	@Override
	public void setTemplateStaticContentCache(String templateKey, String staticContent) {
		this.redisCache.setCacheObject(TEMPLATE_STATIC_CONTENT_CACHE_KEY_PREFIX + templateKey, staticContent, 24,
				TimeUnit.HOURS);
	}

	@Override
	public void clearTemplateStaticContentCache(String templateKey) {
		this.redisCache.deleteObject(TEMPLATE_STATIC_CONTENT_CACHE_KEY_PREFIX + templateKey);
	}

	@Override
	public void clearSiteAllTemplateStaticContentCache(CmsSite site) {
		List<CmsTemplate> dbTemplates = this.lambdaQuery().eq(CmsTemplate::getSiteId, site.getSiteId()).list();
		dbTemplates.forEach(template -> {
			String templateKey = SiteUtils.getTemplateKey(site, template.getPublishPipeCode(), template.getPath());
			clearTemplateStaticContentCache(templateKey);
		});
	}

	@Override
	public ITemplateType getTemplateType(String typeId) {
		return this.templateTypes.get(ITemplateType.BEAN_NAME_PREFIX + typeId);
	}

	/**
	 * 扫描模板目录，创建模板数据库记录
	 */
	@Override
	public void scanTemplates(CmsSite site) {
		List<CmsTemplate> dbTemplates = this.lambdaQuery().eq(CmsTemplate::getSiteId, site.getSiteId()).list();
		this.publishPipeService.getPublishPipes(site.getSiteId()).forEach(pp -> {
			String siteRoot = SiteUtils.getSiteRoot(site, pp.getCode());
			String templateDirectory = siteRoot + ContentCoreConsts.TemplateDirectory;
			// 处理变更模板
			List<File> templateFiles = FileExUtils.loopFiles(templateDirectory,
					f -> f.getName().endsWith(TemplateSuffix.getValue()));
			for (File file : templateFiles) {
				String path = StringUtils.substringAfterLast(FileExUtils.normalizePath(file.getAbsolutePath()),
						ContentCoreConsts.TemplateDirectory);
				Optional<CmsTemplate> opt = dbTemplates.stream()
						.filter(t -> t.getPublishPipeCode().equals(pp.getCode()) && t.getPath().equals(path))
						.findFirst();
				opt.ifPresentOrElse(t -> {
					if (t.getModifyTime() != file.lastModified()) {
						try {
							t.setFilesize(file.length());
							t.setContent(FileUtils.readFileToString(file, StandardCharsets.UTF_8));
							t.setModifyTime(file.lastModified());
							t.updateBy(SysConstants.SYS_OPERATOR);
							updateById(t);
							// 清理include缓存
							this.clearTemplateStaticContentCache(t);
						} catch (IOException e) {
							log.error("Read file content failed: {}", file.getAbsolutePath(), e);
						}
					}
				}, () -> {
					try {
						CmsTemplate t = new CmsTemplate();
						t.setTemplateId(IdUtils.getSnowflakeId());
						t.setSiteId(site.getSiteId());
						t.setPublishPipeCode(pp.getCode());
						t.setPath(path);
						t.setFilesize(file.length());
						t.setContent(FileUtils.readFileToString(file, StandardCharsets.UTF_8));
						t.setModifyTime(file.lastModified());
						t.createBy(SysConstants.SYS_OPERATOR);
						save(t);
					} catch (IOException e) {
						log.error("Read file content failed: {}", file.getAbsolutePath(), e);
					}
				});
			}
			// 处理删除掉的模板
			List<String> templatePaths = templateFiles.stream().map(f -> {
				return StringUtils.substringAfterLast(FileExUtils.normalizePath(f.getAbsolutePath()),
						ContentCoreConsts.TemplateDirectory);
			}).toList();
			List<CmsTemplate> list = this.lambdaQuery().eq(CmsTemplate::getSiteId, site.getSiteId())
					.eq(CmsTemplate::getPublishPipeCode, pp.getCode()).list();
			for (CmsTemplate template : list) {
				if (!templatePaths.contains(template.getPath())) {
					this.removeById(template);
				}
			}

		});
	}

	@Override
	public void renameTemplate(CmsTemplate template, String path, String remark, String operator) throws IOException {
		String newPath = FileExUtils.normalizePath(path);
		if (!template.getPath().equals(newPath)) {
			CmsSite site = this.siteService.getSite(template.getSiteId());
			String siteRoot = SiteUtils.getSiteRoot(site, template.getPublishPipeCode());
			File file = new File(siteRoot + ContentCoreConsts.TemplateDirectory + template.getPath());
			File dest = new File(siteRoot + ContentCoreConsts.TemplateDirectory + newPath);
			FileUtils.moveFile(file, dest);

			template.setPath(newPath);
		}
		template.setRemark(remark);
		template.updateBy(operator);
		this.updateById(template);
	}

	/**
	 * 保存模板内容
	 */
	@Override
	public void saveTemplate(CmsTemplate template, TemplateUpdateDTO dto) throws IOException {
		template.setContent(dto.getContent());
		template.setRemark(dto.getRemark());
		// 变更文件内容
		File file = this.getTemplateFile(template);
		FileExUtils.mkdirs(file.getParentFile().getAbsolutePath());

		FileUtils.writeStringToFile(file, dto.getContent(), StandardCharsets.UTF_8);

		template.setModifyTime(file.lastModified());
		template.updateBy(dto.getOperator().getUsername());
		this.updateById(template);
		// 清理include缓存
		this.clearTemplateStaticContentCache(template);
	}

	@Override
	public void addTemplate(TemplateAddDTO dto) throws IOException {
		CmsTemplate template = new CmsTemplate();
		template.setSiteId(dto.getSiteId());
		template.setPublishPipeCode(dto.getPublishPipeCode());
		template.setPath(FileExUtils.normalizePath(dto.getPath()));
		template.setRemark(dto.getRemark());

		File file = this.getTemplateFile(template);
		if (file.exists()) {
			throw ContentCoreErrorCode.TEMPLATE_PATH_EXISTS.exception();
		}
		FileUtils.writeStringToFile(file, StringUtils.EMPTY, StandardCharsets.UTF_8);

		template.setTemplateId(IdUtils.getSnowflakeId());
		template.setContent(StringUtils.EMPTY);
		template.setModifyTime(file.lastModified());
		template.setFilesize(file.length());
		template.createBy(dto.getOperator().getUsername());
		this.save(template);
	}

	@Override
	public void deleteTemplates(CmsSite site, List<Long> templateIds) throws IOException {
		List<CmsTemplate> templates = this.lambdaQuery()
				.eq(CmsTemplate::getSiteId, site.getSiteId())
				.in(CmsTemplate::getTemplateId, templateIds)
				.list();
		for (CmsTemplate template : templates) {
			File f = this.getTemplateFile(template);
			if (f.exists()) {
				FileUtils.delete(f);
			}
			// 清理include缓存
			this.clearTemplateStaticContentCache(template);
		}
		this.removeByIds(templateIds);
	}

	private void clearTemplateStaticContentCache(CmsTemplate template) {
		CmsSite site = this.siteService.getSite(template.getSiteId());
		String templateKey = SiteUtils.getTemplateKey(site, template.getPublishPipeCode(), template.getPath());
		this.clearTemplateStaticContentCache(templateKey);
	}

	@Override
	public File getTemplateFile(CmsTemplate template) {
		CmsSite site = this.siteService.getSite(template.getSiteId());
		String siteRoot = SiteUtils.getSiteRoot(site, template.getPublishPipeCode());
		File templateFile = new File(siteRoot + ContentCoreConsts.TemplateDirectory + template.getPath());
		if (templateFile.exists() && !templateFile.isFile()) {
			throw ContentCoreErrorCode.TEMPLATE_PATH_EXISTS.exception();
		}
		return templateFile;
	}

	@Override
	public File findTemplateFile(CmsSite site, String templatePath, String publishPipeCode) {
		if (StringUtils.isEmpty(templatePath)) {
			return null;
		}
		String siteRoot = SiteUtils.getSiteRoot(site, publishPipeCode);
		File templateFile = new File(siteRoot + ContentCoreConsts.TemplateDirectory + templatePath);
		if (!templateFile.exists() || !templateFile.isFile()) {
			return null;
		}
		return templateFile;
	}
}
