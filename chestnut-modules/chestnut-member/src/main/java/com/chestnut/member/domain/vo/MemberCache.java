package com.chestnut.member.domain.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class MemberCache {

    private Long memberId;

    private String displayName;

    private String cover;

    private String avatar;

    private String slogan;

    private Map<String, Integer> stat = new HashMap<>();

    private List<MemberMenuVO> menus = new ArrayList<>();
}