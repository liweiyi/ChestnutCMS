package com.chestnut.member.privilege;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * MemberPrivService
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Service
@RequiredArgsConstructor
public class MemberPrivService {



    /**
     * 保存会员权限信息+
     *
     * @param ownerType
     * @param owner
     */
    public void savePrivilege(String ownerType, String owner, String privType, Set<String> privKeys) {

    }
}
