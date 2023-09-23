package com.chestnut.cms.member.domain.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ContributorVO {

    private Long uid;

    private String displayName;

    private String avatar;

    private String avatarSrc;

    private String slogan;

    /**
     * 会员相关统计数据
     */
    private Map<String, Integer> stat = new HashMap<>();

    /**
     * 最近发表的文章
     */
    private List<FavoriteContentVO> contents = new ArrayList<>();
}