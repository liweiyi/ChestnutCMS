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
package com.chestnut.article.mapper;

import com.chestnut.common.db.DBConstants;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chestnut.article.domain.CmsArticleDetail;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 文章Mapper 接口
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface CmsArticleDetailMapper extends BaseMapper<CmsArticleDetail> {

	/**
	 * 删除逻辑删除标识deleted=1的数据
	 *
	 * @param contentId
	 * @return
	 */
	@Delete("DELETE FROM cms_article_detail WHERE deleted = 1 AND content_id = #{contentId}")
	Long deleteLogicDeletedById(@Param("contentId") Long contentId);

	/**
	 * 删除站点内容，忽略逻辑删除标识影响
	 *
	 * @param siteId
	 * @param limit
	 * @return
	 */
	@Delete("DELETE FROM cms_article_detail WHERE site_id = #{siteId} limit ${limit}")
	Long deleteBySiteIdIgnoreLogicDel(@Param("siteId") Long siteId, @Param("limit") Integer limit);

	/**
	 * 查询站点内容，忽略逻辑删除标识影响
	 *
	 * @param siteId
	 * @return
	 */
	@Select("SELECT count(*) FROM cms_article_detail WHERE site_id = #{siteId}")
	Long selectCountBySiteIdIgnoreLogicDel(@Param("siteId") Long siteId);

	/**
	 * 设置cms_article_detail的deleted逻辑删除标识值为0
	 *
	 * @param contentId
	 * @return
	 */
	@Update("UPDATE cms_article_detail SET deleted = " + DBConstants.DELETED_NO + " WHERE content_id = #{contentId}")
	Long recoverById(@Param("contentId") Long contentId);
}
