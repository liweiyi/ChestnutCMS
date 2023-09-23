package com.chestnut.contentcore.publish;

import com.chestnut.common.utils.IdUtils;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.contentcore.service.IPublishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * 内容发布任务
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@RequiredArgsConstructor
@Component(IPublishTask.BeanPrefix + ContentPublishTask.Type)
public class ContentPublishTask implements IPublishTask {

    private final IPublishService publishService;

    private final IContentService contentService;

    public final static String Type = "content";

    @Override
    public String getType() {
        return Type;
    }

    @Override
    public void publish(Map<String, String> dataMap) {
        Long contentId = MapUtils.getLong(dataMap, "id");
        if (IdUtils.validate(contentId)) {
            CmsContent content = this.contentService.getById(contentId);
            if (Objects.nonNull(content)) {
                this.publishService.contentStaticize(content);
            }
        }
    }
}
