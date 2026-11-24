package com.chestnut.member.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.chestnut.common.security.domain.Operator;
import com.chestnut.member.domain.MemberSubscribeLog;
import com.chestnut.member.domain.MemberSubscribeRecord;
import com.chestnut.member.domain.dto.AdjustSubscribeDurationReq;
import com.chestnut.member.service.impl.MemberSubscribeRecordServiceImpl;

import java.util.List;

/**
 * 会员订阅基础服务接口，提供结束点调整、权限查询和审计读取。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IMemberSubscribeRecordService extends IService<MemberSubscribeRecord> {

    /** 独立事务调整秒级结束时间并记录审计，同一会员相同来源重试返回首次审计回执。 */
    MemberSubscribeLog adjustDuration(AdjustSubscribeDurationReq req, Operator operator);

    /**
     * 获取会员订阅过期时间，使用UTC纪元秒。
     * 缓存未命中时通过独立只读事务读取最新已提交的结束时间。
     *
     * @param memberId 会员ID
     */
    List<MemberSubscribeRecordServiceImpl.MemberSubscribe> getMemberSubscribes(Long memberId);

    /**
     * 会员是否拥有某项服务的订阅
     *
     * @param memberId 会员ID
     * @param serviceItemId 服务项ID
     * @return 结果
     */
    boolean isSubscribedService(Long memberId, String serviceItemId);
}
