package com.chestnut.member.listener.event;

import com.chestnut.member.domain.MemberFavorites;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 会员取消收藏后事件
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
public class AfterMemberCancelFavoriteEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private MemberFavorites memberFavorites;

    public AfterMemberCancelFavoriteEvent(Object source, MemberFavorites memberFavorites) {
        super(source);
        this.memberFavorites = memberFavorites;
    }
}
