package com.chestnut.word.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chestnut.word.domain.HotWordGroup;

/**
 * <p>
 * 热词分组Mapper 接口
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface HotWordGroupMapper extends BaseMapper<HotWordGroup> {

	@Select("select group_id from cms_hot_word_group where code in(#{codes})")
	public List<Long> getHotWordIdsByCode(List<String> codes);
}
