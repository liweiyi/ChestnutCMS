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
package com.chestnut.cms.stat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chestnut.cms.stat.domain.CmsCatalogContentStat;
import com.chestnut.cms.stat.domain.CmsUserContentStat;
import com.chestnut.cms.stat.domain.vo.ContentStatusTotal;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface CmsUserContentStatMapper extends BaseMapper<CmsUserContentStat> {

    @Select("SELECT status, count(*) as total FROM cms_content WHERE contributor_id = 0 AND site_id = #{siteId} AND create_by = #{userName} GROUP BY status")
    List<ContentStatusTotal> statContentByStatus(@Param("siteId") Long siteId, @Param("userName") String userName);
}

