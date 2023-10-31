package com.chestnut.contentcore.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.chestnut.common.db.domain.BaseEntityWithLogicDelete;
import com.chestnut.system.fixed.dict.YesOrNo;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 内容表对象 [cms_content]
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(value = CmsContent.TABLE_NAME, autoResultMap = true)
public class CmsContent extends BaseEntityWithLogicDelete {

    private static final long serialVersionUID=1L;
    
    public static final String TABLE_NAME = "cms_content";
    
    public static final Map<String, SFunction<CmsContent, ?>> MAP_PARAMS = Map.of(
            "publishDate", CmsContent::getPublishDate,"createTime", CmsContent::getCreateTime,
            "viewCount", CmsContent::getViewCount, "favoriteCount", CmsContent::getFavoriteCount,
            "likeCount", CmsContent::getLikeCount, "commentCount", CmsContent::getCommentCount);
    
    public static SFunction<CmsContent, ?> getSFunction(String key) {
    	return MAP_PARAMS.get(key);
    }

    /**
     * 内容ID
     */
    @TableId(value = "content_id", type = IdType.INPUT)
    private Long contentId;

    /**
     * 所属站点ID
     */
    private Long siteId;

    /**
     * 所属栏目ID
     */
    private Long catalogId;

    /**
     * 所属栏目祖级IDs
     */
    private String catalogAncestors;

    /**
     * 所属顶级栏目
     */
    private Long topCatalog;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 部门编码
     */
    private String deptCode;

    /**
     * 内容类型
     */
    private String contentType;

    /**
     * 标题
     */
    private String title;

    /**
     * 副标题
     */
    private String subTitle;

    /**
     * 短标题
     */
    private String shortTitle;

    /**
     * 标题样式
     */
    private String titleStyle;

    /**
     * logo
     */
    private String logo;

    /**
     * 来源
     */
    private String source;

    /**
     * 来源URL
     */
    private String sourceUrl;

    /**
     * 是否原创
     */
    private String original;

    /**
     * 作者
     */
    private String author;

    /**
     * 编辑
     */
    private String editor;

    /**
     * 投稿会员Id
     */
    private Long contributorId;

    /**
     * 摘要
     */
    private String summary;

    /**
     * 自定义静态化文件路径
     */
    private String staticPath;

    /**
     * 内容状态
     */
    private String status;

    /**
     * 内容属性
     */
    private Integer attributes;

    /**
     * 是否链接内容
     */
    private String linkFlag;

    /**
     * 链接
     */
    private String redirectUrl;

    /**
     * 置顶标识
     */
    private Long topFlag;

    /**
     * 置顶结束时间
     */
    private LocalDateTime topDate;

    /**
     * 排序字段
     */
    private Long sortFlag;

    /**
     * 关键词
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String[] keywords;

    /**
     * TAGs
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String[] tags;

    /**
     * 复制类型
     */
    private Integer copyType;

    /**
     * 复制源ID
     */
    private Long copyId;

    /**
     * 发布时间
     */
    private LocalDateTime publishDate;

    /**
     * 下线时间
     */
    private LocalDateTime offlineDate;

    /**
     * 点赞数
     */
    private Long likeCount;

    /**
     * 评论数
     */
    private Long commentCount;

    /**
     * 收藏数
     */
    private Long favoriteCount;

    /**
     * 文章浏览数
     */
    private Long viewCount;

    /**
     * 是否锁定
     */
    private String isLock;

    /**
     * 锁定用户名
     */
    private String lockUser;

    /**
     * SEO标题
     */
    private String seoTitle;

    /**
     * SEO关键词
     */
    private String seoKeywords;

    /**
     * SEO描述
     */
    private String seoDescription;

    /**
     * 发布通道
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String[] publishPipe;

    /**
     * 发布通道属性
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Map<String, Object>> publishPipeProps;

    /**
     * 扩展属性
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> configProps;
    
    public boolean isLock() {
    	return YesOrNo.isYes(this.isLock);
    }
    
    public boolean isLinkContent() {
    	return YesOrNo.isYes(this.linkFlag);
    }
}
