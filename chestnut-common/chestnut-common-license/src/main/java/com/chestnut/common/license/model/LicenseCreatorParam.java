package com.chestnut.common.license.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * License创建需要的参数
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class LicenseCreatorParam implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 证书Subject(产品名称)
     */
    private String subject;

    /**
     * 私钥别名
     */
    private String privateAlias;

    /**
     * 秘钥密码（需要妥善保管，不能让使用者知道）
     */
    private String keyPass;

    /**
     * 私钥库存储路径
     */
    private String privateKeysStorePath;

    /**
     * 访问私钥库的密码
     */
    private String storePass;

    /**
     * 证书生成路径
     */
    private String licensePath;

    /**
     * 证书生效时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date issuedTime = new Date();

    /**
     * 证书失效时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expiryTime;

    /**
     * 用户类型
     */
    private String consumerType = "user";

    /**
     * 用户数量
     */
    private Integer consumerAmount = 1;

    /**
     * 描述信息
     */
    private String description = "";

    /**
     * 额外的服务器硬件校验信息（或者其他的信息都可以放）
     */
    private LicenseExtraParam extra;

    /**
     * 证书下载地址
     */
    private String licenseUrl;
}
