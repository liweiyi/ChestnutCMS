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
package com.chestnut.common.storage.s3;

import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.storage.*;
import com.chestnut.common.utils.Assert;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;
import software.amazon.awssdk.services.s3.waiters.S3Waiter;

import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component(IFileStorageType.BEAN_NAME_PREIFX + AmazonS3FileStorageType.TYPE)
public class AmazonS3FileStorageType implements IFileStorageType {

	public final static String TYPE = "AmazonS3";

	private final Map<String, OSSClient<S3Client>> clients = new HashMap<>();

	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public String getName() {
		return I18nUtils.get("{STORAGE.TYPE." + TYPE + "}");
	}
	
	@Override
	public boolean testConnection(String endPoint, String region, String accessKey, String accessSecret) {
		OSSClient<S3Client> client = this.getClient(endPoint, region, accessKey, accessSecret);
		try {
			client.getClient();
			return true;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	}

	@Override
	public void reloadClient(String endpoint, String region, String accessKey, String accessSecret) {
		OSSClient<S3Client> client = this.clients.get(endpoint);
		if (client != null) {
			client.getClient().close();
			this.clients.remove(endpoint);
		}
		this.getClient(endpoint, region, accessKey, accessSecret);
		log.info("The amazon s3 client reloaded: " + endpoint);
	}
	
	/**
	 * 创建存储桶
	 */
	@Override
	public void createBucket(StorageCreateBucketArgs args) {
		OSSClient<S3Client> client = this.getClient(args.getEndpoint(), args.getRegion(), args.getAccessKey(), args.getAccessSecret());
		try(S3Waiter s3Waiter = client.getClient().waiter()) {
			CreateBucketRequest bucketRequest = CreateBucketRequest.builder()
					.bucket(args.getBucket())
					.build();

			client.getClient().createBucket(bucketRequest);
			HeadBucketRequest bucketRequestWait = HeadBucketRequest.builder()
					.bucket(args.getBucket())
					.build();

			// Wait until the bucket is created and print out the response.
			WaiterResponse<HeadBucketResponse> waiterResponse = s3Waiter.waitUntilBucketExists(bucketRequestWait);
			Optional<HeadBucketResponse> response = waiterResponse.matched().response();
			response.ifPresent(res -> {
				log.info("AmazonS3: bucket [{}] is ready.", res.bucketLocationName());
			});
		} catch (S3Exception e) {
			log.error(e.awsErrorDetails().errorMessage(), e);
		}
	}

	@Override
	public boolean exists(StorageExistArgs args) {
		OSSClient<S3Client> client = this.getClient(args.getEndpoint(), args.getRegion(), args.getAccessKey(), args.getAccessSecret());
		try {
			Assert.notEmpty(args.getBucket(), () -> CommonErrorCode.NOT_EMPTY.exception("bucketName"));
			client.getClient().getBucketAcl(r -> r.bucket(args.getBucket()));
			return true;
		} catch (AwsServiceException ase) {
			// A redirect error or an AccessDenied exception means the bucket exists but it's not in this region
			// or we don't have permissions to it.
			if ((ase.statusCode() == HttpStatusCode.MOVED_PERMANENTLY) || "AccessDenied".equals(ase.awsErrorDetails().errorCode())) {
				return true;
			}
			if (ase.statusCode() == HttpStatusCode.NOT_FOUND) {
				return false;
			}
			throw ase;
		}
	}
	
	@Override
	public InputStream read(StorageReadArgs args) {
		OSSClient<S3Client> client = this.getClient(args.getEndpoint(), args.getRegion(), args.getAccessKey(), args.getAccessSecret());
		try {
			GetObjectRequest objectRequest = GetObjectRequest
					.builder()
					.key(args.getPath())
					.bucket(args.getBucket())
					.build();

			return client.getClient().getObject(objectRequest, ResponseTransformer.toInputStream());
		} catch (S3Exception e) {
			log.error(e.awsErrorDetails().errorMessage(), e);
			throw e;
		}
	}

	@Override
	public List<String> list(StorageListArgs args) {
		OSSClient<S3Client> client = this.getClient(args.getEndpoint(), args.getRegion(), args.getAccessKey(), args.getAccessSecret());
		try {
			ListObjectsV2Request listReq = ListObjectsV2Request.builder()
					.bucket(args.getBucket())
					.maxKeys(args.getMaxKeys())
					.prefix(args.getPrefix())
					.build();

			ListObjectsV2Iterable listRes = client.getClient().listObjectsV2Paginator(listReq);
			return listRes.stream()
					.flatMap(r -> r.contents().stream())
					.map(S3Object::key).toList();
		} catch (S3Exception e) {
			log.error(e.awsErrorDetails().errorMessage(), e);
			throw e;
		}
	}

	@Override
	public void write(StorageWriteArgs args) {
		OSSClient<S3Client> client = this.getClient(args.getEndpoint(), args.getRegion(), args.getAccessKey(), args.getAccessSecret());
		try {
			PutObjectRequest putOb = PutObjectRequest.builder()
					.bucket(args.getBucket())
					.key(args.getPath())
					.build();

			client.getClient().putObject(putOb, RequestBody.fromInputStream(args.getInputStream(), args.getLength()));
		} catch (S3Exception e) {
			log.error(e.awsErrorDetails().errorMessage(), e);
			throw e;
		}
	}

	@Override
	public void remove(StorageRemoveArgs args) {
		OSSClient<S3Client> client = this.getClient(args.getEndpoint(), args.getRegion(), args.getAccessKey(), args.getAccessSecret());
		try {
			DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
					.bucket(args.getBucket())
					.key(args.getPath())
					.build();

			client.getClient().deleteObject(deleteObjectRequest);
		} catch (S3Exception e) {
			log.error(e.awsErrorDetails().errorMessage(), e);
			throw e;
		}
	}

	@Override
	public void copy(StorageCopyArgs args) {
		OSSClient<S3Client> client = this.getClient(args.getEndpoint(), args.getRegion(), args.getAccessKey(), args.getAccessSecret());
		try {
			CopyObjectRequest copyObjectRequest = CopyObjectRequest.builder()
					.sourceBucket(args.getBucket())
					.sourceKey(args.getSourcePath())
					.destinationBucket(args.getBucket())
					.destinationKey(args.getDestPath())
					.build();

			client.getClient().copyObject(copyObjectRequest);
		} catch (S3Exception e) {
			log.error(e.awsErrorDetails().errorMessage(), e);
			throw e;
		}
	}

	@Override
	public void move(StorageMoveArgs args) {
		OSSClient<S3Client> client = this.getClient(args.getEndpoint(), args.getRegion(), args.getAccessKey(), args.getAccessSecret());
		try {
			CopyObjectRequest copyObjectRequest = CopyObjectRequest.builder()
					.sourceBucket(args.getBucket())
					.sourceKey(args.getSourcePath())
					.destinationBucket(args.getBucket())
					.destinationKey(args.getDestPath())
					.build();
			client.getClient().copyObject(copyObjectRequest);
			// 复制后删除源
			client.getClient().deleteObject(b -> b.bucket(args.getBucket()).key(args.getSourcePath()));
		} catch (S3Exception e) {
			log.error(e.awsErrorDetails().errorMessage(), e);
			throw e;
		}
	}
	
	private OSSClient<S3Client> getClient(String endpoint, String region, String accessKey, String accessSecret) {
		OSSClient<S3Client> client = this.clients.get(endpoint);
		if (client == null) {
			client = new OSSClient<>();
			S3Client s3Client = S3Client.builder()
					.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, accessSecret)))
					.region(Region.AWS_GLOBAL)
					.endpointOverride(URI.create(endpoint))
					.serviceConfiguration(S3Configuration.builder()
							.pathStyleAccessEnabled(false)
							.chunkedEncodingEnabled(false)
							.build())
					.build();
			client.setClient(s3Client);
			this.clients.put(endpoint, client);
		}
		client.setLastActiveTime(System.currentTimeMillis());
		return client;
	}

	@PreDestroy
	public void destroy() {
		this.clients.values().forEach(client -> client.getClient().close());
	}
}
