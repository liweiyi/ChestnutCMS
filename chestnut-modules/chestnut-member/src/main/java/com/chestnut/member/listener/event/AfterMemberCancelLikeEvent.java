package com.chestnut.member.listener.event;

import com.chestnut.member.domain.MemberFavorites;
import com.chestnut.member.domain.MemberLike;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 会员取消点赞后事件
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
public class AfterMemberCancelLikeEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private MemberLike memberLike;

    public AfterMemberCancelLikeEvent(Object source, MemberLike memberLike) {
        super(source);
        this.memberLike = memberLike;
    }
}
