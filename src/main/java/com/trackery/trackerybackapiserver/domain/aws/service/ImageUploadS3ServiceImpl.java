package com.trackery.trackerybackapiserver.domain.aws.service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.aws.service
 * fileName       : ImageUploadS3ServiceImpl
 * author         : durururuk
 * date           : 25. 4. 21.
 * description    : 
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 21.		durururuk		최초 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageUploadS3ServiceImpl implements S3Service {
	private final S3Presigner s3Presigner;

	@Value("${aws.bucketName}")
	private String bucketName;

	@Override
	public String generatePreSignedGetUrl(String objectKey) {
		GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
			.signatureDuration(java.time.Duration.ofMinutes(10))
			.getObjectRequest(getObjectRequest -> getObjectRequest.bucket(bucketName).key(objectKey))
			.build();

		PresignedGetObjectRequest pregisnedRequest = s3Presigner.presignGetObject(presignRequest);
		return pregisnedRequest.url().toString();
	}

	@Override
	public String generatePreSignedPutUrl(String objectKey) {
		String keyWithTempFolder = "temp/" + objectKey;

		PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
			.signatureDuration(Duration.ofMinutes(10))
			.putObjectRequest(putObjectRequest -> putObjectRequest.bucket(bucketName).key(keyWithTempFolder))
			.build();

		PresignedPutObjectRequest pregisnedRequest = s3Presigner.presignPutObject(presignRequest);
		return pregisnedRequest.url().toString();
	}
}
