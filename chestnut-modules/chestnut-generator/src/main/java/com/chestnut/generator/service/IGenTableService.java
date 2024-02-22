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
package com.chestnut.generator.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.generator.domain.GenTable;

/**
 * 业务 服务层
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IGenTableService extends IService<GenTable> {

	/**
	 * 查询据库列表
	 * 
	 * @param genTable
	 *            业务信息
	 * @return 数据库表集合
	 */
	public List<GenTable> selectDbTableList(GenTable genTable);

	/**
	 * 查询据库列表
	 * 
	 * @param tableNames
	 *            表名称组
	 * @return 数据库表集合
	 */
	public List<GenTable> selectDbTableListByNames(String[] tableNames);

	/**
	 * 查询所有表信息
	 * 
	 * @return 表信息集合
	 */
	public List<GenTable> selectGenTableAll();

	/**
	 * 查询业务信息
	 * 
	 * @param id
	 *            业务ID
	 * @return 业务信息
	 */
	public GenTable selectGenTableById(Long id);

	/**
	 * 修改业务
	 * 
	 * @param genTable
	 *            业务信息
	 * @return 结果
	 */
	public void updateGenTable(GenTable genTable);

	/**
	 * 删除业务信息
	 * 
	 * @param tableIds
	 *            需要删除的表数据ID
	 * @return 结果
	 */
	public void deleteGenTableByIds(List<Long> tableIds);

	/**
	 * 导入表结构
	 * 
	 * @param tableList
	 *            导入表列表
	 */
	public void importGenTable(List<GenTable> tableList, String operator);

	/**
	 * 预览代码
	 * 
	 * @param tableId
	 *            表编号
	 * @return 预览数据列表
	 */
	public Map<String, String> previewCode(Long tableId);

	/**
	 * 生成代码（下载方式）
	 * 
	 * @param tableName
	 *            表名称
	 * @return 数据
	 */
	public byte[] downloadCode(String tableName);

	/**
	 * 生成代码（自定义路径）
	 * 
	 * @param tableName
	 *            表名称
	 * @return 数据
	 */
	public void generatorCode(String tableName);

	/**
	 * 同步数据库
	 * 
	 * @param tableName
	 *            表名称
	 */
	public void synchDb(String tableName);

	/**
	 * 批量生成代码（下载方式）
	 * 
	 * @param tableNames
	 *            表数组
	 * @return 数据
	 */
	public byte[] downloadCode(String[] tableNames);

	/**
	 * 修改保存参数校验
	 * 
	 * @param genTable
	 *            业务信息
	 */
	public void validateEdit(GenTable genTable);
}
