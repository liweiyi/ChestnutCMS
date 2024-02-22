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
package com.chestnut.common.security.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TableData<T> {
	
	private List<T> rows;
	
	private long total;
	
	public TableData(List<T> rows, long total) {
		this.rows = rows;
		this.total = total;
	}

    public static <T> TableData<T> of(List<T> rows) {
		return new TableData<T>(rows, 0);
    }

	public static <T> TableData<T> of(List<T> rows, long total) {
		return new TableData<T>(rows, total);
	}

	public static <T> TableData<T> of(IPage<T> page) {
		return new TableData<T>(page.getRecords(), page.getTotal());
	}

//	public void exportExcel(OutputStream os) {
//		try (ExcelWriter writer = EasyExcel.write(os, clazz).build()) {
//			WriteSheet sheet = EasyExcel.writerSheet(clazz.getSimpleName()).build();
//			writer.write(rows, sheet);
//		}
//	}
//
//	public void exportExcel(HttpServletResponse response) throws IOException {
//		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//		response.setCharacterEncoding(StandardCharsets.UTF_8.displayName());
//		String fileName = "Export_" + clazz.getSimpleName() + "_" + DateUtils.dateTimeNow("yyyyMMddHHmmss");
//		response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
//		exportExcel(response.getOutputStream());
//		response.setStatus(HttpStatus.OK.value());
//	}
}