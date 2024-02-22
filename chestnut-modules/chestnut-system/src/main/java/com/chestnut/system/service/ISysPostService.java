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
package com.chestnut.system.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.system.domain.SysPost;

/**
 * 岗位信息 服务层
 */
public interface ISysPostService extends IService<SysPost> {

	/**
	 * 根据用户ID获取岗位选择框列表
	 * 
	 * @param userId
	 *            用户ID
	 * @return 选中岗位ID列表
	 */
	public List<SysPost> selectPostListByUserId(Long userId);

	/**
	 * 校验岗位名称
	 * 
	 * @param post
	 *            岗位信息
	 * @return 结果
	 */
	public boolean checkPostNameUnique(SysPost post);

	/**
	 * 校验岗位编码
	 * 
	 * @param post
	 *            岗位信息
	 * @return 结果
	 */
	public boolean checkPostCodeUnique(SysPost post);

	/**
	 * 批量删除岗位信息
	 * 
	 * @param postIds
	 *            需要删除的岗位ID
	 * @return 结果
	 */
	public void deletePostByIds(List<Long> postIds);

	/**
	 * 新增保存岗位信息
	 * 
	 * @param post
	 *            岗位信息
	 * @return 结果
	 */
	public void insertPost(SysPost post);

	/**
	 * 修改保存岗位信息
	 * 
	 * @param post
	 *            岗位信息
	 * @return 结果
	 */
	public void updatePost(SysPost post);

	/**
	 * 获取岗位缓存信息
	 * 
	 * @param postCode
	 * @return
	 */
	SysPost getPost(String postCode);
}
