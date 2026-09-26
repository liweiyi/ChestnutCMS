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
package com.chestnut.cloud.controller;

import com.chestnut.cloud.domain.CcCloudConfig;
import com.chestnut.cloud.domain.dto.QueryCdnRefreshLogRequest;
import com.chestnut.cloud.domain.vo.CdnRefreshLogVO;
import com.chestnut.cloud.exception.CloudErrorCode;
import com.chestnut.cloud.logs.ICdnRefreshLogProvider;
import com.chestnut.cloud.service.ICloudConfigService;
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.domain.R;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.TableData;
import com.chestnut.system.permission.SysMenuPriv;
import com.chestnut.system.security.AdminUserType;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@XComment("{API.DOC.CLOUD.CDN_REFRESH_LOG.MODULE}")
@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.MonitorLogsView)
@RestController
@RequiredArgsConstructor
@RequestMapping("/monitor/cdnRefreshLog")
public class CdnRefreshLogController extends BaseRestController {

    private final ICloudConfigService cloudConfigService;

    private final List<ICdnRefreshLogProvider> providers;

    @XComment("{API.DOC.CLOUD.CDN_REFRESH_LOG.CONFIG_OPTIONS}")
    @GetMapping("/configOptions")
    public R<List<SelectOption>> configOptions() {
        List<String> types = providers.stream().map(ICdnRefreshLogProvider::getCloudType).toList();
        if (types.isEmpty()) {
            return R.ok(List.of());
        }
        List<CcCloudConfig> configs = cloudConfigService.lambdaQuery()
                .select(CcCloudConfig::getConfigId, CcCloudConfig::getConfigName, CcCloudConfig::getType)
                .in(CcCloudConfig::getType, types)
                .orderByDesc(CcCloudConfig::getConfigId).list();
        return bindSelectOptions(configs, config -> config.getConfigId().toString(), CcCloudConfig::getConfigName);
    }

    @XComment("{API.DOC.CLOUD.CDN_REFRESH_LOG.GET_LIST}")
    @GetMapping("/list")
    public R<TableData<CdnRefreshLogVO>> list(@Validated QueryCdnRefreshLogRequest req) {
        CcCloudConfig config = cloudConfigService.getCloudConfig(req.getConfigId());
        ICdnRefreshLogProvider provider = providers.stream()
                .filter(item -> item.getCloudType().equals(config.getType())).findFirst()
                .orElseThrow(() -> CloudErrorCode.UNSUPPORTED_CLOUD_PROVIDER.exception(config.getType()));
        if (req.getBeginTime() == null && req.getEndTime() == null) {
            LocalDateTime now = LocalDateTime.now(ICdnRefreshLogProvider.TIME_ZONE).withNano(0);
            req.setBeginTime(now.minusDays(1));
            req.setEndTime(now);
        }
        return R.ok(provider.queryLogs(config.getConfigProps(), req));
    }
}
