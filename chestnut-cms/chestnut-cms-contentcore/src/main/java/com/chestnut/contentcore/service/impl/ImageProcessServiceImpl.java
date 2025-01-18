/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
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
package com.chestnut.contentcore.service.impl;

import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.exception.GlobalException;
import com.chestnut.common.storage.IFileStorageType;
import com.chestnut.common.storage.exception.StorageErrorCode;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.image.ImageHelper;
import com.chestnut.contentcore.core.impl.ResourceType_Image;
import com.chestnut.contentcore.domain.CmsResource;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.dto.ImageCropDTO;
import com.chestnut.contentcore.domain.dto.ImageRotateDTO;
import com.chestnut.contentcore.exception.ContentCoreErrorCode;
import com.chestnut.contentcore.properties.FileStorageTypeProperty;
import com.chestnut.contentcore.service.IImageProcessService;
import com.chestnut.contentcore.service.IResourceService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.util.FileStorageHelper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * ImageProcessServiceImpl
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Service
@RequiredArgsConstructor
public class ImageProcessServiceImpl implements IImageProcessService {

    private final Map<String, IFileStorageType> fileStorageTypes;

    private final ISiteService siteService;

    private final IResourceService resourceService;

    private IFileStorageType getFileStorageType(String type) {
        IFileStorageType fileStorageType = fileStorageTypes.get(IFileStorageType.BEAN_NAME_PREIFX + type);
        Assert.notNull(fileStorageType, () -> StorageErrorCode.UNSUPPORTED_STORAGE_TYPE.exception(type));
        return fileStorageType;
    }

    @Override
    public void cropImage(ImageCropDTO dto) throws IOException {
        CmsResource resource = this.resourceService.getById(dto.getResourceId());
        Assert.notNull(resource, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("resourceId", dto.getResourceId()));

        boolean isImage = ResourceType_Image.ID.equals(resource.getResourceType());
        Assert.isTrue(isImage, ContentCoreErrorCode.ONLY_SUPPORT_IMAGE::exception);

        CmsSite site = this.siteService.getSite(resource.getSiteId());

        if (!resource.getStorageType().equals(FileStorageTypeProperty.getValue(site.getConfigProps()))) {
            throw ContentCoreErrorCode.UNSUPPORTED_RESOURCE_STORAGE.exception();
        }
        // 读取存储配置
        String fileStorageType = FileStorageTypeProperty.getValue(site.getConfigProps());
        IFileStorageType fst = this.getFileStorageType(fileStorageType);
        FileStorageHelper fileStorageHelper = FileStorageHelper.of(fst, site);
        // 站点存储策略与资源一致才能处理
        // 写入磁盘/OSS
        String imageFormat = FilenameUtils.getExtension(resource.getFileName());
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            try (InputStream is = fileStorageHelper.read(resource.getPath())) {
                ImageHelper.of(is).format(imageFormat)
                        .crop(dto.getX(), dto.getY(), dto.getWidth(), dto.getHeight())
                        .to(os);
            }
            fileStorageHelper.write(resource.getPath(), os.toByteArray());
        }
        // 重新生成缩略图
        regenerateThumbnails(resource, imageFormat, fileStorageHelper);
    }

    @Override
    public void rotateImage(ImageRotateDTO dto) throws IOException {
        CmsResource resource = this.resourceService.getById(dto.getResourceId());
        Assert.notNull(resource, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("resourceId", dto.getResourceId()));

        boolean isImage = ResourceType_Image.ID.equals(resource.getResourceType());
        Assert.isTrue(isImage, ContentCoreErrorCode.ONLY_SUPPORT_IMAGE::exception);

        CmsSite site = this.siteService.getSite(resource.getSiteId());

        if (!resource.getStorageType().equals(FileStorageTypeProperty.getValue(site.getConfigProps()))) {
            throw new GlobalException("资源存储方式与当前站点配置不一致");
        }
        // 读取存储配置
        String fileStorageType = FileStorageTypeProperty.getValue(site.getConfigProps());
        IFileStorageType fst = getFileStorageType(fileStorageType);
        FileStorageHelper fileStorageHelper = FileStorageHelper.of(fst, site);
        // 站点存储策略与资源一致才能处理
        // 写入磁盘/OSS
        String imageFormat = FilenameUtils.getExtension(resource.getPath());
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            try (InputStream is = fileStorageHelper.read(resource.getPath())) {
                ImageHelper.of(is).format(imageFormat)
                        .resize(Objects.requireNonNullElse(dto.getWidth(), 0),
                                Objects.requireNonNullElse(dto.getHeight(), 0))
                        .rotate(dto.getRotate())
                        .flip(dto.getFlipX(), dto.getFlipY())
                        .to(os);
            }
            fileStorageHelper.write(resource.getPath(), os.toByteArray());
        }
        // 重新生成缩略图
        regenerateThumbnails(resource, imageFormat, fileStorageHelper);
    }

    public void regenerateThumbnails(CmsResource resource, String imageFormat, FileStorageHelper fileStorageHelper) throws IOException {
        String pathPrefix = StringUtils.substringBefore(resource.getPath(), ".") + "_";
        List<String> pathList = fileStorageHelper.listObjects(pathPrefix);
        for (String path : pathList) {
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                String fileName = StringUtils.substringAfterLast(path, "/");
                String[] size = fileName.substring(fileName.indexOf("_") + 1, fileName.indexOf(".")).split("x");
                try (InputStream is = fileStorageHelper.read(resource.getPath())) {
                    ImageHelper.of(is).format(imageFormat)
                            .resize(Integer.parseInt(size[0]), Integer.parseInt(size[1]))
                            .to(os);
                }
                fileStorageHelper.write(path, os.toByteArray());
            }
        }
    }
}
