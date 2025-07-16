package com.trackery.trackerybackapiserver.domain.image.service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.image.service
 * fileName       : ImageS3Service
 * author         : durururuk
 * date           : 25. 4. 21.
 * description    : 이미지 업로드 기능 사용 시 사용될 S3 기능의 구현체
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 21.		durururuk		최초 생성
 * 25. 4. 21.		durururuk		이미지 업로드 기능 구현
 * 25. 4. 23.		durururuk		ImageUploadService Mock 단위 테스트 작성
 * 25. 4. 25.		durururuk		이미지 메타데이터 저장 후 이미지를 s3 임시 폴더에서 이미지 폴더로 이동시키는 작업 추가
 * 25. 5. 6.		durururuk		이미지 업로드 시 객체 존재 여부 확인 로직 추가
 * 25. 5. 15.		durururuk		이미지 조회 기능 폴더 지정 오류로 안 되던 문제 수정
 * 25. 6. 30.		durururuk		변경된 이미지 저장 방식에 맞게 ImageS3Service 수정
 * 25. 7. 1.		durururuk		이미지 썸네일을 조회하는 메서드에서 썸네일 DTO를 반환하게 변경
 * 25. 7. 1.		durururuk		경로를 의도한 대로 동작하게 수정
 * 25. 7. 1.		durururuk		경로를 의도한 대로 동작하게 수정
 * 25. 7. 11.		durururuk		PresignedUrl 생성 전에 Head 메서드로 해당 key가 존재하는지 예외 처리 추가
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
				imageKeyWithSuffix = imageKey + "-orig.webp";
				yield String.format("%s/%s/%s", userId, type, imageKeyWithSuffix);
			}
			case ("thumbnail") -> {
				imageKeyWithSuffix = imageKey + "-thumbnail.webp";
				yield String.format("%s/%s/%s", userId, type, imageKeyWithSuffix);
			}
			default -> throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
		};

		try {
			HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
				.bucket(imageBucket)
				.key(actualKey)
				.build();
			
			s3Client.headObject(headObjectRequest);
		} catch (NoSuchKeyException e) {
			throw new ApiException(ErrorCode.NOT_FOUND_IMAGE_OBJECT_KEY);
		} catch (S3Exception e) {
			throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
		}

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
