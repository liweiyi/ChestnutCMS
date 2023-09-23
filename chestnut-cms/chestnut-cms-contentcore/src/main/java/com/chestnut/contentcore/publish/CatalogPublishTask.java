package com.chestnut.contentcore.publish;

import com.chestnut.common.utils.IdUtils;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.IPublishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * 栏目发布任务
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@RequiredArgsConstructor
@Component(IPublishTask.BeanPrefix + CatalogPublishTask.Type)
public class CatalogPublishTask implements IPublishTask {

    private final IPublishService publishService;

    private final ICatalogService catalogService;

    public final static String Type = "catalog";

    @Override
    public String getType() {
        return Type;
    }

    @Override
    public void publish(Map<String, String> dataMap) {
        Long catalogId = MapUtils.getLong(dataMap, "id");
        if (IdUtils.validate(catalogId)) {
            CmsCatalog catalog = this.catalogService.getCatalog(catalogId);
            if (Objects.nonNull(catalog)) {
                this.publishService.catalogStaticize(catalog);
            }
        }
    }
}
