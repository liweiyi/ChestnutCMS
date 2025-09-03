/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
package com.chestnut.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.system.domain.SysNotice;
import com.chestnut.system.domain.dto.CreateNoticeRequest;
import com.chestnut.system.domain.dto.UpdateNoticeRequest;

import java.util.List;

/**
 * 公告 服务层
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface ISysNoticeService extends IService<SysNotice> {
	
	/**
	 * 新增公告
	 * 
	 * @param req 公告信息
	 */
	void insertNotice(CreateNoticeRequest req);

	/**
	 * 修改公告
	 * 
	 * @param req 公告信息
	 */
	void updateNotice(UpdateNoticeRequest req);

	/**
	 * 批量删除公告信息
	 * 
	 * @param noticeIds 公告ID列表
	 */
	void deleteNoticeByIds(List<Long> noticeIds);
}
