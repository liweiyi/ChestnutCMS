package com.chestnut.common.storage;

import java.util.List;

public class StorageListResult<T> {

	private List<T> objects; 
	
	/**
	 * 列举文件使用的continuationToken
	 */
	private String nextContinuationToken ;
	
	public String getNextContinuationToken() {
		return nextContinuationToken;
	}

	public void setNextContinuationToken(String nextContinuationToken) {
		this.nextContinuationToken = nextContinuationToken;
	}

	public List<T> getObjects() {
		return objects;
	}

	public void setObjects(List<T> objects) {
		this.objects = objects;
	}
}
