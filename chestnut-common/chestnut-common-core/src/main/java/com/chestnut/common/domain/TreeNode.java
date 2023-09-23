package com.chestnut.common.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 树结构节点实体类
 */
public class TreeNode<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	/** 节点ID */
	private T id;

	/** 节点父ID */
	private T parentId;

	/** 节点名称 */
	private String label;

	/**
	 * 是否顶级节点
	 */
	private boolean isRoot;

	/**
	 * 是否禁用
	 */
	private boolean isDisabled = false;

	/**
	 * 是否新节点，会不同颜色显示
	 */
	private boolean isNew = false;

	/**
	 * 是否默认展开
	 */
	private boolean isDefaultExpanded = false;

	/**
	 * 是否默认选中
	 */
	private boolean checked = false;

	/** 节点属性 */
	private Map<String, Object> props;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<TreeNode<T>> children;

	/**
	 * 构建树结构
	 * 
	 * @param <T>
	 * @param list
	 * @return
	 */
	public static <T> List<TreeNode<T>> build(List<TreeNode<T>> list) {
		Map<T, List<TreeNode<T>>> mapChildren = list.stream().filter(n -> !n.isRoot)
				.collect(Collectors.groupingBy(TreeNode::getParentId));
		List<TreeNode<T>> result = new ArrayList<>();
		list.forEach(n -> {
			n.setChildren(mapChildren.get(n.getId()));
			if (n.isRoot) {
				result.add(n);
			}
		});
		return result;
	}

	public TreeNode(T nodeId, T parentId, String nodeName, boolean isRoot) {
		this.id = nodeId;
		this.parentId = parentId;
		this.label = nodeName;
		this.isRoot = isRoot;
	}

	public T getId() {
		return id;
	}

	public void setId(T id) {
		this.id = id;
	}

	public T getParentId() {
		return parentId;
	}

	public void setParentId(T parentId) {
		this.parentId = parentId;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public List<TreeNode<T>> getChildren() {
		return children;
	}

	public void setChildren(List<TreeNode<T>> children) {
		this.children = children;
	}

	public boolean getIsRoot() {
		return isRoot;
	}

	public void setIsRoot(boolean isRoot) {
		this.isRoot = isRoot;
	}

	public boolean getIsDisabled() {
		return isDisabled;
	}

	public void setDisabled(boolean isDisabled) {
		this.isDisabled = isDisabled;
	}

	public boolean getIsNew() {
		return isNew;
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}

	public boolean getIsDefaultExpanded() {
		return isDefaultExpanded;
	}

	public void setDefaultExpanded(boolean isDefaultExpanded) {
		this.isDefaultExpanded = isDefaultExpanded;
	}

	public Map<String, Object> getProps() {
		if (props == null) {
			props = new HashMap<>();
		}
		return props;
	}

	public void setProps(Map<String, Object> props) {
		this.props = props;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}
}
