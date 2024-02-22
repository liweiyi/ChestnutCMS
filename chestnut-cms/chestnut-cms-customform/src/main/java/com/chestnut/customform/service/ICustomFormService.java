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
package com.chestnut.customform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.customform.domain.CmsCustomForm;
import com.chestnut.customform.domain.dto.CustomFormAddDTO;
import com.chestnut.customform.domain.dto.CustomFormEditDTO;
import com.chestnut.xmodel.domain.XModel;
import com.chestnut.xmodel.dto.XModelDTO;

import java.io.IOException;
import java.util.List;

public interface ICustomFormService extends IService<CmsCustomForm> {

	/**
	 * 添加自定义表单
	 * 
	 * @param dto
	 * @return
	 */
	void addCustomForm(CustomFormAddDTO dto);
	
	/**
	 * 编辑自定义表单
	 * 
	 * @param dto
	 * @return
	 */
	void editCustomForm(CustomFormEditDTO dto);
	
	/**
	 * 删除自定义表单
	 * 
	 * @param formIds
	 * @return
	 */
	void deleteCustomForm(List<Long> formIds);

	/**
	 * 发布自定义表单
	 *
	 * @param formIds
	 * @param username
	 */
    void publishCustomForms(List<Long> formIds, String username);

	/**
	 * 下线自定义表单
	 *
	 * @param formIds
	 * @param username
	 */
    void offlineCustomForms(List<Long> formIds, String username) throws IOException;
}
