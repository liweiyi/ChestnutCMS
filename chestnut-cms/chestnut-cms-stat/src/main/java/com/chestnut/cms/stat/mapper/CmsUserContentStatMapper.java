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

