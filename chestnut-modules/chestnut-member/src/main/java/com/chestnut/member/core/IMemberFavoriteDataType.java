package com.chestnut.member.core;

import java.util.List;

/**
 * 会员收藏数据类型接口。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IMemberFavoriteDataType {

    String BEAN_PREFIX = "MemberFavoriteDataType_";

    /**
     * 根据数据ID读取数据信息
     *
     * @param dataIds 数据ID列表
     * @return 数据信息猎豹
     */
    List<MemberFavoriteData> getDataList(List<String> dataIds);

    record MemberFavoriteData(String type, String id, String name, String desc) {}
}
