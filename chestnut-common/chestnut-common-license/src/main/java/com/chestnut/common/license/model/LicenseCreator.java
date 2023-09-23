package com.chestnut.common.license.model;

import com.chestnut.common.domain.R;
import com.chestnut.common.license.ChestnutLicenseManager;
import de.schlichtherle.license.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.security.auth.x500.X500Principal;
import java.io.File;
import java.util.prefs.Preferences;

/**
 * <TODO description class purpose>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@RequiredArgsConstructor
public class LicenseCreator extends LicenseManager {

    /**
     * 证书的发行者和主体字段信息
     */
    private final static X500Principal DEFAULT_HOLDER_AND_ISSUER = new X500Principal("CN=a, OU=a, O=a, L=a, ST=a, C=a");

    private final LicenseCreatorParam param;

    /**
     * <p>生成License证书</p>
     * @return GxLicenseResult 证书生成结果
     */
    public R<LicenseContent> generateLicense(){
        try {
            // 1、根据外部传入的创建Lic的参数信息初始化lic参数（秘钥部分）
            LicenseParam licenseParam = initLicenseParam(param);
            // 2、根据外部传入的创建Lic的属性信息初始化lic内容（除了truelicense自带的还包括自己定义的）
            LicenseContent licenseContent = initLicenseContent(param);
            // 3、构建Lic管理器
            LicenseManager licenseManager = new ChestnutLicenseManager(licenseParam);
            // 4、根据param传入的lic生成的路径创建空文件
            File licenseFile = new File(param.getLicensePath());
            // 5、通过Lic管理器，将内容写入Lic文件中
            licenseManager.store(licenseContent, licenseFile);
            return R.ok(licenseContent, "License generate success!");
        }catch (Exception e){
            log.error("License generate failed!", e);
            return R.fail("License generate failed: " + e.getMessage());
        }
    }

    private LicenseParam initLicenseParam(LicenseCreatorParam param){
        Preferences preferences = Preferences.userNodeForPackage(de.schlichtherle.license.LicenseCreator.class);
        /* 设置对证书内容加密的秘钥 */
        CipherParam cipherParam = new DefaultCipherParam(param.getStorePass());
        KeyStoreParam privateStoreParam = new DefaultKeyStoreParam(de.schlichtherle.license.LicenseCreator.class
                ,param.getPrivateKeysStorePath()
                ,param.getPrivateAlias()
                ,param.getStorePass()
                ,param.getKeyPass());
        return new DefaultLicenseParam(param.getSubject(),preferences,privateStoreParam,cipherParam);
    }

    /**
     * 初始化证书内容信息对象
     *
     * @param param 生成证书参数
     * @return LicenseContent 证书内容
     */
    private LicenseContent initLicenseContent(LicenseCreatorParam param){
        LicenseContent licenseContent = new LicenseContent();
        licenseContent.setHolder(DEFAULT_HOLDER_AND_ISSUER);
        licenseContent.setIssuer(DEFAULT_HOLDER_AND_ISSUER);
        /* 设置证书名称 */
        licenseContent.setSubject(param.getSubject());
        /* 设置证书有效期 */
        licenseContent.setIssued(param.getIssuedTime());
        /* 设置证书生效日期 */
        licenseContent.setNotBefore(param.getIssuedTime());
        /* 设置证书失效日期 */
        licenseContent.setNotAfter(param.getExpiryTime());
        /* 设置证书用户类型 */
        licenseContent.setConsumerType(param.getConsumerType());
        /* 设置证书用户数量 */
        licenseContent.setConsumerAmount(param.getConsumerAmount());
        /* 设置证书描述信息 */
        licenseContent.setInfo(param.getDescription());
        /* 设置证书扩展信息（对象 -- 额外的ip、mac、cpu等信息）  */
        licenseContent.setExtra(param.getExtra());
        return licenseContent;
    }
}
