package com.trackery.trackerybackapiserver.domain.image.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.test.util.ReflectionTestUtils;

import com.github.pagehelper.PageInfo;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.image.dto.ImageDto;
import com.trackery.trackerybackapiserver.domain.image.dto.ImageUpdateRequestDto;
import com.trackery.trackerybackapiserver.domain.image.entity.Image;
import com.trackery.trackerybackapiserver.domain.image.mapper.ImageMapper;
import com.trackery.trackerybackapiserver.domain.location.dto.LocationInfoDto;
import com.trackery.trackerybackapiserver.domain.location.entity.CoordinatePoint;
import com.trackery.trackerybackapiserver.domain.location.service.LocationService;
import com.trackery.trackerybackapiserver.domain.location.entity.JusoSido;
import com.trackery.trackerybackapiserver.domain.location.entity.JusoSigungu;

/**
 *packageName    : com.trackery.trackerybackapiserver.domain.home.service
 * fileName       : ImageServiceTest
 * author         : inari
 * date           : 25. 2. 14.
 * description    : ImageService의 테스트 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 14.        inari       최초 생성
 * 25. 5. 19.		durururuk	이미지 조회 단위 테스트 작성
 * 25. 6. 20.		inari		이미지 삭제 및 수정 테스트 작성
 */
@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

	@Mock
	private ImageMapper imageMapper;
	@Mock
	private CacheManager cacheManager;
	@Mock
	private ImageS3Service imageS3Service;
	@Mock
	private LocationService locationService;
	@InjectMocks
	private ImageService imageService;

	@Test
	@DisplayName("공개 이미지 URL 목록을 정상적으로 조회하는지 테스트")
	void getPublicImageUrls_ShouldReturnImageUrls() {
		// Given
		List<String> expectedUrls = Arrays.asList(
			"http://example.com/image1.jpg",
			"http://example.com/image2.jpg"
		);
		when(imageMapper.selectPublicImageFiles()).thenReturn(expectedUrls);

		// When
		List<String> actualUrls = imageService.getPublicImageUrls();

		// Then
		assertThat(actualUrls).isNotNull()
			.hasSize(2)
			.containsExactlyElementsOf(expectedUrls);
		verify(imageMapper).selectPublicImageFiles();
	}

	@Test
	@DisplayName("이미지가 없을 경우 NOT_FOUND_IMAGE를 반환하는지 테스트")
	void getPublicImageUrls_ShouldReturnEmptyList_WhenNoImages() {
		// Given
		when(imageMapper.selectPublicImageFiles()).thenReturn(null);

		// When & Then
		assertThatThrownBy(() -> imageService.getPublicImageUrls())
			.isInstanceOf(ApiException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_IMAGE);

		verify(imageMapper).selectPublicImageFiles();
	}

	@Test
	@DisplayName("캐시가 정상적으로 초기화되는지 테스트")
	void evictImageCache_ShouldClearCache() {
		// Given
		List<String> mockUrls = List.of("http://example.com/image1.jpg");
		when(imageMapper.selectPublicImageFiles()).thenReturn(mockUrls);

		// When
		// 첫 번째 호출 - 실제 DB 조회
		imageService.getPublicImageUrls();
		// 캐시 삭제
		imageService.evictImageCache();
		// 두 번째 호출 - 캐시가 삭제되었으므로 다시 DB 조회
		imageService.getPublicImageUrls();

		// Then
		// selectPublicImageFiles가 두 번 호출되었는지 확인
		verify(imageMapper, times(2)).selectPublicImageFiles();
	}

	@Nested
	class getImagesTest {
		private Image testImage1;

		private final Long testUserId = 1L;
		private final Long testSidoId = 2L;
		private final Long testSigunguId = 3L;
		private final Long testCoordinatePointId = 4L;
		private final Long testImageId = 5L;
		private final String testPresignedUrl = "testPresignedUrl";
		private final LocationInfoDto testLocationInfo = new LocationInfoDto(127.0495556, 37.5398056, "서울특별시", "강남구");

		@BeforeEach
		void setUp() {
			JusoSido sido = new JusoSido();
			ReflectionTestUtils.setField(sido, "sidoId", testSidoId);
			ReflectionTestUtils.setField(sido, "sidoName", "서울특별시");

			JusoSigungu sigungu = new JusoSigungu();
			ReflectionTestUtils.setField(sigungu, "sigunguId", testSigunguId);
			ReflectionTestUtils.setField(sigungu, "sigunguName", "강남구");
			ReflectionTestUtils.setField(sigungu, "sido", sido);

			Coordinate coordinate = new Coordinate(127.0495556, 37.5398056);
			PrecisionModel precisionModel = new PrecisionModel(PrecisionModel.FLOATING);
			Point testPoint = new Point(coordinate, precisionModel, 4321);

			CoordinatePoint coordPoint = new CoordinatePoint();
			ReflectionTestUtils.setField(coordPoint, "coordinatePointId", testCoordinatePointId);
			ReflectionTestUtils.setField(coordPoint, "coordinatePointName", "테스트 좌표");
			ReflectionTestUtils.setField(coordPoint, "coordinatePointPoint", testPoint);
			ReflectionTestUtils.setField(coordPoint, "sigungu", sigungu);

			testImage1 = Image.builder()
				.coordPoint(coordPoint)
				.imageName("테스트 이미지")
				.imageFile("images/test.jpg")
				.isPublic(1)
				.isDeleted(false)
				.imageType("jpg")
				.imageContent("테스트 이미지 설명")
				.imageDate(LocalDateTime.now())
				.imageRegDate(LocalDateTime.now())
				.userId(testUserId)
				.build();

			ReflectionTestUtils.setField(testImage1, "imageId", testImageId);

		}

		@Test
		@DisplayName("이미지 ID로 단건 조회 테스트 - 성공")
		void getImageByImageId_success() {
			when(imageS3Service.generatePreSignedGetUrl(anyString())).thenReturn(testPresignedUrl);
			when(imageMapper.findImageByImageId(5L)).thenReturn(Optional.of(testImage1));

			ImageDto result = imageService.getImageByImageId(5L);

			assertNotNull(result);
			assertEquals(testImageId, result.getImageId());
			assertEquals(testUserId, result.getUserId());
			assertEquals("테스트 이미지", result.getImageName());
			assertEquals("테스트 이미지 설명", result.getImageContent());
			assertEquals(testPresignedUrl, result.getImageUrl());
			assertEquals("서울특별시", result.getSdName());
			assertEquals("강남구", result.getSggName());

			verify(imageMapper).findImageByImageId(testImageId);
			verify(imageS3Service).generatePreSignedGetUrl(testImage1.getImageFile());
		}

		@Test
		@DisplayName("이미지 ID로 단건 조회 테스트 - 실패 - 이미지를 찾지 못했을 경우")
		void getImageByImageId_failure_1() {
			when(imageMapper.findImageByImageId(5L)).thenReturn(Optional.empty());

			ApiException expect = new ApiException(ErrorCode.NOT_FOUND_IMAGE);

			ApiException result = assertThrows(ApiException.class, () -> imageService.getImageByImageId(5L));

			assertEquals(expect.getMessage(), result.getMessage());

			verify(imageMapper).findImageByImageId(testImageId);
			verify(imageS3Service, never()).generatePreSignedGetUrl(anyString());
		}

		@Nested
		@DisplayName("유저 ID로 다건 조회 테스트")
		class getImagesByUserIdV2Test {
			@Test
			@DisplayName("성공")
			void getImageListByUserIdV2_success() {
				when(imageMapper.findImagesByUserId(testUserId)).thenReturn(List.of(testImage1));
				when(imageS3Service.generatePreSignedGetUrl(anyString())).thenReturn(testPresignedUrl);

				PageInfo<ImageDto> pageInfo = imageService.getImageListByUserIdV2(testUserId, 1, 10);

				assertNotNull(pageInfo);
				assertFalse(pageInfo.getList().isEmpty());
				ImageDto result = pageInfo.getList().get(0);

				assertEquals(testImageId, result.getImageId());
				assertEquals(testUserId, result.getUserId());
				assertEquals(testPresignedUrl, result.getImageUrl());
				assertEquals("서울특별시", result.getSdName());
				assertEquals("강남구", result.getSggName());

				verify(imageMapper).findImagesByUserId(testUserId);
			}

			@Test
			@DisplayName("성공 - 결과 데이터 없음")
			void getImageListByUserIdV2_emptyResult() {
				when(imageMapper.findImagesByUserId(testUserId)).thenReturn(List.of());

				PageInfo<ImageDto> pageInfo = imageService.getImageListByUserIdV2(testUserId, 1, 10);

				assertNotNull(pageInfo);
				assertTrue(pageInfo.getList().isEmpty());

				verify(imageMapper).findImagesByUserId(testUserId);
			}
		}
	}

	@Nested
	@DisplayName("이미지 메타데이터 수정 테스트")
	class UpdateImageMetadataTest {
		private Image existingImage;
		private final Long imageId = 1L;
		private final Long userId = 2L;
		private final Long otherUserId = 3L;

		@BeforeEach
		void setUp() {
			JusoSido sido = new JusoSido();
			ReflectionTestUtils.setField(sido, "sidoId", 1L);
			ReflectionTestUtils.setField(sido, "sidoName", "서울특별시");

			JusoSigungu sigungu = new JusoSigungu();
			ReflectionTestUtils.setField(sigungu, "sigunguId", 1L);
			ReflectionTestUtils.setField(sigungu, "sigunguName", "강남구");
			ReflectionTestUtils.setField(sigungu, "sido", sido);

			Coordinate coordinate = new Coordinate(127.0495556, 37.5398056);
			PrecisionModel precisionModel = new PrecisionModel(PrecisionModel.FLOATING);
			Point testPoint = new Point(coordinate, precisionModel, 4321);

			CoordinatePoint coordPoint = new CoordinatePoint();
			ReflectionTestUtils.setField(coordPoint, "coordinatePointId", 1L);
			ReflectionTestUtils.setField(coordPoint, "coordinatePointName", "테스트 좌표");
			ReflectionTestUtils.setField(coordPoint, "coordinatePointPoint", testPoint);
			ReflectionTestUtils.setField(coordPoint, "sigungu", sigungu);

			existingImage = Image.builder()
				.coordPoint(coordPoint)
				.imageName("기존 이미지")
				.imageFile("images/test.jpg")
				.imageContent("기존 설명")
				.isPublic(0)
				.userId(userId)
				.build();
			ReflectionTestUtils.setField(existingImage, "imageId", imageId);
		}

		@Test
		@DisplayName("성공 - 메타데이터 수정")
		void updateImageMetadata_success() {
			ImageUpdateRequestDto updateRequest = ImageUpdateRequestDto.builder()
				.imageName("수정된 이미지")
				.imageContent("수정된 설명")
				.isPublic(1)
				.build();

			when(imageMapper.findImageByImageId(imageId)).thenReturn(Optional.of(existingImage));
			when(imageMapper.updateImageMetadata(eq(imageId), eq("수정된 이미지"), eq("수정된 설명"), isNull(), eq(1)))
				.thenReturn(1);
			when(imageS3Service.generatePreSignedGetUrl(anyString())).thenReturn("test-url");

			ImageDto result = imageService.updateImageMetadata(imageId, userId, updateRequest);

			assertNotNull(result);
			verify(imageMapper, times(2)).findImageByImageId(imageId);
			verify(imageMapper).updateImageMetadata(eq(imageId), eq("수정된 이미지"), eq("수정된 설명"), isNull(), eq(1));
		}

		@Test
		@DisplayName("실패 - 이미지 존재하지 않음")
		void updateImageMetadata_imageNotFound() {
			ImageUpdateRequestDto updateRequest = ImageUpdateRequestDto.builder()
				.imageName("수정된 이미지")
				.build();

			when(imageMapper.findImageByImageId(imageId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> imageService.updateImageMetadata(imageId, userId, updateRequest))
				.isInstanceOf(ApiException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_IMAGE);

			verify(imageMapper).findImageByImageId(imageId);
			verify(imageMapper, never()).updateImageMetadata(any(), any(), any(), any(), any());
		}

		@Test
		@DisplayName("실패 - 권한 없음 (다른 사용자)")
		void updateImageMetadata_forbidden() {
			ImageUpdateRequestDto updateRequest = ImageUpdateRequestDto.builder()
				.imageName("수정된 이미지")
				.build();

			when(imageMapper.findImageByImageId(imageId)).thenReturn(Optional.of(existingImage));

			assertThatThrownBy(() -> imageService.updateImageMetadata(imageId, otherUserId, updateRequest))
				.isInstanceOf(ApiException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);

			verify(imageMapper).findImageByImageId(imageId);
			verify(imageMapper, never()).updateImageMetadata(any(), any(), any(), any(), any());
		}

		@Test
		@DisplayName("성공 - 지역 정보 포함 메타데이터 수정")
		void updateImageMetadata_withLocation_success() {
			ImageUpdateRequestDto updateRequest = ImageUpdateRequestDto.builder()
				.imageName("수정된 이미지")
				.imageContent("수정된 설명")
				.isPublic(1)
				.latitude(37.5665)
				.longitude(126.978)
				.build();

			CoordinatePoint newCoordinatePoint = new CoordinatePoint();
			ReflectionTestUtils.setField(newCoordinatePoint, "coordinatePointId", 100L);

			when(imageMapper.findImageByImageId(imageId)).thenReturn(Optional.of(existingImage));
			when(imageMapper.updateImageMetadata(eq(imageId), eq("수정된 이미지"), eq("수정된 설명"), isNull(), eq(1)))
				.thenReturn(1);
			when(locationService.insertCoordinatePoint(any())).thenReturn(newCoordinatePoint);
			when(imageMapper.updateImageLocation(imageId, 100L)).thenReturn(1);
			when(imageS3Service.generatePreSignedGetUrl(anyString())).thenReturn("test-url");

			ImageDto result = imageService.updateImageMetadata(imageId, userId, updateRequest);

			assertNotNull(result);
			verify(imageMapper, times(2)).findImageByImageId(imageId);
			verify(imageMapper).updateImageMetadata(eq(imageId), eq("수정된 이미지"), eq("수정된 설명"), isNull(), eq(1));
			verify(locationService).insertCoordinatePoint(any());
			verify(imageMapper).updateImageLocation(imageId, 100L);
		}
	}

	@Nested
	@DisplayName("이미지 삭제 테스트")
	class DeleteImageTest {
		private Image existingImage;
		private final Long imageId = 1L;
		private final Long userId = 2L;
		private final Long otherUserId = 3L;

		@BeforeEach
		void setUp() {
			existingImage = Image.builder()
				.imageName("삭제할 이미지")
				.userId(userId)
				.build();
			ReflectionTestUtils.setField(existingImage, "imageId", imageId);
		}

		@Test
		@DisplayName("성공 - 이미지 삭제")
		void deleteImage_success() {
			when(imageMapper.findImageByImageId(imageId)).thenReturn(Optional.of(existingImage));
			when(imageMapper.deleteImage(imageId)).thenReturn(1);

			assertDoesNotThrow(() -> imageService.deleteImage(imageId, userId));

			verify(imageMapper).findImageByImageId(imageId);
			verify(imageMapper).deleteImage(imageId);
		}

		@Test
		@DisplayName("실패 - 이미지 존재하지 않음")
		void deleteImage_imageNotFound() {
			when(imageMapper.findImageByImageId(imageId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> imageService.deleteImage(imageId, userId))
				.isInstanceOf(ApiException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_IMAGE);

			verify(imageMapper).findImageByImageId(imageId);
			verify(imageMapper, never()).deleteImage(any());
		}

		@Test
		@DisplayName("실패 - 권한 없음 (다른 사용자)")
		void deleteImage_forbidden() {
			when(imageMapper.findImageByImageId(imageId)).thenReturn(Optional.of(existingImage));

			assertThatThrownBy(() -> imageService.deleteImage(imageId, otherUserId))
				.isInstanceOf(ApiException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);

			verify(imageMapper).findImageByImageId(imageId);
			verify(imageMapper, never()).deleteImage(any());
		}
	}
}
