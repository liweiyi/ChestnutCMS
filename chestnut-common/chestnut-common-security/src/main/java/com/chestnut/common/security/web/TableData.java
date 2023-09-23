package com.chestnut.common.security.web;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TableData<T> {
	
	private List<T> rows;
	
	private long total;
	
	public TableData(List<T> rows, long total) {
		this.rows = rows;
		this.total = total;
	}
}