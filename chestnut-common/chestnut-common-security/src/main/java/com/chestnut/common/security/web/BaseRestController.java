package com.chestnut.common.security.web;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chestnut.common.domain.R;
import com.chestnut.common.utils.DateUtils;
import com.chestnut.common.utils.StringUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Validated
public class BaseRestController {

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(Date.class, new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) {
				setValue(DateUtils.parseDate(text));
			}
		});
		binder.registerCustomEditor(LocalDateTime.class, new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) {
				setValue(LocalDateTime.parse(text, DateUtils.FORMAT_YYYY_MM_DD_HH_MM_SS));
			}
		});
		binder.registerCustomEditor(LocalDate.class, new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) {
				setValue(LocalDate.parse(text, DateUtils.FORMAT_YYYY_MM_DD));
			}
		});
	}
	
	/**
	 * 设置请求分页数据
	 */
	protected PageRequest getPageRequest() {
		return PageRequestDTO.buildPageRequest();
	}

	protected <T> R<TableData<T>> bindDataTable(IPage<T> page) {
		return R.ok(new TableData<>(page.getRecords(), page.getTotal()));
	}
	
	protected <T> R<TableData<T>> bindDataTable(List<T> list) {
		return R.ok(new TableData<>(list, list.size()));
	}
	
	protected <T> R<TableData<T>> bindDataTable(List<T> list, int total) {
		return R.ok(new TableData<>(list, total));
	}
	
	protected <T> R<TableData<T>> bindDataTable(List<T> list, long total) {
		return R.ok(new TableData<>(list, total));
	}

	/**
	 * 页面跳转
	 */
	public String redirect(String url) {
		return StringUtils.messageFormat("redirect:{0}", url);
	}

	/**
	 * 导出excel
	 */
	protected <T> void exportExcel(List<T> list, Class<T> clazz, HttpServletResponse response) {
		try {
			response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
	        response.setCharacterEncoding(StandardCharsets.UTF_8.displayName());
			String fileName = "Export_" + clazz.getSimpleName() + "_" + DateUtils.dateTimeNow("yyyyMMddHHmmss");
	        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
	        
	        ExcelWriter writer = EasyExcel.write(response.getOutputStream(), clazz).build();
	        WriteSheet sheet = EasyExcel.writerSheet(clazz.getSimpleName()).build();
	        writer.write(list, sheet);
	        writer.finish();
        	response.setStatus(HttpStatus.OK.value());
		} catch (IOException e) {
        	response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        	e.printStackTrace();
		}
	}
}
