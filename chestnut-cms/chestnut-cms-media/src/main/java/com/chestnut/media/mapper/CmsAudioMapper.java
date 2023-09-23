package com.chestnut.media.mapper;

import com.chestnut.common.db.DBConstants;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chestnut.media.domain.CmsAudio;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 音频集集音频数据Mapper 接口
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface CmsAudioMapper extends BaseMapper<CmsAudio> {

	/**
	 * 删除逻辑删除标识 deleted = 1 的数据
	 *
	 * @param contentId
	 * @return
	 */
	@Delete("DELETE FROM cms_audio WHERE deleted = " + DBConstants.DELETED_YES + " AND content_id = #{contentId}")
	Long deleteLogicDeletedByContentId(Long contentId);

	/**
	 * 删除站点内容，忽略逻辑删除标识
	 *
	 * @param siteId
	 * @param limit
	 * @return
	 */
	@Delete("DELETE FROM cms_audio WHERE site_id = #{siteId} limit ${limit}")
	Long deleteBySiteIdIgnoreLogicDel(@Param("siteId") Long siteId, @Param("limit") Integer limit);

	/**
	 * 查询站点内容，忽略逻辑删除标识
	 *
	 * @param siteId
	 * @return
	 */
	@Select("SELECT count(*) FROM cms_audio WHERE site_id = #{siteId}")
	Long selectCountBySiteIdIgnoreLogicDel(@Param("siteId") Long siteId);

	/**
	 * 设置 cms_audio 的 deleted 逻辑删除标识值为0
	 *
	 * @param contentId
	 * @return
	 */
	@Update("UPDATE cms_audio SET deleted = " + DBConstants.DELETED_NO + " WHERE content_id = #{contentId}")
	void recoverByContentId(Long contentId);
}
