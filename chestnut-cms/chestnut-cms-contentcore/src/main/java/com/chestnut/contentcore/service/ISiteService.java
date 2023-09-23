package com.chestnut.contentcore.service;

import java.io.IOException;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.dto.SiteDTO;
import com.chestnut.contentcore.domain.dto.SiteDefaultTemplateDTO;

import jakarta.servlet.http.HttpServletRequest;

public interface ISiteService extends IService<CmsSite> {

	/**
	 * 校验站点名称及目录是否唯一
	 *
	 * @param siteName
	 * @param sitePath
	 * @param siteId
	 * @return
	 */
	public boolean checkSiteUnique(String siteName, String sitePath, Long siteId);

    /**
     * 获取当前站点，保存在token中
     */
    public CmsSite getCurrentSite(HttpServletRequest request);

	/**
	 * 获取站点数据
	 * 
	 * @param siteId
	 * @return
	 */
	public CmsSite getSite(Long siteId);

	/**
	 * 新增站点数据
	 *
	 * @param dto
	 * @return
	 * @throws IOException
	 */
	public CmsSite addSite(SiteDTO dto) throws IOException;
	
	/**
	 * 修改站点数据
	 * 
	 * @param site
	 * @return
	 * @throws IOException
	 */
	public CmsSite saveSite(SiteDTO site) throws IOException;
	
	/**
	 * 删除站点数据
	 * 
	 * @param siteId
	 * @return
	 * @throws IOException
	 */
	public void deleteSite(Long siteId) throws IOException;

	/**
	 * 保存站点默认模板配置
	 * 
	 * @param dto
	 * @return
	 */
	public void saveSiteDefaultTemplate(SiteDefaultTemplateDTO dto);

	/**
	 * 清理站点缓存数据
	 * 
	 * @param siteId
	 */
	void clearCache(long siteId);

	/**
	 * 保存站点扩展配置
	 * 
	 * @param site
	 * @param configs
	 * @param operator
	 */
	void saveSiteExtend(CmsSite site, Map<String, String> configs, String operator);
}
