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
package com.chestnut.system.service.impl;

import java.util.List;

import com.chestnut.common.utils.IdUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.utils.Assert;
import com.chestnut.system.domain.SysNotice;
import com.chestnut.system.domain.dto.SysNoticeDTO;
import com.chestnut.system.mapper.SysNoticeMapper;
import com.chestnut.system.service.ISysNoticeService;

/**
 * 公告 服务层实现
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Service
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice> implements ISysNoticeService {

	@Override
	public void insertNotice(SysNoticeDTO dto) {
		SysNotice notice = new SysNotice();
		notice.setNoticeId(IdUtils.getSnowflakeId());
		notice.setNoticeTitle(dto.getNoticeTitle());
		notice.setNoticeType(dto.getNoticeType());
		notice.setNoticeContent(dto.getNoticeContent());
		notice.setStatus(dto.getStatus());
		notice.setRemark(dto.getRemark());
		notice.createBy(dto.getOperator().getUsername());
		this.save(notice);
	}

	@Override
	public void updateNotice(SysNoticeDTO dto) {
		SysNotice db = this.getById(dto.getNoticeId());
		Assert.notNull(db, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("noticeId", dto.getNoticeId()));

		db.setNoticeTitle(dto.getNoticeTitle());
		db.setNoticeType(dto.getNoticeType());
		db.setNoticeContent(dto.getNoticeContent());
		db.setStatus(dto.getStatus());
		db.setRemark(dto.getRemark());
		db.updateBy(dto.getOperator().getUsername());
		this.updateById(db);
	}

	/**
	 * 批量删除公告信息
	 * 
	 * @param noticeIds
	 *            需要删除的公告ID
	 * @return 结果
	 */
	@Override
	public void deleteNoticeByIds(List<Long> noticeIds) {
		this.removeBatchByIds(noticeIds);
	}
}
