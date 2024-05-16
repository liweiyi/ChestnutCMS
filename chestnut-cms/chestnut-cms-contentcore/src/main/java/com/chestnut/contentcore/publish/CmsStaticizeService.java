package com.chestnut.contentcore.publish;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * CmsStaticizeService
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Service
@RequiredArgsConstructor
public class CmsStaticizeService {

    private final Map<String, IStaticizeType> staticizeTypeMap;

    public IStaticizeType getStaticizeType(String type) {
        return staticizeTypeMap.get(IStaticizeType.BEAN_PREFIX + type);
    }
}

