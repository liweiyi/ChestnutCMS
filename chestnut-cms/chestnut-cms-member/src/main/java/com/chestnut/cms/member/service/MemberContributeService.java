package com.chestnut.cms.member.service;

import com.chestnut.cms.member.domain.dto.ArticleContributeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 会员投稿服务类
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Service
@RequiredArgsConstructor
public class MemberContributeService {

    /**
     * 投稿
     *
     * @param dto
     */
    public void contribute(ArticleContributeDTO dto) {

        // 会员投稿数 + 1

    }
}
