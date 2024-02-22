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

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chestnut.contentcore.domain.CmsResource;
import com.chestnut.contentcore.service.impl.SiteStatServiceImpl.SiteStatData;

public interface CmsResourceMapper extends BaseMapper<CmsResource> {
	
	/**
	 * 按资源类型分组统计数量
	 * 
	 * @param siteId
	 * @return
	 */
	@Select("""
			<script>
			SELECT resource_type dataKey, COUNT(*) dataValue FROM cms_resource
			<if test=' siteId != null and siteId != 0 '> WHERE site_id = #{siteId}  </if>
			GROUP BY resource_type
			</script>
			""")
	List<SiteStatData> countResourceGroupByType(@Param("siteId") Long siteId);
}
