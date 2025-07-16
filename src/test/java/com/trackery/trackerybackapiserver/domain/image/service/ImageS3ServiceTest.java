package com.trackery.trackerybackapiserver.domain.image.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.image.service
 * fileName       : ImageS3ServiceTest
 * author         : durururuk
 * date           : 25. 5. 21.
 * description    : ImageS3Service 단위 테스트
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 21.		durururuk		최초 생성
 * 25. 5. 21.		durururuk		ImageS3ServiceTest 작성
 * 25. 7. 1.		durururuk		변경된 서비스 로직에 맞게 테스트코드 수정
 * 25. 7. 1.		durururuk		더 이상 쓰이지 않는 변수 정리, 재사용 가능한 지역변수 전역변수화
 */
@ExtendWith(MockitoExtension.class)
class ImageS3ServiceTest {

	@Mock
	private S3Presigner s3Presigner;

	@Mock
	private S3Client s3Client;

	@InjectMocks
	private ImageS3Service imageS3Service;

	@BeforeEach
	void setUp() {
		String UPLOAD_BUCKET = "test-upload-bucket";
		String IMAGE_BUCKET = "test-image-bucket";
		ReflectionTestUtils.setField(imageS3Service, "uploadBucket", UPLOAD_BUCKET);
		ReflectionTestUtils.setField(imageS3Service, "imageBucket", IMAGE_BUCKET);
	}

	@Test
	@DisplayName("generatePreSignedGetUrl 성공 테스트 - original")
	void generatePreSignedGetUrl_success_original() throws MalformedURLException {

		String imageKey = "testImage";
		Long userId = 1L;
		String type = "original";
		String expectedUrl = "https://s3.test.com/1/original/testImage-orig.webp";

		PresignedGetObjectRequest mockPresignedRequest = mock(PresignedGetObjectRequest.class);
		when(mockPresignedRequest.url()).thenReturn(new URL(expectedUrl));
		when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(mockPresignedRequest);

		String actualUrl = imageS3Service.generatePreSignedGetUrl(imageKey, userId, type);

		// then
		assertEquals(expectedUrl, actualUrl);
	}

	@Test
	@DisplayName("generatePreSignedGetUrl 성공 테스트 - thumbnail")
	void generatePreSignedGetUrl_success_thumbnail() throws MalformedURLException {

		String imageKey = "testImage";
		Long userId = 1L;
		String type = "thumbnail";
		String expectedUrl = "https://s3.test.com/1/thumbnail/testImage-thumbnail.webp";

		PresignedGetObjectRequest mockPresignedRequest = mock(PresignedGetObjectRequest.class);
		when(mockPresignedRequest.url()).thenReturn(new URL(expectedUrl));
		when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(mockPresignedRequest);

		String actualUrl = imageS3Service.generatePreSignedGetUrl(imageKey, userId, type);

		// then
		assertEquals(expectedUrl, actualUrl);
	}

	@Test
	@DisplayName("generatePreSignedGetUrl 실패 테스트 - 잘못된 타입")
	void generatePreSignedGetUrl_fail_invalidType() {

		String imageKey = "testImage";
		Long userId = 1L;
		String invalidType = "invalid";

		ApiException exception = assertThrows(ApiException.class, () -> {
			imageS3Service.generatePreSignedGetUrl(imageKey, userId, invalidType);
		});
		assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, exception.getErrorCode());

		verify(s3Presigner, never()).presignGetObject(any(GetObjectPresignRequest.class));
	}

	@Test
	@DisplayName("generatePreSignedPutUrl 성공 테스트")
	void generatePreSignedPutUrl_success() throws MalformedURLException {

		String imageKey = "testImage";
		Long userId = 1L;
		String expectedUrl = "https://s3.test.com/temp/1/images/testImage";

		PresignedPutObjectRequest mockPresignedRequest = mock(PresignedPutObjectRequest.class);
		when(mockPresignedRequest.url()).thenReturn(new URL(expectedUrl));
		when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(mockPresignedRequest);

		String actualUrl = imageS3Service.generatePreSignedPutUrl(imageKey, userId);

		assertEquals(expectedUrl, actualUrl);
	}
}