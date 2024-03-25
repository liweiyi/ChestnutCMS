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
package com.chestnut.common.staticize;

import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.staticize.func.IFunction;
import com.chestnut.common.staticize.tag.ITag;
import com.chestnut.common.utils.Assert;
import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModelException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

@Slf4j
@Service
public class StaticizeService {

	private final Configuration cfg;
	
	public StaticizeService(@Qualifier("staticizeConfiguration") Configuration cfg, List<ITag> tags,
			List<IFunction> functions) {
		this.cfg = cfg;
		tags.forEach(this::registerTag);
		functions.forEach(this::registerFunction);
	}

	/**
	 * 处理模板，静态化结果写入StringWriter
	 * 
	 * <p>
	 * 此方法只返回TemplateContext中制定页码的静态化结果
	 * </p>
	 * 
	 * @param context
	 * @param writer
	 * @throws TemplateException
	 * @throws IOException
	 */
	public void process(TemplateContext context, Writer writer) throws TemplateException, IOException {
		// 设置模板
		Template template = cfg.getTemplate(context.getTemplateId());
		// 处理模板
		long s = System.currentTimeMillis();
		context.setTimeMillis(s);
		Environment env = template.createProcessingEnvironment(context.getVariables(), writer);
		FreeMarkerUtils.addGlobalVariables(env, context);
		env.process();
		log.debug("[{}], page：{}, cost: {}ms", context.getTemplateId(), context.getPageIndex(),
				System.currentTimeMillis() - s);
	}

	/**
	 * 生成静态化文件，自动处理分页
	 * 
	 * @param context
	 * @throws TemplateException
	 * @throws IOException
	 */
	public void process(TemplateContext context) throws TemplateException, IOException {
		// 设置模板
		Template template = cfg.getTemplate(context.getTemplateId());
		// 处理模板
		long s = System.currentTimeMillis();
		context.setTimeMillis(s);
		Environment env = null;
		String filePath = context.getStaticizeFilePath(context.getPageIndex());
		try (FileWriter writer = new FileWriter(filePath)) {
			env = template.createProcessingEnvironment(context.getVariables(), writer);
			FreeMarkerUtils.addGlobalVariables(env, context);
			// 生成静态化文件
			env.process();
		} catch (Exception e) {
			throw new TemplateException(e, env);
		}
		log.debug("[{}], page：{}, cost: {}ms", filePath, context.getPageIndex(), System.currentTimeMillis() - s);

		// 发布分页
		while (context.hasNextPage()) {
			s = System.currentTimeMillis();
			context.setPageIndex(context.getPageIndex() + 1);
			env.setGlobalVariable(StaticizeConstants.TemplateVariable_PageNo,
					env.getObjectWrapper().wrap(context.getPageIndex()));
			filePath = context.getStaticizeFilePath(context.getPageIndex());
			try (FileWriter writer = new FileWriter(filePath)) {
				env.setOut(writer);
				env.process();
			} catch (Exception e) {
				throw new TemplateException(e, env);
			}
			log.debug("[{}], page：{}, cost: {}ms", filePath, context.getPageIndex(), System.currentTimeMillis() - s);
		}
	}

	public void clearTemplateCache() {
		cfg.clearTemplateCache();
	}

	public void registerTag(ITag tag) {
		// 注册模板标签
		try {
			Assert.isNull(cfg.getSharedVariable(tag.getTagName()),
					() -> new IllegalArgumentException("Freemarker directive conflict: " + tag.getTagName()));
			cfg.setSharedVariable(tag.getTagName(), tag);
		} catch (TemplateModelException e) {
			log.error("Freemarker directive '<@{}>' register failed.", tag.getTagName());
			throw new IllegalArgumentException(e);
		}
		log.info("Freemarker directive: <@{}>", tag.getTagName());
	}

	public void registerFunction(IFunction func) {
		// 注册模板函数
		try {
			Assert.isNull(cfg.getSharedVariable(func.getFuncName()),
					() -> new IllegalArgumentException("Freemarker function conflict: " + func.getFuncName()));
			cfg.setSharedVariable(func.getFuncName(), func);
			if (!func.getAliases().isEmpty()) {
				for (String alias : func.getAliases()) {
					Assert.isNull(cfg.getSharedVariable(alias),
							() -> new IllegalArgumentException("Freemarker function alias conflict: " + alias));
					cfg.setSharedVariable(alias, func);
				}
			}
		} catch (TemplateModelException e) {
			log.error("Freemarker function '{}' register failed.", func.getFuncName());
			throw new IllegalArgumentException(e);
		}
		log.info("Freemarker function: {}", func.getFuncName());
	}
}
