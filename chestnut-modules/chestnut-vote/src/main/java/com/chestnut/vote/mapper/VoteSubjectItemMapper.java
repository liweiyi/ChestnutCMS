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