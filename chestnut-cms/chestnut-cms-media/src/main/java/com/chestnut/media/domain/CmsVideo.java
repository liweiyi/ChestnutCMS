package com.chestnut.media.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.db.domain.BaseEntityWithLogicDelete;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * 视频数据表对象 [cms_video]
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(CmsVideo.TABLE_NAME)
public class CmsVideo extends BaseEntityWithLogicDelete {

    private static final long serialVersionUID = 1L;
    
    public static final String TABLE_NAME = "cms_video";

    @TableId(value = "video_id", type = IdType.INPUT)
    private Long videoId;

    /**
     * 所属内容ID
     */
    private Long contentId;
    
    /**
     * 所属站点ID
     */
    private long siteId;

    /**
     * 视频封面图
     */
    private String cover;

    /**
     * 视频封面图预览路径
     */
    @TableField(exist = false)
    private String coverSrc;

    /**
     * 视频标题
     */
    private String title;
    
    /**
     * 简介
     */
    private String description;

    /**
     * 视频类型
     */
    private String type;

    /**
     * 视频相对站点资源目录路径，如果type==SHARE则存放第三方引用代码
     */
    private String path;

    /**
     * 预览路径
     */
    @TableField(exist = false)
    private String src;

    /**
     * 音频文件大小
     */
    private Long fileSize;
    
    @TableField(exist = false)
    private String fileSizeName;
    
    /**
     * 视频格式
     */
    private String format;

    /**
     * 视频时长，单位：毫秒
     */
    private Long duration;
    
    /**
     * 编码方式
     */
    private String decoder;
    
    /**
     * 视频宽
     */
    private Integer width;
    
    /**
     * 视频高
     */
    private Integer height;
    
    /**
     * 比特率（bsp）
     */
    private Integer bitRate;
    
    /**
     * 帧率
     */
    private Integer frameRate;

    /**
     * 排序字段
     */
    private Integer sortFlag;
}
