/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
