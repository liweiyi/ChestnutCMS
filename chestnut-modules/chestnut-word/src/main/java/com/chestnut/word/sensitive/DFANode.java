package com.chestnut.word.sensitive;

import java.util.Map;

public class DFANode<T> {
	
	/**
	 * 上级节点Key
	 */
	String parentKey;
	
	/**
	 * 节点Key
	 */
	String key;
	
	/**
	 * 是否叶子节点
	 */
	boolean end = false;
	
	/**
	 * 叶子节点记录词类型
	 */
	T data;
	
	/**
	 * 子节点
	 */
	Map<String, DFANode<T>> children;
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder()
				.append("{")
				.append("key=").append(key).append(", parent=").append(parentKey).append(", end=").append(end);
		if (data != null) {
			sb.append(", data=").append(data.toString());
		}
		if (this.children != null) {
			sb.append(", children=").append(this.children.toString());
		}
		sb.append("}");
		return sb.toString();
	}
}