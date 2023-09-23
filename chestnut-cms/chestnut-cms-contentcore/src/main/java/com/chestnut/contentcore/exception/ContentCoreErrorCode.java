package com.chestnut.contentcore.exception;

import com.chestnut.common.exception.ErrorCode;

public enum ContentCoreErrorCode implements ErrorCode {

	/**
	 * 无站点数据
	 */
	NO_SITE,

	/**
	 * 不支持的页面部件类型：{0}
	 */
	UNSUPPORTED_PAGE_WIDGET_TYPE,

	/**
	 * 模板未配置或模板文件不存在
	 */
	TEMPLATE_EMPTY,

	/**
	 * 模板文件不存在
	 */
	TEMPLATE_FILE_NOT_FOUND,

	/**
	 * 模板文件名只能使用大小写字母和下划线，且必须以`{0}`结尾
	 */
	INVALID_TEMPLATE_NAME,

	/**
	 * 不支持的内容类型：{0}
	 */
	UNSUPPORTED_CONTENT_TYPE,

	/**
	 * 不支持的栏目类型：{0}
	 */
	UNSUPPORTED_CATALOG_TYPE,

	/**
	 * 不支持的内部数据类型：{0}
	 */
	UNSUPPORTED_INTERNAL_DATA_TYPE,

	/**
	 * 不支持的资源类型：{0}
	 */
	UNSUPPORTED_RESOURCE_TYPE,

	/**
	 * 不支持的动态模板类型：{0}
	 */
	UNSUPPORTED_DYNAMIC_PAGE_TYPE,

	/**
	 * 请先删除子栏目
	 */
	DEL_CHILD_FIRST,

	/**
	 * 栏目名称/别名/目录重复
	 */
	CONFLICT_CATALOG,

	/**
	 * 栏目层级超出上限<br/>
	 * @see com.chestnut.contentcore.ContentCoreConsts.CATALOG_MAX_TREE_LEVEL
	 */
	CATALOG_MAX_TREE_LEVEL,

	/**
	 * 栏目不能转移到自身或子栏目
	 */
	CATALOG_MOVE_TO_SELF_OR_CHILD,

	/**
	 * 扩展属性[{0}={1}]校验失败
	 */
	INVALID_PROPERTY,

	/**
	 * 站点目录已存在
	 */
	EXISTS_SITE_PATH,

	/**
	 * 内容已被'{0}'锁定
	 */
	CONTENT_LOCKED,

	/**
	 * 标题重复
	 */
	TITLE_REPLEAT,

	/**
	 * 已发布内容不允许编辑，请先下线内容！
	 */
	CANNOT_EDIT_PUBLISHED_CONTENT,

	/**
	 * 无可用发布通道
	 */
	NO_PUBLISHPIPE,

	/**
	 * 栏目发布失败：栏目不可见/不可静态化/标题栏目。
	 */
	CATALOG_CANNOT_PUBLISH,

	/**
	 * 不能操作非当前站点文件
	 */
	SITE_FILE_OP_ERR,

	/**
	 * 模板文件已存在或路径被占用
	 */
	TEMPLATE_PATH_EXISTS,

	/**
	 * 禁止上传的文件类型：{0}
	 */
	NOT_ALLOW_FILE_TYPE,

	/**
	 * 指定文件不支持在线编辑
	 */
	NOT_EDITABLE_FILE,

	/**
	 * 文件不存在
	 */
	FILE_NOT_EXIST,

	/**
	 * 文件已存在
	 */
	FILE_ALREADY_EXISTS,

	/**
	 * 栏目排序值不能为0
	 */
	CATALOG_SORT_VALUE_ZERO,

	/**
	 * 站点导出任务正在进行中
	 */
	SITE_EXPORT_TASK_EXISTS,

	/**
	 * 只能删除初稿或已下线内容
	 */
	DEL_CONTENT_ERR;

	@Override
	public String value() {
		return "{ERRCODE.CMS.CONTENTCORE." + this.name() + "}";
	}
}
