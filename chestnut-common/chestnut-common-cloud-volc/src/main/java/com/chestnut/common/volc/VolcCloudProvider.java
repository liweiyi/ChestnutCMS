package com.chestnut.common.volc;

import com.chestnut.common.cloud.CdnRefreshType;
import com.chestnut.common.cloud.ICloudProvider;
import com.chestnut.common.exception.GlobalException;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.volcengine.ApiClient;
import com.volcengine.ApiException;
import com.volcengine.cdn.CdnApi;
import com.volcengine.cdn.model.SubmitRefreshTaskRequest;
import com.volcengine.cdn.model.SubmitRefreshTaskResponse;
import com.volcengine.model.Error;
import com.volcengine.sign.Credentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component(ICloudProvider.BEAN_PREFIX + VolcCloudProvider.ID)
public class VolcCloudProvider implements ICloudProvider {

    public static final String ID = "Volc";

    public static final String NAME = "火山云";


    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void dealSensitive(ObjectNode configProps) {
        configProps.put("secretKey", "******");
    }

    @Override
    public void updateConfigProps(ObjectNode oldProps, ObjectNode newProps) {
        String appSecret = newProps.get("secretKey").asText();
        if ("******".equals(appSecret)) {
            newProps.put("secretKey", oldProps.get("secretKey").asText());
        }
    }

    @Override
    public void refreshCdn(ObjectNode props, CdnRefreshType type, List<String> urls) {
        VolcConfig config = JacksonUtils.convertValue(props, VolcConfig.class);
        if (Objects.isNull(config)) {
            throw new GlobalException("Refresh cdn failed, config is null.");
        }
        if (StringUtils.isEmpty(config.getAccessKey())) {
            throw new GlobalException("Refresh cdn failed, Missing access key.");
        }
        if (StringUtils.isEmpty(config.getSecretKey())) {
            throw new GlobalException("Refresh cdn failed, Missing secret key.");
        }
        ApiClient apiClient = new ApiClient()
                .setCredentials(Credentials.getCredentials(config.getAccessKey(),  config.getSecretKey()))
                .setRegion(StringUtils.defaultIfEmpty(config.getRegion(), "cn-beijing"));

        CdnApi cdnApi = new CdnApi(apiClient);

        String refreshType = "file";
        if (CdnRefreshType.DIR == type) {
            refreshType = "dir";
        } else if (CdnRefreshType.REGEX == type) {
            refreshType = "regex";
        }
        SubmitRefreshTaskRequest submitRefreshTaskRequest = new SubmitRefreshTaskRequest();
        submitRefreshTaskRequest.setUrls(String.join("\n", urls));
        submitRefreshTaskRequest.setType(refreshType);
        try {
            SubmitRefreshTaskResponse response = cdnApi.submitRefreshTask(submitRefreshTaskRequest);
            Error error = response.getResponseMetadata().getError();
            if (Objects.nonNull(error)) {
                throw new GlobalException("[%s] %s".formatted(error.getCode(), error.getMessage()));
            }
            log.debug("Refresh cdn complete: {}", String.join(",", urls));
        } catch (ApiException e) {
            throw new VolcEngineException("Refresh cdn failed: " + e.getResponseMetadata().getError().getMessage(), e);
        }
    }
}
