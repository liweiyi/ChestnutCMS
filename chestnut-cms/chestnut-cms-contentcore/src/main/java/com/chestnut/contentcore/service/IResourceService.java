package com.chestnut.contentcore.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.contentcore.domain.CmsResource;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.dto.ResourceUploadDTO;

import jakarta.servlet.http.HttpServletResponse;

public interface IResourceService extends IService<CmsResource> {

	/**
	 * 上传资源
	 * 
	 * @param resourceFile
	 * @param site
	 * @param name
	 * @param remark
	 * @return 
	 * @throws IOException
	 */
	CmsResource addResource(ResourceUploadDTO dto) throws IOException;

	/**
	 * 添加base64图片资源
	 * 
	 * @param site
	 * @param operator
	 * @param base64Data
	 * @throws IOException
	 */
	CmsResource addBase64Image(CmsSite site, String operator, String base64Data) throws IOException;

	/**
	 * 添加本地文件到资源库
	 *
	 * @param site
	 * @param operator
	 * @param imageFile
	 * @return
	 * @throws IOException
	 */
	CmsResource addImageFromFile(CmsSite site, String operator, File imageFile) throws IOException;

	/**
	 * 删除资源
	 * 
	 * @param resourceIds
	 * @return
	 */
	void deleteResource(List<Long> resourceIds);

	/**
	 * 获取资源访问路径
	 * 
	 * @param resource
	 * @param pageIndex
	 * @param publishPipeCode
	 * @param preview
	 * @return
	 */
	String getResourceLink(CmsResource resource, boolean preview);

	/**
	 * 下载资源文件
	 * 
	 * @param resource
	 * @param os
	 */
	void downloadResource(CmsResource resource, HttpServletResponse os);

	/**
	 * 下载远程图片
	 * 
	 * @param url
	 * @param siteId
	 * @param operator
	 * @return
	 * @throws IOException
	 * @throws Exception 
	 */
	CmsResource downloadImageFromUrl(String url, long siteId, String operator) throws Exception;
}
