package com.chestnut.system.groovy;

import java.io.PrintWriter;

public abstract class BaseGroovyScript {

	private PrintWriter writer;

	public void setPrintWriter(PrintWriter writer) {
		this.writer = writer;
	}
	
	public void run() throws Exception {
		try {
			this.run(writer);
		} catch (Exception e) {
			e.printStackTrace(writer);
		}
	}

	protected abstract void run(PrintWriter out) throws Exception;
}