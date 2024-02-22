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
package com.chestnut.block.domain.vo;

import java.util.List;

import com.chestnut.block.ManualPageWidgetType.RowData;
import com.chestnut.contentcore.domain.vo.PageWidgetVO;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 动态区块页面部件展示对象
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter 
@Setter
@Accessors(chain = true)
public class ManualPageWidgetVO extends PageWidgetVO {
	
	/**
	 * 区块内容
	 */
    private List<RowData> content;
}
