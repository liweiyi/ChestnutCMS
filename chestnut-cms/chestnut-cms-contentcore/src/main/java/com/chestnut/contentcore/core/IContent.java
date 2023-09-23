package com.chestnut.contentcore.core;

import java.time.LocalDateTime;
import java.util.Map;

import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.CmsSite;

/**
 * 内容抽象接口
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IContent<T> {

	/**
	 * 获取站点ID
	 */
	Long getSiteId();

	/**
	 * 获取所属站点
	 */
	CmsSite getSite();

	/**
	 * 获取栏目ID
	 */
	Long getCatalogId();

	/**
	 * 获取所属栏目
	 */
	CmsCatalog getCatalog();

	/**
	 * 获取内容类型
	 */
	String getContentType();

	/**
	 * 获取内容Entity
	 */
	CmsContent getContentEntity();

	void setContentEntity(CmsContent contentEntity);
	
	/**
	 * 获取内容扩展Entitiy
	 */
	T getExtendEntity();
	
	/**
	 * 新建内容
	 */
	Long add();
	
	/**
	 * 更新内容信息
	 */
	Long save();

	/**
	 * 删除内容
	 */
	void delete();

	/**
	 * 发布内容
	 */
	boolean publish();

	/**
	 * 获取操作人信息
	 */
	LoginUser getOperator();

	/**
	 * 设置操作人信息
	 * 
	 * @param operator
	 */
	void setOperator(LoginUser operator);

	/**
	 * 复制内容到指定栏目
	 * 
	 * @param catalog
	 * @param copyType
	 * @return
	 */
	void copyTo(CmsCatalog catalog, Integer copyType);

	/**
	 * 转移内容到指定栏目
	 * 
	 * @param catalog
	 * @return
	 */
	void moveTo(CmsCatalog catalog);

	/**
	 * 自定义参数，扩展用
	 */
	Map<String, Object> getParams();

	/**
	 * 设置自定义参数
	 * 
	 * @param params
	 */
	void setParams(Map<String, Object> params);

	/**
	 * 置顶
	 * @return
	 */
	void setTop(LocalDateTime topEndTime);

	/**
	 * 取消置顶
	 * @return
	 */
	void cancelTop();

	/**
	 * 排序
	 * @return
	 */
	void sort(Long targetContentId);

	/**
	 * 下线
	 * @return
	 */
	void offline();

	/**
	 * 待发布
	 */
	void toPublish();

	/**
	 * 归档
	 * @return
	 */
	void archive();

	/**
	 * 全文检索分词内容
	 * 
	 * @return
	 */
	String getFullText();

	/**
	 * 是否有扩展表
	 */
	boolean hasExtendEntity();
}
