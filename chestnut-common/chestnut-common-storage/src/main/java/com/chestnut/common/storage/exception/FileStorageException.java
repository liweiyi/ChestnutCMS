package com.chestnut.common.storage.exception;

public class FileStorageException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public FileStorageException(Throwable e) {
		super(e);
	}

	public FileStorageException(String message) {
		super(message);
	}

	public FileStorageException(String message, Throwable e) {
		super(message, e);
	}
}
