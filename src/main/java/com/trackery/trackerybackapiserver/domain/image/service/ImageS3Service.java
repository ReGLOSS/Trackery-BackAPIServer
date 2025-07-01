package com.trackery.trackerybackapiserver.domain.image.service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;
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
@Service
@RequiredArgsConstructor
public class ImageS3Service {
	private final S3Presigner s3Presigner;
	private final S3Client s3Client;

	@Value("${AWS_UPLOAD_BUCKET_NAME}")
	private String uploadBucket;
	@Value("${AWS_IMAGE_BUCKET_NAME}")
	private String imageBucket;

	/**
	 * Object Get Presigned URL을 요청하는 메서드
	 * @return PresignedGetUrl
	 */
	public String generatePreSignedGetUrl(String imageKey, Long userId, String type) {
		String actualKey;
		String imageKeyWithSuffix;

		actualKey = switch (type) {
			case ("original") -> {
				imageKeyWithSuffix = imageKey + "-original.webp";
				yield String.format("%s/%s/%s", userId, type, imageKeyWithSuffix);
			}
			case ("thumbnail") -> {
				imageKeyWithSuffix = imageKey + "-thumbnail.webp";
				yield String.format("%s/%s/%s", userId, type, imageKeyWithSuffix);
			}
			default -> throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
		};

		GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
			.signatureDuration(Duration.ofMinutes(10))
			.getObjectRequest(
				getObjectRequest -> getObjectRequest.bucket(imageBucket).key(actualKey)
			)
			.build();

		PresignedGetObjectRequest pregisnedRequest = s3Presigner.presignGetObject(presignRequest);
		return pregisnedRequest.url().toString();
	}

	/**
	 * Object Put PresignedURL을 요청하는 메서드입니다.
	 * 먼저 temp폴더에 객체를 추가하고 메타데이터가 저장되면
	 * 그 때 진짜 이미지 폴더로 이동시키기 위해 temp폴더에 먼저 객체를 생성합니다.
	 * @return PresignedPutURL
	 */
	public String generatePreSignedPutUrl(String imageKey, Long userId) {
		String actualKey = String.format("temp/%s/images/%s", userId, imageKey);

		PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
			.signatureDuration(Duration.ofMinutes(10))
			.putObjectRequest(putObjectRequest -> putObjectRequest.bucket(uploadBucket).key(actualKey))
			.build();

		PresignedPutObjectRequest pregisnedRequest = s3Presigner.presignPutObject(presignRequest);
		return pregisnedRequest.url().toString();
	}
}
