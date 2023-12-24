package com.chestnut.contentcore.core;

import com.chestnut.common.utils.IdUtils;
import com.chestnut.contentcore.config.CMSConfig;
import com.chestnut.contentcore.core.impl.InternalDataType_Catalog;
import com.chestnut.contentcore.core.impl.InternalDataType_Content;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.util.InternalUrlUtils;
import com.chestnut.contentcore.util.SiteUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 站点主题导入上下文
 */
@Getter
@Setter
public class SiteImportContext implements ISiteThemeContext {

    /**
     * 导入临时目录
     */
    public static final String ImportDir = "_import/theme/";

    /**
     * 栏目ID映射<原栏目ID，导入后的栏目ID>
     */
    private Map<Long, Long> catalogIdMap = new HashMap<>();

    /**
     * 内容ID映射<原内容ID，导入后的内容ID>
     */
    private Map<Long, Long> contentIdMap = new HashMap<>();

    /**
     * 资源ID映射<原资源ID，导入后的资源ID>
     */
    private Map<Long, Long> resourceIdMap = new HashMap<>();

    /**
     * 页面部件ID映射<原页面部件ID，导入后的页面部件ID>
     */
    private Map<Long, Long> pageWidgetIdMap = new HashMap<>();

    private final CmsSite site;

    private CmsSite sourceSite;

    private String operator;

    /**
     * 是否在导入后清理临时目录
     */
    private boolean clearTempFile = true;

    public SiteImportContext(CmsSite site) {
        this.site = site;
    }

    public List<File> readDataFiles(String tableName) {
        String siteResourceRoot = SiteUtils.getSiteResourceRoot(site);
        File dbDir = new File(siteResourceRoot + ImportDir + DataDirPath);
        File[] files = dbDir.listFiles(f -> f.getName().startsWith(tableName + SPLITER));
        if (Objects.isNull(files)) {
            return List.of();
        }
        return Stream.of(files).toList();
    }

    public String dealInternalUrl(String iurl) {
        InternalURL internalURL = InternalUrlUtils.parseInternalUrl(iurl);
        if (Objects.nonNull(internalURL)) {
            Long id = switch (internalURL.getType()) {
                case InternalDataType_Catalog.ID -> catalogIdMap.get(internalURL.getId());
                case InternalDataType_Content.ID -> contentIdMap.get(internalURL.getId());
                default -> null;
            };
            if (IdUtils.validate(id)) {
                internalURL.setId(id);
                return internalURL.toIUrl();
            }
        }
        return iurl;
    }

    public void copySiteFiles() throws IOException {
        String siteResourceRoot = SiteUtils.getSiteResourceRoot(site);
        // 复制资源目录文件
        File source = new File(siteResourceRoot + ImportDir + SiteDirPath + sourceSite.getPath());
        FileUtils.copyDirectory(source, new File(siteResourceRoot));
        // 复制发布通道目录文件
        File[] files = new File(siteResourceRoot + ImportDir + SiteDirPath).listFiles(f -> {
            return f.getName().startsWith(sourceSite.getPath() + "_");
        });
        if (Objects.nonNull(files)) {
            for (File file : files) {
                String pp = StringUtils.substringAfterLast(file.getName(), "_");
                File dest = new File(CMSConfig.getResourceRoot() + SiteUtils.getSitePublishPipePath(site.getPath(), pp));
                FileUtils.copyDirectory(file, dest);
            }
        }
    }
    public void clearTempFiles() throws IOException {
        String siteResourceRoot = SiteUtils.getSiteResourceRoot(site);
        FileUtils.deleteDirectory(new File(siteResourceRoot + ImportDir));
    }
}