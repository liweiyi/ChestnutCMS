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
package com.chestnut.search.domain.dto;

import java.util.ArrayList;
import java.util.List;

import com.chestnut.common.annotation.XComment;
import com.chestnut.common.security.domain.BaseDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XComment("{API.DOC.SEARCH.SEARCH_MODEL_REQ}")
public class SearchModelDTO extends BaseDTO {

	@XComment("{API.DOC.SEARCH.MODEL_LABEL}")
	private String label;

	@XComment("{API.DOC.SEARCH.MODEL_NAME}")
	private String name;

	@XComment("{API.DOC.SEARCH.MODEL_FIELDS}")
	private List<SearchIndexField> fields = new ArrayList<>();
	
	@Getter
	@Setter
	@XComment("{API.DOC.SEARCH.INDEX_FIELD}")
	public static class SearchIndexField {
		
		@XComment("{API.DOC.SEARCH.FIELD_LABEL}")
		private String label;
		
		@XComment("{API.DOC.SEARCH.FIELD_NAME}")
		private String name;
		
		@XComment("{API.DOC.SEARCH.FIELD_TYPE}")
		private String type;
		
		@XComment("{API.DOC.SEARCH.FIELD_PRIMARY}")
		private boolean primary;
		
		@XComment("{API.DOC.SEARCH.FIELD_WEIGHT}")
		private double weight = 1;
		
		@XComment("{API.DOC.SEARCH.FIELD_INDEX}")
		private boolean index;
		
		@XComment("{API.DOC.SEARCH.FIELD_ANALYZER}")
		private String analyzer;
		
		public SearchIndexField(String label, String name, String type, boolean primary) {
			this.label = label;
			this.name = name;
			this.type = type;
			this.primary = primary;
			this.weight = 1;
			this.index = true;
		}
	}
}
