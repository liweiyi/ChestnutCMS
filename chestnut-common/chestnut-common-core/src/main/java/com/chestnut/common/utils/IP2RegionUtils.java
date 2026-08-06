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
package com.chestnut.common.utils;

import org.lionsoul.ip2region.xdb.LongByteArray;
import org.lionsoul.ip2region.xdb.Searcher;
import org.lionsoul.ip2region.xdb.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * IP2Region工具类，内存查询
 */
public class IP2RegionUtils {
	
    private static final Logger logger = LoggerFactory.getLogger(IP2RegionUtils.class);

	private static final String IP2REGION_DB_PATH = "ip2region/ip2region.xdb";
	
	private static Searcher searcher;
	
	static {
		try (InputStream is = IP2RegionUtils.class.getClassLoader().getResourceAsStream(IP2REGION_DB_PATH)) {
			if (is == null) {
				throw new IOException("ip2region.xdb not found: " + IP2REGION_DB_PATH);
			}
			LongByteArray cBuff = Searcher.loadContentFromInputStream(is);
			searcher = Searcher.newWithBuffer(Version.IPv4, cBuff);
		} catch (IOException e) {
			logger.error("Load ip2region.xdb failed.", e);
		}
	}

	/**
	 * ip转区域，格式：
	 *
	 * @param ip IPv4/IPv6
	 * @return 国家|0|省份|城市|运营商
	 */
	public static String ip2Region(String ip) {
		try {
			if (ServletUtils.isUnknown(ip)) {
				return ServletUtils.UNKNOWN;
			}
			if (ServletUtils.isInternalIp(ip)) {
				return ServletUtils.INTERNAL_IP;
			}
			return searcher.search(ip);
        } catch (Exception e) {
        	if (logger.isDebugEnabled()) {
        		logger.error("Ip2region failed: {}", ip, e);
        	}
        	return ServletUtils.UNKNOWN;
        }
	}

	public static void close() {
        try {
            searcher.close();
        } catch (IOException e) {
            logger.error("Close ip2region searcher failed.", e);
        }
    }
}
