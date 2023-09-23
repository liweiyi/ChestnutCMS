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
