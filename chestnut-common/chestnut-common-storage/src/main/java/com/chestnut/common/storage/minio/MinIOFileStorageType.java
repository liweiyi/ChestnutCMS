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
package com.chestnut.common.storage.minio;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.aliyun.oss.internal.Mimetypes;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.storage.IFileStorageType;
import com.chestnut.common.storage.OSSClient;
import com.chestnut.common.storage.StorageCopyArgs;
import com.chestnut.common.storage.StorageCreateBucketArgs;
import com.chestnut.common.storage.StorageMoveArgs;
import com.chestnut.common.storage.StorageReadArgs;
import com.chestnut.common.storage.StorageRemoveArgs;
import com.chestnut.common.storage.StorageWriteArgs;
import com.chestnut.common.storage.exception.FileStorageException;
import com.chestnut.common.utils.StringUtils;

import io.minio.BucketExistsArgs;
import io.minio.ComposeObjectArgs;
import io.minio.ComposeSource;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;

@Component(IFileStorageType.BEAN_NAME_PREIFX + MinIOFileStorageType.TYPE)
public class MinIOFileStorageType implements IFileStorageType {

	public final static String TYPE = "MinIO";

	private Map<String, OSSClient<MinioClient>> clients = new HashMap<>();

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getName() {
		return I18nUtils.get("{STORAGE.TYPE." + TYPE + "}");
	}

	private OSSClient<MinioClient> getClient(String endpoint, String accessKey, String accessSecret) {
		OSSClient<MinioClient> client = this.clients.get(endpoint);
		if (client == null) {
			client = new OSSClient<>();
			client.setClient(MinioClient.builder().endpoint(endpoint).credentials(accessKey, accessSecret).build());
			this.clients.put(endpoint, client);
		}
		client.setLastActiveTime(System.currentTimeMillis());
		return client;
	}

	@Override
	public boolean testConnection(String endpoint, String accessKey, String accessSecret) {
		OSSClient<MinioClient> client = this.getClient(endpoint, accessKey, accessSecret);
		if (client == null) {
			throw new FileStorageException("OSSClient cannot be empty: " + TYPE);
		}
		try {
			client.getClient().listBuckets();
			return true;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 创建存储桶
	 * 
	 * @param minioClientKey
	 * @param bucketName
	 */
	@Override
	public void createBucket(StorageCreateBucketArgs args) {
		if (StringUtils.isEmpty(args.getBucket())) {
			throw CommonErrorCode.NOT_EMPTY.exception("bucket");
		}
		OSSClient<MinioClient> client = this.getClient(args.getEndpoint(), args.getAccessKey(), args.getAccessSecret());
		this.createBucket0(client, args.getBucket());
	}

	private void createBucket0(OSSClient<MinioClient> client, String bucketName) {
		try {
			BucketExistsArgs bucketExistsArgs = BucketExistsArgs.builder().bucket(bucketName).build();
			if (client.getClient().bucketExists(bucketExistsArgs)) {
				return;
			}
			MakeBucketArgs makeBucketArgs = MakeBucketArgs.builder().bucket(bucketName).build();
			client.getClient().makeBucket(makeBucketArgs);
		} catch (Exception e) {
			throw new FileStorageException(e);
		}
	}

	@Override
	public void reloadClient(String endpoint, String accessKey, String accessSecret) {
		this.clients.remove(endpoint);
		this.getClient(endpoint, accessKey, accessSecret);
	}

	@Override
	public InputStream read(StorageReadArgs args) {
		try {
			OSSClient<MinioClient> client = this.getClient(args.getEndpoint(), args.getAccessKey(),
					args.getAccessSecret());
			GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket(args.getBucket()).object(args.getPath())
					.build();
			return client.getClient().getObject(getObjectArgs);
		} catch (Exception e) {
			throw new FileStorageException(e);
		}
	}

	@Override
	public void write(StorageWriteArgs args) {
		try {
			if (args.getInputStream() == null) {
				throw CommonErrorCode.NOT_EMPTY.exception("inputStream");
			}
			String mimetype = Mimetypes.getInstance().getMimetype(args.getPath());
			OSSClient<MinioClient> client = this.getClient(args.getEndpoint(), args.getAccessKey(),
					args.getAccessSecret());
			PutObjectArgs putObjectArgs = PutObjectArgs.builder().bucket(args.getBucket()).object(args.getPath())
					.contentType(mimetype).stream(args.getInputStream(), args.getInputStream().available(), -1).build();
			client.getClient().putObject(putObjectArgs);
		} catch (Exception e) {
			throw new FileStorageException(e);
		}
	}

	@Override
	public void remove(StorageRemoveArgs args) {
		try {
			OSSClient<MinioClient> client = this.getClient(args.getEndpoint(), args.getAccessKey(),
					args.getAccessSecret());
			RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder().bucket(args.getBucket())
					.object(args.getPath()).build();
			client.getClient().removeObject(removeObjectArgs);
		} catch (Exception e) {
			throw new FileStorageException(e);
		}
	}

	@Override
	public void copy(StorageCopyArgs args) {
		OSSClient<MinioClient> client = this.getClient(args.getEndpoint(), args.getAccessKey(), args.getAccessSecret());
		ComposeSource composeSource = new ComposeSource(
				CopySource.builder().bucket(args.getBucket()).object(args.getSourcePath()).build());
		ComposeObjectArgs objectArgs = ComposeObjectArgs.builder().bucket(args.getBucket())
				.sources(List.of(composeSource)).object(args.getDestPath()).build();
		CopyObjectArgs copyArgs = new CopyObjectArgs(objectArgs);
		try {
			client.getClient().copyObject(copyArgs);
		} catch (Exception e) {
			throw new FileStorageException(e);
		}
	}

	@Override
	public void move(StorageMoveArgs args) {
		OSSClient<MinioClient> client = this.getClient(args.getEndpoint(), args.getAccessKey(), args.getAccessSecret());
		ComposeSource composeSource = new ComposeSource(
				CopySource.builder().bucket(args.getBucket()).object(args.getSourcePath()).build());
		ComposeObjectArgs objectArgs = ComposeObjectArgs.builder().bucket(args.getBucket())
				.sources(List.of(composeSource)).object(args.getDestPath()).build();
		CopyObjectArgs copyArgs = new CopyObjectArgs(objectArgs);
		try {
			client.getClient().copyObject(copyArgs);
		} catch (Exception e) {
			throw new FileStorageException(e);
		}
		// 复制后删除源
		RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder().bucket(args.getBucket())
				.object(args.getSourcePath()).build();
		try {
			client.getClient().removeObject(removeObjectArgs);
		} catch (Exception e) {
			throw new FileStorageException(e);
		}
	}
}
