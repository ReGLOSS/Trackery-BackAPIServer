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
 * description    : 이미지 업로드 기능 사용 시 사용될 S3 기능의 구현체
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 21.		durururuk		최초 생성
 */
@Slf4j
@Service("imageUploadS3ServiceImpl")
@RequiredArgsConstructor
public class ImageUploadS3ServiceImpl implements S3Service {
	private final S3Presigner s3Presigner;

	@Value("${aws.bucketName}")
	private String bucketName;

	/**
	 * Object Get Presigned URL을 요청하는 메서드
	 * @param objectKey 조회할 Object Key
	 * @return PresignedGetUrl
	 */
	@Override
	public String generatePreSignedGetUrl(String objectKey) {
		GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
			.signatureDuration(java.time.Duration.ofMinutes(10))
			.getObjectRequest(getObjectRequest -> getObjectRequest.bucket(bucketName).key(objectKey))
			.build();

		PresignedGetObjectRequest pregisnedRequest = s3Presigner.presignGetObject(presignRequest);
		return pregisnedRequest.url().toString();
	}

	/**
	 * Object Put PresignedURL을 요청하는 메서드입니다.
	 * 먼저 temp폴더에 객체를 추가하고 메타데이터가 저장되면
	 * 그 때 진짜 이미지 폴더로 이동시키기 위해 temp폴더에 먼저 객체를 생성합니다.
	 * @param objectKey 추가될 Object의 key
	 * @return PresignedPutURL
	 */
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
