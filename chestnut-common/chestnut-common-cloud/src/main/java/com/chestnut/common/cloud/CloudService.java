package com.chestnut.common.cloud;

import com.chestnut.common.utils.Assert;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CloudService {

    private final Map<String, ICloudProvider> cloudProviderMap;

    public ICloudProvider getCloudProvider(String cloudProviderId) {
        ICloudProvider provider = cloudProviderMap.get(ICloudProvider.BEAN_PREFIX + cloudProviderId);
        Assert.notNull(provider, () -> CloudErrorCode.UNSUPPORTED_CLOUD_PROVIDER.exception(cloudProviderId));
        return provider;
    }

    public Optional<ICloudProvider> optCloudProvider(String cloudProviderId) {
        ICloudProvider provider = cloudProviderMap.get(cloudProviderId);
        return Optional.ofNullable(provider);
    }
}
