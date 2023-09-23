package com.chestnut.comment.domain.dto;

import com.chestnut.common.security.domain.BaseDTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmitCommentDTO extends BaseDTO {

    /**
     * 评论目标类型
     */
    @NotEmpty
    private String sourceType;

    /**
     * 评论目标ID
     */
    @NotEmpty
    private String sourceId;

    /**
     * 回复的评论ID
     */
    @Min(0)
    private Long commentId;

    /**
     * 回复的用户UID
     */
    private Long replyUid;

    /**
     * 评论内容
     */
    @NotBlank
    private String content;

    /**
     * IP
     */
    private String clientIp;

    /**
     * UA
     */
    private String userAgent;
}
