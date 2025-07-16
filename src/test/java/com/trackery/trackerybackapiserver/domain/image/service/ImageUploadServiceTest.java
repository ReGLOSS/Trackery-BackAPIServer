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
 * description    : ImageUploadService 단위 테스트
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 23.		durururuk		최초 생성
 * 25. 4. 23.		durururuk		ImageUploadService Mock 단위 테스트 작성
 * 25. 4. 25.		durururuk		이미지 메타데이터 저장 후 이미지를 s3 임시 폴더에서 이미지 폴더로 이동시키는 작업 추가
 * 25. 7. 1.		durururuk		변경된 서비스 로직에 맞게 테스트코드 수정
 * 25. 7. 1.		durururuk		더 이상 쓰이지 않는 변수 정리, 재사용 가능한 지역변수 전역변수화
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
	private ImageS3Service imageS3Service;


	@Nested
	@DisplayName("Object Put PresignedURL 요청 테스트")
	class getPresignedPutUrlTest {
		@Test
		@DisplayName("성공")
		void success() {
			String imageFileName = "image.jpg";
			String expected = "Presigned Put URL";

			when(imageS3Service.generatePreSignedPutUrl(imageFileName, 1L)).thenReturn("Presigned Put URL");

			String result = imageUploadService.getPresignedPutUrl(imageFileName, 1L);

			assertEquals(expected, result);
		}

		@Test
		@DisplayName("실패 - 파일명이 이미지 확장자가 아닌 경우")
		void failure_1() {
			String imageFileName = "text.txt";

			ApiException expect = new ApiException(ErrorCode.BAD_REQUEST_INVALID_IMAGE_FILE);

			ApiException result = assertThrows(ApiException.class, () -> imageUploadService.getPresignedPutUrl(imageFileName, 1L));

			assertEquals(expect.getMessage(), result.getMessage());
		}
	}

	@Nested
	class saveImageTest {
		private final ImageUploadDto imageUploadDto = new ImageUploadDto();
		private CoordinatePoint coordinatePoint;

		@BeforeEach
		void setUp() {
			ReflectionTestUtils.setField(imageUploadDto, "imageName", "image");
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