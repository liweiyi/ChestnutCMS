package com.chestnut.contentcore.core;

import java.io.IOException;

import org.apache.commons.lang3.ArrayUtils;

import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsResource;

/**
 * 资源类型
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IResourceType {
	
	/**
	 * Bean名称前缀
	 */
	String BEAN_NAME_PREFIX = "ResourceType_";
    
    /**
     * 站点上传资源文件目录
     */
    String UploadResourceDirectory = "resources/";

    /**
     * 唯一标识
     */
	String getId();

	/**
	 * 名称
	 */
	String getName();

	/**
	 * 资源类型所用后缀
	 */
	String[] getUsableSuffix();
	
	/**
	 * 校验文件后缀是否符合当前资源类型
	 * 
	 * @param suffix
	 * @return
	 */
	default boolean check(String suffix) {
		return ArrayUtils.contains(this.getUsableSuffix(), suffix.toLowerCase());
	}
	
	default String getUploadPath() {
		return UploadResourceDirectory + this.getId() + StringUtils.SLASH;
	}
	
	/**
	 * 处理资源：提取资源属性、添加水印等
	 * 
	 * @param resource
	 * @throws IOException 
	 */
	default byte[] process(CmsResource resource, byte[] bytes) throws IOException {
		resource.setFileSize((long) bytes.length);
		return bytes;
	}
}
