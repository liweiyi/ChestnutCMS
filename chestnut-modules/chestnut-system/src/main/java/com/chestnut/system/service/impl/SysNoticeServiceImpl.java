/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
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

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.system.domain.SysNotice;
import com.chestnut.system.domain.dto.CreateNoticeRequest;
import com.chestnut.system.domain.dto.UpdateNoticeRequest;
import com.chestnut.system.mapper.SysNoticeMapper;
import com.chestnut.system.service.ISysNoticeService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 公告 服务层实现
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Service
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice> implements ISysNoticeService {

	@Override
	public void insertNotice(CreateNoticeRequest req) {
		SysNotice notice = new SysNotice();
		notice.setNoticeId(IdUtils.getSnowflakeId());
		notice.setNoticeTitle(req.getNoticeTitle());
		notice.setNoticeType(req.getNoticeType());
		notice.setNoticeContent(req.getNoticeContent());
		notice.setStatus(req.getStatus());
		notice.createBy(req.getOperator().getUsername());
		this.save(notice);
	}

	@Override
	public void updateNotice(UpdateNoticeRequest req) {
		SysNotice db = this.getById(req.getNoticeId());
		Assert.notNull(db, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("noticeId", req.getNoticeId()));

		db.setNoticeTitle(req.getNoticeTitle());
		db.setNoticeType(req.getNoticeType());
		db.setNoticeContent(req.getNoticeContent());
		db.setStatus(req.getStatus());
		db.updateBy(req.getOperator().getUsername());
		this.updateById(db);
	}

	@Override
	public void deleteNoticeByIds(List<Long> noticeIds) {
		this.removeBatchByIds(noticeIds);
	}
}
