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
import com.trackery.trackerybackapiserver.domain.image.entity.Image;
import com.trackery.trackerybackapiserver.domain.image.mapper.ImageMapper;
import com.trackery.trackerybackapiserver.domain.location.dto.LocationInfoDto;
import com.trackery.trackerybackapiserver.domain.location.entity.CoordinatePoint;
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
 * 25. 5. 19.		durururul	이미지 조회 단위 테스트 작성
 */
@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

	@Mock
	private ImageMapper imageMapper;
	@Mock
	private CacheManager cacheManager;
	@Mock
	private ImageS3Service imageS3Service;
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
}
