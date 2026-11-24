package com.chestnut.member.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.chestnut.member.domain.MemberSubscribeService;
import com.chestnut.member.domain.dto.SaveMemberSubscribeServiceReq;

import java.util.List;

/**
 * 会员订阅服务项的创建、编辑和删除接口。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IMemberSubscribeServiceService extends IService<MemberSubscribeService> {

    /**
     * 创建订阅服务项
     */
    void createMemberSubscribeService(SaveMemberSubscribeServiceReq req);

    /**
     * 更新订阅服务项
     */
    void updateMemberSubscribeService(SaveMemberSubscribeServiceReq req);

    /**
     * 批量删除订阅服务项
     */
    void deleteMemberSubscribeServices(List<String> ids);
}
