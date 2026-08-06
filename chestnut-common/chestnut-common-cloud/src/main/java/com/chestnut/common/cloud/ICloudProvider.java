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

import tools.jackson.databind.node.ObjectNode;

import java.util.List;

public interface ICloudProvider {

    String BEAN_PREFIX = "CloudProvider_";

    String getId();

    String getName();

    void refreshCdn(ObjectNode config, CdnRefreshType type, List<String> urls);

    default String addDnsRecord(ObjectNode config, String domainName, String rr, String type, String value) {
        throw new UnsupportedOperationException("Unsupported API: addDnsRecord");
    }

    default String updateDnsRecord(ObjectNode config, String recordId, String domainName, String rr, String type, String value) {
        throw new UnsupportedOperationException("Unsupported API: updateDnsRecord");
    }

    default void deleteDnsRecord(ObjectNode config, String recordId, String domainName) {
        throw new UnsupportedOperationException("Unsupported API: deleteDnsRecord");
    }

    default List<DnsRecord> listDnsRecords(ObjectNode config, String domainName) {
        throw new UnsupportedOperationException("Unsupported API: listDnsRecords");
    }

    void dealSensitive(ObjectNode configProps);

    void updateConfigProps(ObjectNode oldProps, ObjectNode newProps);
}
