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
package com.chestnut.common.storage.oss;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.CopyObjectResult;
import com.aliyun.oss.model.CreateBucketRequest;
import com.aliyun.oss.model.ListObjectsV2Request;
import com.aliyun.oss.model.ListObjectsV2Result;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.PutObjectRequest;
import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.storage.IFileStorageType;
import com.chestnut.common.storage.OSSClient;
import com.chestnut.common.storage.StorageCopyArgs;
import com.chestnut.common.storage.StorageCreateBucketArgs;
import com.chestnut.common.storage.StorageExistArgs;
import com.chestnut.common.storage.StorageListArgs;
import com.chestnut.common.storage.StorageListResult;
import com.chestnut.common.storage.StorageMoveArgs;
import com.chestnut.common.storage.StorageReadArgs;
import com.chestnut.common.storage.StorageRemoveArgs;
import com.chestnut.common.storage.StorageWriteArgs;
import com.chestnut.common.storage.exception.FileStorageException;

@Component(IFileStorageType.BEAN_NAME_PREIFX + AliyunFileStorageType.TYPE)
public class AliyunFileStorageType implements IFileStorageType {

	public final static String TYPE = "AliyunOSS";

	private Map<String, OSSClient<OSS>> clients = new HashMap<>();

	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public String getName() {
		return I18nUtils.get("{STORAGE.TYPE." + TYPE + "}");
	}
	
	@Override
	public boolean testConnection(String endPoint, String accessKey, String accessSecret) {
		OSSClient<OSS> client = this.getClient(endPoint, accessKey, accessSecret);
		try {
			client.getClient().getUserQosInfo();
			return true;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	}

	@Override
	public void reloadClient(String endPoint, String accessKey, String accessSecret) {
		OSSClient<OSS> client = this.getClient(endPoint, accessKey, accessSecret);
		if (client != null) {
			client.getClient().shutdown();
			this.clients.remove(endPoint);
		}
		this.getClient(endPoint, accessKey, accessSecret);
	}
	
	/**
	 * 创建存储桶
	 * 
	 * @param bucketName
	 */
	@Override
	public void createBucket(StorageCreateBucketArgs args) {
		OSSClient<OSS> client = this.getClient(args.getEndpoint(), args.getAccessKey(), args.getAccessSecret());
		if (client.getClient().doesBucketExist(args.getBucket())) {
			return;
		}
		CreateBucketRequest request = new CreateBucketRequest(args.getBucket());
		request.setCannedACL(CannedAccessControlList.PublicRead);
		client.getClient().createBucket(request);
	}

	@Override
	public boolean exists(StorageExistArgs args) {
		OSSClient<OSS> client = this.getClient(args.getEndpoint(), args.getAccessKey(), args.getAccessSecret());
		return client.getClient().doesObjectExist(args.getBucket(), args.getPath());
	}
	
	@Override
	public InputStream read(StorageReadArgs args) {
		OSSClient<OSS> client = this.getClient(args.getEndpoint(), args.getAccessKey(), args.getAccessSecret());
		OSSObject object = client.getClient().getObject(args.getBucket(), args.getPath());
		return object.getObjectContent();
	}
	
	public StorageListResult<OSSObjectSummary> list(StorageListArgs args) {
		OSSClient<OSS> client = this.getClient(args.getEndpoint(), args.getAccessKey(), args.getAccessSecret());
		
		ListObjectsV2Request listObjectsV2Request = new ListObjectsV2Request()
				.withBucketName(args.getBucket())
				.withContinuationToken(args.getContinuationToken())
				.withMaxKeys(args.getMaxKeys());
		ListObjectsV2Result listObjectsV2 = client.getClient().listObjectsV2(listObjectsV2Request);
		StorageListResult<OSSObjectSummary> storageListResult = new StorageListResult<>();
		storageListResult.setNextContinuationToken(listObjectsV2.getNextContinuationToken());
		storageListResult.setObjects(listObjectsV2.getObjectSummaries());
		return storageListResult;
	}

	@Override
	public void write(StorageWriteArgs args) {
		OSSClient<OSS> client = this.getClient(args.getEndpoint(), args.getAccessKey(), args.getAccessSecret());
		PutObjectRequest putObjectRequest = new PutObjectRequest(args.getBucket(), args.getPath(), args.getInputStream());
		client.getClient().putObject(putObjectRequest);
	}

	@Override
	public void remove(StorageRemoveArgs args) {
		OSSClient<OSS> client = this.getClient(args.getEndpoint(), args.getAccessKey(), args.getAccessSecret());
		client.getClient().deleteObject(args.getBucket(), args.getPath());
	}

	@Override
	public void copy(StorageCopyArgs args) {
		OSSClient<OSS> client = this.getClient(args.getEndpoint(), args.getAccessKey(), args.getAccessSecret());
		CopyObjectResult res = client.getClient().copyObject(args.getBucket(), args.getSourcePath(), args.getBucket(), args.getDestPath());
		if (!res.getResponse().isSuccessful()) {
			throw new FileStorageException(res.getResponse().getErrorResponseAsString());
		}
	}

	@Override
	public void move(StorageMoveArgs args) {
		OSSClient<OSS> client = this.getClient(args.getEndpoint(), args.getAccessKey(), args.getAccessSecret());
		CopyObjectResult res = client.getClient().copyObject(args.getBucket(), args.getSourcePath(), args.getBucket(), args.getDestPath());
		if (!res.getResponse().isSuccessful()) {
			throw new FileStorageException(res.getResponse().getErrorResponseAsString());
		}
		// 复制后删除源
		client.getClient().deleteObject(args.getBucket(), args.getSourcePath());
	}
	
	private OSSClient<OSS> getClient(String endpoint, String accessKey, String accessSecret) {
		OSSClient<OSS> client = this.clients.get(endpoint);
		if (client == null) {
			client = new OSSClient<>();
			client.setClient(new OSSClientBuilder().build(endpoint, accessKey, accessSecret));
			this.clients.put(endpoint, client);
		}
		client.setLastActiveTime(System.currentTimeMillis());
		return client;
	}
}
