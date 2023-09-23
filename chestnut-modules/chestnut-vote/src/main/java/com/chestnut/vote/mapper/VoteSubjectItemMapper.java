package com.chestnut.vote.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chestnut.vote.domain.VoteSubjectItem;

/**
 * <p>
 * 问卷调查主题选项表Mapper 接口
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface VoteSubjectItemMapper extends BaseMapper<VoteSubjectItem> {

	/**
	 * 选项票数+1
	 * 
	 * @param itemId
	 * @return
	 */
	@Update("UPDATE " + VoteSubjectItem.TABLE_NAME + " SET total = total + 1 WHERE item_id = #{itemId}")
	public int incrItemTotal(@Param("itemId") Long itemId);

}