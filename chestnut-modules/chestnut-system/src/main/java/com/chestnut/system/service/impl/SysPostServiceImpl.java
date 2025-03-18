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
package com.chestnut.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.SysConstants;
import com.chestnut.system.domain.SysPost;
import com.chestnut.system.domain.SysUserPost;
import com.chestnut.system.exception.SysErrorCode;
import com.chestnut.system.mapper.SysPostMapper;
import com.chestnut.system.mapper.SysUserPostMapper;
import com.chestnut.system.service.ISysPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 岗位信息 服务层处理
 */
@Service
@RequiredArgsConstructor
public class SysPostServiceImpl extends ServiceImpl<SysPostMapper, SysPost> implements ISysPostService {

	private final SysUserPostMapper userPostMapper;

	private final RedisCache redisCache;

	@Override
	public SysPost getPost(String postCode) {
		SysPost post = redisCache.getCacheObject(SysConstants.CACHE_SYS_DEPT_KEY + postCode, SysPost.class);
		if (Objects.nonNull(post)) {
			return post;
		}
		post = this.getOne(new LambdaQueryWrapper<SysPost>().eq(SysPost::getPostCode, postCode));
		if (Objects.nonNull(post)) {
			redisCache.setCacheObject(SysConstants.CACHE_SYS_DEPT_KEY + postCode, post);
		}
		return post;
	}
	
	/**
	 * 根据用户ID获取岗位选择框列表
	 * 
	 * @param userId
	 *            用户ID
	 * @return 选中岗位ID列表
	 */
	@Override
	public List<SysPost> selectPostListByUserId(Long userId) {
		List<Long> postIds = this.userPostMapper.selectList(
					new LambdaQueryWrapper<SysUserPost>().eq(SysUserPost::getUserId, userId)
				).stream().map(SysUserPost::getPostId).toList();
		if (StringUtils.isEmpty(postIds)) {
			return Collections.emptyList();
		}
		return this.listByIds(postIds);
	}

	/**
	 * 校验岗位名称是否唯一
	 * 
	 * @param post
	 *            岗位信息
	 * @return 结果
	 */
	@Override
	public boolean checkPostNameUnique(SysPost post) {
		return this.checkPostUnique(post);
	}

	/**
	 * 校验岗位编码是否唯一
	 * 
	 * @param post
	 *            岗位信息
	 * @return 结果
	 */
	@Override
	public boolean checkPostCodeUnique(SysPost post) {
		return this.checkPostUnique(post);
	}

	private boolean checkPostUnique(SysPost post) {
		LambdaQueryWrapper<SysPost> q = new LambdaQueryWrapper<SysPost>()
				.and(wrapper -> wrapper
						.eq(StringUtils.isNotEmpty(post.getPostName()), SysPost::getPostName, post.getPostName()).or()
						.eq(StringUtils.isNotEmpty(post.getPostCode()), SysPost::getPostCode, post.getPostCode()))
				.ne(IdUtils.validate(post.getPostId()), SysPost::getPostId, post.getPostId());
		return this.count(q) == 0;
	}

	/**
	 * 批量删除岗位信息
	 * 
	 * @param postIds 需要删除的岗位ID
	 */
	@Override
	public void deletePostByIds(List<Long> postIds) {
		List<SysPost> posts = this.listByIds(postIds);
		for (SysPost post : posts) {
			Long count = this.userPostMapper.selectCount(new LambdaQueryWrapper<SysUserPost>()
					.in(SysUserPost::getPostId, post.getPostId()));
			Assert.isTrue(count == 0, () -> SysErrorCode.POST_USER_NOT_EMPTY.exception(post.getPostName()));

			this.redisCache.deleteObject(SysConstants.CACHE_SYS_POST_KEY + post.getPostCode());
		}
		this.removeByIds(postIds);
	}

	/**
	 * 新增保存岗位信息
	 * 
	 * @param post 岗位信息
	 */
	@Override
	public void insertPost(SysPost post) {
		boolean checkPostUnique = this.checkPostUnique(post);
		Assert.isTrue(checkPostUnique, CommonErrorCode.DATA_CONFLICT::exception);

		post.setPostId(IdUtils.getSnowflakeId());
		post.setCreateTime(LocalDateTime.now());
		this.save(post);
		this.redisCache.deleteObject(SysConstants.CACHE_SYS_POST_KEY + post.getPostCode());
	}

	/**
	 * 修改保存岗位信息
	 * 
	 * @param post 岗位信息
	 */
	@Override
	public void updatePost(SysPost post) {
		SysPost db = this.getById(post.getPostId());
		Assert.notNull(db, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(post.getPostId()));
		boolean checkPostUnique = this.checkPostUnique(post);
		Assert.isTrue(checkPostUnique, CommonErrorCode.DATA_CONFLICT::exception);

		post.setUpdateTime(LocalDateTime.now());
		this.updateById(post);
		this.redisCache.deleteObject(SysConstants.CACHE_SYS_POST_KEY + post.getPostCode());
	}
}
