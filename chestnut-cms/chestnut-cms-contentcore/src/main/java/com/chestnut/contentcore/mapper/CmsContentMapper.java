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

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.service.impl.SiteStatServiceImpl.SiteStatData;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CmsContentMapper extends BaseMapper<CmsContent> {

	/**
	 * 按内容类型分组统计内容数量
	 * 
	 * @param siteId 站点ID
	 * @return 数据列表
	 */
	@Select("""
			<script>
			SELECT content_type dataKey, COUNT(*) dataValue FROM cms_content
			<if test=' siteId != null and siteId != 0 '> WHERE site_id = #{siteId}  </if>
			GROUP BY content_type
			</script>
			""")
	List<SiteStatData> countContentGroupByType(@Param("siteId") Long siteId);
}
