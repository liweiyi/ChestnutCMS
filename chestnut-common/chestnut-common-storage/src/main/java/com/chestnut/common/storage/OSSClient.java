package com.chestnut.common.storage;

public class OSSClient<T> {

	private T client;

	private long lastActiveTime;

	public T getClient() {
		return client;
	}

	public void setClient(T client) {
		this.client = client;
	}

	public long getLastActiveTime() {
		return lastActiveTime;
	}

	public void setLastActiveTime(long lastActiveTime) {
		this.lastActiveTime = lastActiveTime;
	}
}