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
package com.chestnut.contentcore.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.domain.R;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.contentcore.domain.CmsSiteProperty;
import com.chestnut.contentcore.mapper.CmsSitePropertyMapper;
import com.chestnut.contentcore.service.ISitePropertyService;

@Service
public class SitePropertyServiceImpl extends ServiceImpl<CmsSitePropertyMapper, CmsSiteProperty> implements ISitePropertyService {

	@Override
	public R<String> addSiteProperty(CmsSiteProperty siteProperty) {
		if (!checkUnique(siteProperty.getPropCode(), siteProperty.getSiteId(), null)) {
			return R.fail("属性代码不能重复");
		}
		siteProperty.setPropertyId(IdUtils.getSnowflakeId());
		this.save(siteProperty);
		return R.ok();
	}

	@Override
	public R<String> saveSiteProperty(CmsSiteProperty siteProperty) {
		CmsSiteProperty prop = this.getById(siteProperty.getPropertyId());
		if (prop == null) {
			return R.fail("数据ID错误");
		}
		if (!checkUnique(siteProperty.getPropCode(), siteProperty.getSiteId(), siteProperty.getPropertyId())) {
			return R.fail("属性代码不能重复");
		}
		prop.setPropName(siteProperty.getPropName());
		prop.setPropCode(siteProperty.getPropCode());
		prop.setPropValue(siteProperty.getPropValue());
		prop.setRemark(siteProperty.getRemark());
		prop.updateBy(siteProperty.getUpdateBy());
		this.updateById(prop);
		return R.ok();
	}

	@Override
	public R<String> deleteSiteProperties(List<Long> propertyIds) {
		if (!this.removeByIds(propertyIds)) {
			return R.fail("数据库操作异常");
		}
		return R.ok();
	}

    private boolean checkUnique(String propCode, Long siteId, Long propertyId) {
        LambdaQueryWrapper<CmsSiteProperty> q = new LambdaQueryWrapper<CmsSiteProperty>()
        		.eq(CmsSiteProperty::getSiteId, siteId)
                .eq(CmsSiteProperty::getPropCode, propCode)
                .ne(propertyId != null && propertyId > 0, CmsSiteProperty::getPropertyId, propertyId);
        return this.count(q) == 0;
    }
}
