package com.chestnut.member.listener.event;

import com.chestnut.member.domain.MemberFavorites;
import com.chestnut.member.domain.MemberLike;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 会员点赞数据保存后事件
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
public class AfterMemberLikeEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private MemberLike memberLike;

    public AfterMemberLikeEvent(Object source, MemberLike memberLike) {
        super(source);
        this.memberLike = memberLike;
    }
}
