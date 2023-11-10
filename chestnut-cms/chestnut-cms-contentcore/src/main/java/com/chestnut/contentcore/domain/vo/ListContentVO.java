package com.chestnut.contentcore.domain.vo;

import java.time.LocalDateTime;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ListContentVO {

	/*
	 * 内容ID
	 */
	private Long contentId;

	/*
	 * 内容ID
	 */
	private Long catalogId;

	/*
	 * 内容类型
	 */
	private String contentType;

	/*
	 * 标题
	 */
	private String title;

	/*
	 * 标题样式
	 */
    private String titleStyle;

	/*
	 * 引导图
	 */
    private String logo;

	/*
	 * 引导图预览路径
	 */
    private String logoSrc;

	/*
	 * 内部链接
	 */
    private String internalUrl;

	/*
	 * 是否原创
	 */
    private String original;

	/*
	 * 作者
	 */
    private String author;

	/*
	 * 编辑
	 */
    private String editor;

	/*
	 * 内容状态
	 */
    private String status;

	/*
	 * 内容属性值数组
	 */
    private String[] attributes;

	/*
	 * 关键词
	 */
	private String[] keywords;

	/*
	 * TAGs
	 */
	private String[] tags;

	/*
	 * 摘要
	 */
	private String summary;

	/*
	 * 置顶标识
	 */
    private Long topFlag;

	/*
	 * 置顶时间
	 */
    private Date topDate;

	/*
	 * 是否锁定
	 */
    private String isLock;

	/*
	 * 是否锁定
	 */
    private String lockUser;

	/*
	 * 复制类型
	 */
    private Integer copyType;

	/*
	 * 复制源ID
	 */
    private Long copyId;

	/*
	 * 发布时间
	 */
    private LocalDateTime publishDate;

	/*
	 * 下线时间
	 */
    private LocalDateTime offlineDate;

	/*
	 * 创建人
	 */
    private String createUser;

	/*
	 * 创建时间
	 */
	private LocalDateTime createTime;
}
