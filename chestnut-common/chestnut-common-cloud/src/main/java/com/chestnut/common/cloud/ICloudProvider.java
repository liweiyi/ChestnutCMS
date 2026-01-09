package com.chestnut.common.cloud;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

public interface ICloudProvider {

    String BEAN_PREFIX = "CloudProvider_";

    String getId();

    String getName();

    void refreshCdn(ObjectNode config, CdnRefreshType type, List<String> urls);

    void dealSensitive(ObjectNode configProps);

    void updateConfigProps(ObjectNode oldProps, ObjectNode newProps);
}
