/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
package com.chestnut.common.storage.cos;

import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.storage.*;
import com.chestnut.common.utils.StringUtils;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.*;
import com.qcloud.cos.region.Region;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component(IFileStorageType.BEAN_NAME_PREIFX + TencentStorageType.TYPE)
public class TencentStorageType implements IFileStorageType {

	public final static String TYPE = "TencentCOS";

	private final Map<String, OSSClient<COSClient>> clients = new HashMap<>();

	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public String getName() {
		return I18nUtils.get("{STORAGE.TYPE." + TYPE + "}");
	}
	
	@Override
	public boolean testConnection(String endpoint, String region, String accessKey, String accessSecret) {
		OSSClient<COSClient> client = this.getClient(endpoint, region, accessKey, accessSecret);
		try {
			client.getClient().listBuckets();
			return true;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	}

	@Override
	public void reloadClient(String endpoint, String region, String accessKey, String accessSecret) {
		OSSClient<COSClient> client = this.clients.get(endpoint);
		if (client != null) {
			client.getClient().shutdown();
			this.clients.remove(endpoint);
		}
		this.getClient(endpoint, region, accessKey, accessSecret);
	}
	
	/**
	 * 创建存储桶
	 */
	@Override
	public void createBucket(StorageCreateBucketArgs args) {
		OSSClient<COSClient> client = this.getClient(args.getEndpoint(), args.getAccessKey(), args.getAccessSecret());
		if (client.getClient().doesBucketExist(args.getBucket())) {
			return;
		}
		CreateBucketRequest request = new CreateBucketRequest(args.getBucket());
		client.getClient().createBucket(request);
	}

	@Override
	public boolean exists(StorageExistArgs args) {
		OSSClient<COSClient> client = this.getClient(args.getEndpoint(), args.getAccessKey(), args.getAccessSecret());
		return client.getClient().doesObjectExist(args.getBucket(), args.getPath());
	}
	
	@Override
	public InputStream read(StorageReadArgs args) {
		OSSClient<COSClient> client = this.getClient(args.getEndpoint(), args.getAccessKey(), args.getAccessSecret());
		COSObject object = client.getClient().getObject(args.getBucket(), args.getPath());
		return object.getObjectContent();
	}

	@Override
	public List<String> list(StorageListArgs args) {
		OSSClient<COSClient> client = this.getClient(args.getEndpoint(), args.getAccessKey(), args.getAccessSecret());

		ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
		listObjectsRequest.setBucketName(args.getBucket());
		listObjectsRequest.setMaxKeys(args.getMaxKeys());
		listObjectsRequest.setPrefix(args.getPrefix());
		ObjectListing objectListing = client.getClient().listObjects(listObjectsRequest);
		return objectListing.getObjectSummaries().stream().map(COSObjectSummary::getKey).toList();
	}

	@Override
	public void write(StorageWriteArgs args) {
		OSSClient<COSClient> client = this.getClient(args.getEndpoint(), args.getAccessKey(), args.getAccessSecret());
		ObjectMetadata objectMetadata = new ObjectMetadata();
		if (Objects.nonNull(args.getLength()) && args.getLength() > 0) {
			objectMetadata.setContentLength(args.getLength());
		}
		PutObjectRequest putObjectRequest = new PutObjectRequest(args.getBucket(), args.getPath(), args.getInputStream(), objectMetadata);
		client.getClient().putObject(putObjectRequest);
	}

	@Override
	public void remove(StorageRemoveArgs args) {
		OSSClient<COSClient> client = this.getClient(args.getEndpoint(), args.getAccessKey(), args.getAccessSecret());
		client.getClient().deleteObject(args.getBucket(), args.getPath());
	}

	@Override
	public void copy(StorageCopyArgs args) {
		OSSClient<COSClient> client = this.getClient(args.getEndpoint(), args.getAccessKey(), args.getAccessSecret());
		client.getClient().copyObject(args.getBucket(), args.getSourcePath(), args.getBucket(), args.getDestPath());
	}

	@Override
	public void move(StorageMoveArgs args) {
		OSSClient<COSClient> client = this.getClient(args.getEndpoint(), args.getAccessKey(), args.getAccessSecret());
		client.getClient().copyObject(args.getBucket(), args.getSourcePath(), args.getBucket(), args.getDestPath());
		// 复制后删除源
		client.getClient().deleteObject(args.getBucket(), args.getSourcePath());
	}

	private OSSClient<COSClient> getClient(String endpoint, String accessKey, String accessSecret) {
		return getClient(endpoint, null, accessKey, accessSecret);
	}
	
	private OSSClient<COSClient> getClient(String endpoint, String region, String accessKey, String accessSecret) {
		OSSClient<COSClient> client = this.clients.get(endpoint);
		if (client == null) {
			client = new OSSClient<>();
			COSCredentials credentials = new BasicCOSCredentials(accessKey, accessSecret);
			if (StringUtils.isEmpty(region)) {
				region = StringUtils.substringAfterLast(
						StringUtils.substringBefore(endpoint, ".myqcloud.com"), ".");
			}
			client.setClient(new COSClient(credentials, new ClientConfig(new Region(region))));
			this.clients.put(endpoint, client);
		}
		client.setLastActiveTime(System.currentTimeMillis());
		return client;
	}
}
