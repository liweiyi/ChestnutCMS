package com.chestnut.system.security;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface ILoginType {

    String BEAN_PREFIX = "LoginType.";

    String getType();

    String getName();

    void dealSensitive(ObjectNode configProps);

    void updateConfigProps(ObjectNode oldProps, ObjectNode newProps);
}
