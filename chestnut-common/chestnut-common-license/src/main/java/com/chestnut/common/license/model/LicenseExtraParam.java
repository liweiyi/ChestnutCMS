package com.chestnut.common.license.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 自定义需要校验的License参数
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class LicenseExtraParam implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 是否认证IP
     */
    private boolean isIpCheck ;

    /**
     * 可被允许的IP地址
     */
    private List<String> ipAddress;

    /**
     * 是否认证mac
     */
    private boolean isMacCheck ;

    /**
     * 可被允许的mac地址
     */
    private List<String> macAddress;

    /**
     * 是否认证cpu序列号
     */
    private boolean isCpuCheck ;

    /**
     * 可被允许的CPU序列号
     */
    private String cpuSerial;

    /**
     * 是否认证主板号
     */
    private boolean isBoardCheck ;

    /**
     * 可被允许的主板序列号
     */
    private String mainBoardSerial;
}
