package com.chestnut.contentcore.publish;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface IStaticizeType {

    Logger logger = LoggerFactory.getLogger("publish");

    String BEAN_PREFIX = "CmsStaticizeType";

    String getType();

    void staticize(String dataId);
}
