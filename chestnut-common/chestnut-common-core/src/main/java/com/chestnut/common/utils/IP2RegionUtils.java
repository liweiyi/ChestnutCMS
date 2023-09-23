package com.chestnut.common.utils;

import java.io.IOException;
import java.io.InputStream;

import org.lionsoul.ip2region.xdb.Searcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;

/**
 * IP2Region工具类，内存查询
 */
public class IP2RegionUtils {
	
    private static final Logger logger = LoggerFactory.getLogger(IP2RegionUtils.class);

	private static final String IP2REGION_DB_PATH = "ip2region/ip2region.xdb";
	
	private static Searcher searcher;
	
	static {
        try (InputStream is = IP2RegionUtils.class.getClassLoader().getResourceAsStream(IP2REGION_DB_PATH)) {
        	 byte[] cBuff;
             cBuff = FileCopyUtils.copyToByteArray(is);
             searcher = Searcher.newWithBuffer(cBuff);
        } catch (IOException e1) {
         	logger.error("Load ip2region.xdb failed: {}", e1);
		}
    }
	
	public static String ip2Region(String ip) {
		try {
			if (ServletUtils.isUnknown(ip)) {
				return ServletUtils.UNKNOWN;
			}
			if (ServletUtils.internalIp(ip)) {
				return "内网";
			}
			return searcher.search(ip);
        } catch (Exception e) {
        	if (logger.isDebugEnabled()) {
        		logger.error("Ip2region failed: {}", e);
        	}
        	return ServletUtils.UNKNOWN;
        }
	}
}
