package com.chestnut.cms.stat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chestnut.cms.stat.domain.CmsCatalogContentStat;
import com.chestnut.cms.stat.domain.vo.ContentStatusTotal;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface CmsCatalogContentStatMapper extends BaseMapper<CmsCatalogContentStat> {

    @Select("SELECT status, count(*) as total FROM cms_content WHERE catalog_id = #{catalogId} GROUP BY status")
    List<ContentStatusTotal> statContentByStatus(@Param("catalogId") Long catalogId);
}

