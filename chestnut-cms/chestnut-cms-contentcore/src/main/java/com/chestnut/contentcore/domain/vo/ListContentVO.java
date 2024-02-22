/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	 * 短标题
	 */
	private String shortTitle;

	/*
	 * 副标题
	 */
	private String subTitle;

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
