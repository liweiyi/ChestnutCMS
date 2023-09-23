package com.chestnut.contentcore.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.CmsTemplate;
import com.chestnut.contentcore.domain.dto.TemplateAddDTO;
import com.chestnut.contentcore.domain.dto.TemplateRenameDTO;
import com.chestnut.contentcore.domain.dto.TemplateUpdateDTO;
import com.chestnut.contentcore.template.ITemplateType;

public interface ITemplateService extends IService<CmsTemplate> {

	void clearTemplateStaticContentCache(String templateId);

	/**
	 * 获取模板类型
	 * 
	 * @param typeid
	 * @return
	 */
	ITemplateType getTemplateType(String typeid);

	/**
	 * 获取模板静态化内容缓存，主要区块模板使用
	 * 
	 * @param templateId
	 * @return
	 */
	String getTemplateStaticContentCache(String templateId);

	/**
	 * 缓存模板静态化内容
	 * 
	 * @param templateId 相对resourceRoot路径
	 * @param staticContent
	 */
	void setTemplateStaticContentCache(String templateId, String staticContent);

	/**
	 * 扫描模板目录，创建模板数据库记录
	 * 
	 * @param site
	 */
	void scanTemplates(CmsSite site);

	/**
	 * 保存模板内容
	 * 
	 * @throws IOException 
	 */
	void saveTemplate(CmsTemplate template, TemplateUpdateDTO dto) throws IOException;

	/**
	 * 模板文件重命名
	 *
	 * @param template
	 * @param path
	 * @param remark
	 * @param operator
	 * @throws IOException
	 */
	void renameTemplate(CmsTemplate template, String path, String remark, String operator) throws IOException;

	/**
	 * 获取模板文件
	 * 
	 * @param template
	 * @return
	 */
	File getTemplateFile(CmsTemplate template);

	/**
	 * 查找模板文件
	 * 
	 * @param site
	 * @param templatePath
	 * @param publishPipeCode
	 * @return
	 * @throws FileNotFoundException 
	 */
	File findTemplateFile(CmsSite site, String templatePath, String publishPipeCode);

	/**
	 * 新建模板文件
	 * 
	 * @param dto
	 * @throws IOException
	 */
	void addTemplate(TemplateAddDTO dto) throws IOException;

	/**
	 * 删除模板
	 * @param templateIds
	 * @throws IOException 
	 */
	void deleteTemplates(CmsSite site, List<Long> templateIds) throws IOException;
}
