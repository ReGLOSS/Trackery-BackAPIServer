package com.trackery.trackerybackapiserver.domain.image.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.trackery.trackerybackapiserver.domain.aws.service.S3Service;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.image.dto.upload.ImageUploadDto;
import com.trackery.trackerybackapiserver.domain.image.entity.Image;
import com.trackery.trackerybackapiserver.domain.image.mapper.ImageMapper;
import com.trackery.trackerybackapiserver.domain.location.dto.CoordinateDto;
import com.trackery.trackerybackapiserver.domain.location.entity.CoordinatePoint;
import com.trackery.trackerybackapiserver.domain.location.service.LocationService;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.image.service
 * fileName       : ImageUploadServiceTest
 * author         : durururuk
 * date           : 25. 4. 23.
 * description    : 
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 23.		durururuk		최초 생성
 */
@ExtendWith(MockitoExtension.class)
class ImageUploadServiceTest {
	@InjectMocks
	private ImageUploadService imageUploadService;

	@Mock
	private ImageMapper imageMapper;

	@Mock
	private LocationService locationService;

	@Mock
	private S3Service imageUploadS3ServiceImpl;


	@Nested
	@DisplayName("Object Put PresignedURL 요청 테스트")
	class getPresignedPutUrlTest {
		@Test
		@DisplayName("성공")
		void success() {
			String imageFileName = "image.jpg";
			String expected = "Presigned Put URL";

			when(imageUploadS3ServiceImpl.generatePreSignedPutUrl(imageFileName)).thenReturn("Presigned Put URL");

			String result = imageUploadService.getPresignedPutUrl(imageFileName);

			assertEquals(expected, result);
		}

		@Test
		@DisplayName("실패 - 파일명이 이미지 확장자가 아닌 경우")
		void failure_1() {
			String imageFileName = "text.txt";

			ApiException expect = new ApiException(ErrorCode.BAD_REQUEST_INVALID_IMAGE_FILE);

			ApiException result = assertThrows(ApiException.class, () -> imageUploadService.getPresignedPutUrl(imageFileName));

			assertEquals(expect.getMessage(), result.getMessage());
		}
	}

	@Nested
	class saveImageTest {
		private ImageUploadDto imageUploadDto = new ImageUploadDto();
		private CoordinatePoint coordinatePoint;

		@BeforeEach
		void setUp() {
			ReflectionTestUtils.setField(imageUploadDto, "imageName", "image.jpg");
			ReflectionTestUtils.setField(imageUploadDto, "imageType", "jpg");
			ReflectionTestUtils.setField(imageUploadDto, "description", "이미지 설명");
			ReflectionTestUtils.setField(imageUploadDto, "longitude", 123.123);
			ReflectionTestUtils.setField(imageUploadDto, "latitude", 31.31);
			ReflectionTestUtils.setField(imageUploadDto, "dateString", "2024 / 9 / 27");
			ReflectionTestUtils.setField(imageUploadDto, "isPublic", true);

			coordinatePoint = mock(CoordinatePoint.class);
		}

		@Test
		@DisplayName("성공")
		void success() {
			doNothing().when(imageMapper).insertImage(any(Image.class));
			when(locationService.insertCoordinatePoint(any(CoordinateDto.class))).thenReturn(coordinatePoint);

			Image result = imageUploadService.saveImage(1L, imageUploadDto);

			assertEquals(imageUploadDto.getImageName(), result.getImageName());
			assertEquals(coordinatePoint, result.getCoordPoint());
			assertEquals(1, result.getIsPublic());
			assertEquals(LocalDateTime.of(2024, 9, 27, 0, 0, 0), result.getImageDate());
		}
	}
}