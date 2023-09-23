package com.chestnut.advertisement.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chestnut.advertisement.domain.CmsAdHourStat;

public interface CmsAdHourStatMapper extends BaseMapper<CmsAdHourStat> {

	@Select("""
			<script>
			SELECT * FROM `cms_ad_hour_stat`
			WHERE advertisement_id = #{advertisementId}
			<if test='begin != null'> and hour &gt;= #{begin} </if>
			<if test='end != null'> and hour &lt;= #{end} </if>
			ORDER BY hour ASC
			</script>
			""")
	public List<CmsAdHourStat> selectHourStat(@Param("advertisementId") Long advertisementId,
			@Param("begin") String begin, @Param("end") String end);

	@Select("""
			<script>
			SELECT advertisement_id, sum(click) click, sum(view) view FROM `cms_ad_hour_stat`
			WHERE site_id = #{siteId}
			<if test='begin != null'> AND hour &gt;= #{begin} </if>
			<if test='end != null'> AND hour &lt;= #{end} </if>
			GROUP BY advertisement_id ORDER BY `click` DESC, `view` DESC
			</script>
			""")
	public List<CmsAdHourStat> selectGroupByAdvId(@Param("siteId") Long siteId, @Param("begin") String begin,
			@Param("end") String end);
}
