package com.chestnut.contentcore.publish;

import com.chestnut.common.utils.IdUtils;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.service.IPublishService;
import com.chestnut.contentcore.service.ISiteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * 站点发布任务
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@RequiredArgsConstructor
@Component(IPublishTask.BeanPrefix + SitePublishTask.Type)
public class SitePublishTask implements IPublishTask {

    private final IPublishService publishService;

    private final ISiteService siteService;

    public final static String Type = "site";

    @Override
    public String getType() {
        return Type;
    }

    @Override
    public void publish(Map<String, String> dataMap) {
        Long siteId = MapUtils.getLong(dataMap, "id");
        if (IdUtils.validate(siteId)) {
            CmsSite site = this.siteService.getSite(siteId);
            if (Objects.nonNull(site)) {
                this.publishService.siteStaticize(site);
            }
        }
    }
}
