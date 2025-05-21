package com.trackery.trackerybackapiserver.domain.image.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@ExtendWith(MockitoExtension.class)
class ImageS3ServiceTest {

	@Mock
	private S3Presigner s3Presigner;

	@Mock
	private S3Client s3Client;

	@InjectMocks
	private ImageS3Service imageS3Service;

	private final String TEMP_FOLDER = "temp/";
	private final String IMAGES_FOLDER = "images/";

	@BeforeEach
	void setUp() {
		String BUCKET_NAME = "test-bucket";
		ReflectionTestUtils.setField(imageS3Service, "bucketName", BUCKET_NAME);
	}

	@Test
	@DisplayName("generatePreSignedGetUrl 성공 테스트")
	void generatePreSignedGetUrl_success() throws MalformedURLException {

		String objectKey = "testObjectKey";
		String expectedUrl = "https://s3.test.com/images/testObjectKey";

		PresignedGetObjectRequest mockPresignedRequest = mock(PresignedGetObjectRequest.class);
		when(mockPresignedRequest.url()).thenReturn(new URL(expectedUrl));

		when(s3Client.headObject(ArgumentMatchers.<Consumer<HeadObjectRequest.Builder>>any()))
			.thenReturn(HeadObjectResponse.builder().build());
		when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(mockPresignedRequest);

		String actualUrl = imageS3Service.generatePreSignedGetUrl(objectKey);

		// then
		assertEquals(expectedUrl, actualUrl);
	}

	@Test
	@DisplayName("generatePreSignedGetUrl 실패 테스트 - NoSuchKeyException")
	void generatePreSignedGetUrl_fail_noSuchKey() {

		String objectKey = "nonExistentKey";
		when(s3Client.headObject(ArgumentMatchers.<Consumer<HeadObjectRequest.Builder>>any())).thenThrow(
			NoSuchKeyException.class);

		ApiException exception = assertThrows(ApiException.class, () -> {
			imageS3Service.generatePreSignedGetUrl(objectKey);
		});
		assertEquals(ErrorCode.NOT_FOUND_IMAGE_OBJECT_KEY, exception.getErrorCode());

		verify(s3Presigner, never()).presignGetObject(any(GetObjectPresignRequest.class));
	}

	@Test
	@DisplayName("generatePreSignedPutUrl 성공 테스트")
	void generatePreSignedPutUrl_success() throws MalformedURLException {

		String objectKey = "testObjectKey";
		String expectedUrl = "https://s3.test.com/temp/testObjectKey";

		PresignedPutObjectRequest mockPresignedRequest = mock(PresignedPutObjectRequest.class);
		when(mockPresignedRequest.url()).thenReturn(new URL(expectedUrl));
		when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(mockPresignedRequest);

		String actualUrl = imageS3Service.generatePreSignedPutUrl(objectKey);

		assertEquals(expectedUrl, actualUrl);
	}

	@Test
	@DisplayName("moveObjectTempToImageFolder 성공 테스트")
	void moveObjectTempToImageFolder_success() {

		String objectKey = "testObjectKey";

		when(s3Client.headObject(ArgumentMatchers.<Consumer<HeadObjectRequest.Builder>>any())).thenReturn(
			HeadObjectResponse.builder().build());

		imageS3Service.moveObjectTempToImageFolder(objectKey);

		verify(s3Client, times(1)).copyObject(any(CopyObjectRequest.class));
		verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));
	}

	@Test
	@DisplayName("moveObjectTempToImageFolder 실패 테스트 - NoSuchKeyException")
	void moveObjectTempToImageFolder_fail_noSuchKey() {

		String objectKey = "nonExistentKey";
		when(s3Client.headObject(ArgumentMatchers.<Consumer<HeadObjectRequest.Builder>>any()))
			.thenThrow(NoSuchKeyException.class);

		ApiException exception = assertThrows(ApiException.class, () -> {
			imageS3Service.moveObjectTempToImageFolder(objectKey);
		});
		assertEquals(ErrorCode.NOT_FOUND_IMAGE_OBJECT_KEY, exception.getErrorCode());

		verify(s3Client, never()).copyObject(any(CopyObjectRequest.class));
		verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
	}
}