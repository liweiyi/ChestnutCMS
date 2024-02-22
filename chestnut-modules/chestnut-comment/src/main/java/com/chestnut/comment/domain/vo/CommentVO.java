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
package com.chestnut.comment.domain.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.chestnut.comment.domain.Comment;
import com.chestnut.common.db.DBConstants;
import com.chestnut.member.domain.vo.MemberCache;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

/**
 * 评论数据
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class CommentVO {

    private Long commentId;

    private Long parentId;

    private String dataId;

    private String sourceTitle;

    private String sourceUrl;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论时间
     */
    private Long commentTime;

    /**
     * 逻辑删除标识
     */
    private Integer deleted;

    /**
     * IP
     */
    private String ip;

    /**
     * 属地
     */
    private String location;

    /**
     * 客户端类型
     */
    private String clientType;

    /**
     * 回复数
     */
    private int replyCount;

    /**
     * 如果是回复类型，且回复的也是回复类型评论，则需要设置回复的用户ID
     */
    private MemberCache replyUser;

    /**
     * 评论提交人信息
     */
    private MemberCache user;

    /**
     * 回复列表
     */
    private List<CommentVO> replyList;

    public static CommentVO newInstance(Comment comment) {
        CommentVO vo = new CommentVO();
        vo.setCommentId(comment.getCommentId());
        vo.setParentId(comment.getParentId());
        vo.setDataId(comment.getSourceId());
        vo.setClientType(comment.getClientType());
        vo.setCommentTime(comment.getCommentTime().toInstant(ZoneOffset.UTC).toEpochMilli());
        vo.setDeleted(comment.getDelFlag());
        vo.setIp(comment.getIp());
        vo.setLikeCount(comment.getLikeCount());
        vo.setLocation(comment.getLocation());
        vo.setReplyCount(comment.getReplyCount());
        if (!DBConstants.isDeleted(comment.getDelFlag())) {
            vo.setContent(comment.getContent());
        }
        return vo;
    }
}
