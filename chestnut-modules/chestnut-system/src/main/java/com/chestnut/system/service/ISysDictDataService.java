package com.chestnut.system.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.system.domain.SysDictData;

/**
 * 字典 业务层
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface ISysDictDataService extends IService<SysDictData> {

	/**
	 * 批量删除字典数据信息
	 * 
	 * @param dictCodes
	 *            需要删除的字典数据ID
	 */
	public void deleteDictDataByIds(List<Long> dictCodes);

	/**
	 * 新增保存字典数据信息
	 * 
	 * @param dictData
	 *            字典数据信息
	 * @return 结果
	 */
	public void insertDictData(SysDictData dictData);

	/**
	 * 修改保存字典数据信息
	 * 
	 * @param dictData
	 *            字典数据信息
	 * @return 结果
	 */
	public void updateDictData(SysDictData dictData);
}
