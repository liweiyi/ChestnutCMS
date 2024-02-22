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
package com.chestnut.contentcore.mapper;

import java.time.LocalDateTime;
import java.util.List;

import com.chestnut.common.db.DBConstants;
import com.chestnut.contentcore.enums.ContentCopyType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.service.impl.SiteStatServiceImpl.SiteStatData;
import org.apache.ibatis.annotations.Update;

public interface CmsContentMapper extends BaseMapper<CmsContent> {
	
	/**
	 * 按内容类型分组统计内容数量
	 * 
	 * @param siteId
	 * @return
	 */
	@Select("""
			<script>
			SELECT content_type dataKey, COUNT(*) dataValue FROM cms_content
			<if test=' siteId != null and siteId != 0 '> WHERE site_id = #{siteId}  </if>
			GROUP BY content_type
			</script>
			""")
	List<SiteStatData> countContentGroupByType(@Param("siteId") Long siteId);

	/**
	 * 查询指定条件的逻辑删除内容分页数据
	 *
	 * @param page
	 * @param siteId
	 * @param catalogId
	 * @param contentType
	 * @param status
	 * @param title
	 * @return
	 */
	@Select("""
			<script>
			SELECT * FROM cms_content WHERE site_id = #{siteId} AND deleted = 1
			<if test=' catalogId != null and catalogId != 0 '> AND catalog_id = #{catalogId} </if>
			<if test=' status != null and status != "" '> AND status = #{status} </if>
			<if test=' contentType != null and contentType != "" '> AND content_type = #{contentType} </if>
			<if test=' title != null and title != "" '> AND title LIKE #{title} </if>
			ORDER BY update_time DESC
			</script>
			""")
	Page<CmsContent> selectPageWithLogicDel(IPage<CmsContent> page, @Param("siteId") Long siteId,
											@Param("catalogId") Long catalogId, @Param("contentType") String contentType, @Param("status") String status,
											@Param("title") String title);


	/**
	 * 查询指定IDs的逻辑删除内容列表
	 *
	 * @param contentIds
	 * @return
	 */
	@Select("""
			<script>
			SELECT * FROM cms_content WHERE content_id in (
			<foreach item="contentId" collection="contentIds" separator=",">
			#{contentId}
			</foreach>
			)
			</script>
			""")
	List<CmsContent> selectByIdsWithLogicDel(@Param("contentIds") List<Long> contentIds);

	/**
	 * 获取指定备份时间之前的逻辑删除标识为1的数据总数
	 * 
	 * @param updateTime
	 * @return
	 */
	@Select("SELECT count(*) FROM cms_content WHERE deleted = " + DBConstants.DELETED_YES + " AND update_time < #{updateTime}")
	Long selectCountBeforeWithLogicDel(@Param("updateTime") LocalDateTime updateTime);
	
	/**
	 * 获取指定备份时间之前的内容逻辑删除标识为1的数据
	 * 
	 * @param page
	 * @param updateTime
	 * @return
	 */
	@Select("SELECT * FROM cms_content WHERE deleted = " + DBConstants.DELETED_YES + " AND update_time < #{updateTime}")
	Page<CmsContent> selectPageBeforeWithLogicDel(IPage<CmsContent> page, @Param("updateTime") LocalDateTime updateTime);

	/**
	 * 删除指定内容，忽略逻辑删除标识
	 *
	 * @param contentIds
	 * @return
	 */
	@Delete("""
			<script>
			DELETE FROM cms_content WHERE content_id in (
			<foreach item="contentId" collection="contentIds" separator=",">
			#{contentId}
			</foreach>
			)
			</script>
			""")
	Long deleteByIdsIgnoreLogicDel(@Param("contentIds") List<Long> contentIds);

	/**
	 * 删除所有映射内容，忽略逻辑删除标识
	 *
	 * @param contentId
	 */
	@Delete("DELETE FROM cms_content WHERE copy_id = #{contentId} AND copy_type > " + ContentCopyType.Independency)
	Long deleteMappingIgnoreLogicDel(@Param("contentId") Long contentId);

	/**
	 * 删除站点内容，忽略逻辑删除标识影响
	 *
	 * @param siteId
	 * @param limit
	 * @return
	 */
	@Delete("DELETE FROM cms_content WHERE site_id = #{siteId} limit ${limit}")
	Long deleteBySiteIdIgnoreLogicDel(@Param("siteId") Long siteId, @Param("limit") Integer limit);

	/**
	 * 查询站点内容，忽略逻辑删除标识影响
	 * 
	 * @param siteId
	 * @return
	 */
	@Select("SELECT count(*) FROM cms_content WHERE site_id = #{siteId}")
	Long selectCountBySiteIdIgnoreLogicDel(@Param("siteId") Long siteId);

	/**
	 * 设置cms_content的逻辑删除标识为0
	 *
	 * @param contentId
	 * @return
	 */
	@Update("UPDATE cms_content SET deleted = " + DBConstants.DELETED_NO + " WHERE content_id = #{contentId}")
	Long recoverById(@Param("contentId") Long contentId);
}
