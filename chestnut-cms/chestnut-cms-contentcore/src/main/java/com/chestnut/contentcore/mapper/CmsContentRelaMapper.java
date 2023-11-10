package com.chestnut.contentcore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.CmsContentRela;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface CmsContentRelaMapper extends BaseMapper<CmsContentRela> {

    @Select("""
			<script>
			SELECT c.* FROM cms_content_rela a right join cms_content c on a.rela_content_id = c.content_id
			WHERE a.content_id = #{contentId}
			<if test=' title != null and title != "" '> AND c.title like concat('%', #{title}, '%')  </if>
			ORDER BY a.create_time DESC
			</script>
			""")
    Page<CmsContent> selectRelaContents(IPage<CmsContent> page, @Param("contentId") Long contentId, @Param("title") String title);
}

