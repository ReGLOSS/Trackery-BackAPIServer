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
import org.springframework.test.util.ReflectionTestUtils;

import com.github.pagehelper.PageInfo;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.image.dto.ImageDto;
import com.trackery.trackerybackapiserver.domain.image.dto.ImageThumbnailDto;
import com.trackery.trackerybackapiserver.domain.image.dto.ImageUpdateRequestDto;
import com.trackery.trackerybackapiserver.domain.image.dto.internal.ImageInfoForThumbnailDto;
import com.trackery.trackerybackapiserver.domain.image.dto.internal.ImageSearchByUserIdDto;
import com.trackery.trackerybackapiserver.domain.image.entity.Image;
import com.trackery.trackerybackapiserver.domain.image.mapper.ImageMapper;
import com.trackery.trackerybackapiserver.domain.location.entity.CoordinatePoint;
import com.trackery.trackerybackapiserver.domain.location.entity.JusoSido;
import com.trackery.trackerybackapiserver.domain.location.entity.JusoSigungu;
import com.trackery.trackerybackapiserver.domain.location.service.LocationService;

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
		private final Long testImageId = 5L;
		private final String testPresignedUrl = "testPresignedUrl";

		@BeforeEach
		void setUp() {
			JusoSido sido = new JusoSido();
			Long testSidoId = 2L;
			ReflectionTestUtils.setField(sido, "sidoId", testSidoId);
			ReflectionTestUtils.setField(sido, "sidoName", "서울특별시");

			JusoSigungu sigungu = new JusoSigungu();
			Long testSigunguId = 3L;
			ReflectionTestUtils.setField(sigungu, "sigunguId", testSigunguId);
			ReflectionTestUtils.setField(sigungu, "sigunguName", "강남구");
			ReflectionTestUtils.setField(sigungu, "sido", sido);

			Coordinate coordinate = new Coordinate(127.0495556, 37.5398056);
			PrecisionModel precisionModel = new PrecisionModel(PrecisionModel.FLOATING);
			org.locationtech.jts.geom.GeometryFactory geometryFactory = new org.locationtech.jts.geom.GeometryFactory(precisionModel, 4326);
			Point testPoint = geometryFactory.createPoint(coordinate);

			CoordinatePoint coordPoint = new CoordinatePoint();
			Long testCoordinatePointId = 4L;
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
		void getOriginalImageByImageId_success() {
			when(imageS3Service.generatePreSignedGetUrl(anyString(), eq(testUserId), eq("original"))).thenReturn(
				testPresignedUrl);
			when(imageMapper.findImageByImageId(5L)).thenReturn(Optional.of(testImage1));

			ImageDto result = imageService.getOriginalImageByImageId(testUserId, 5L);

			assertNotNull(result);
			assertEquals(testImageId, result.getImageId());
			assertEquals(testUserId, result.getUserId());
			assertEquals("테스트 이미지", result.getImageName());
			assertEquals("테스트 이미지 설명", result.getImageContent());
			assertEquals(testPresignedUrl, result.getImageUrl());
			assertEquals("서울특별시", result.getSdName());
			assertEquals("강남구", result.getSggName());

			verify(imageMapper).findImageByImageId(testImageId);
			verify(imageS3Service).generatePreSignedGetUrl(testImage1.getImageName(), testUserId, "original");
		}

		@Test
		@DisplayName("이미지 ID로 단건 조회 테스트 - 실패 - 이미지를 찾지 못했을 경우")
		void getOriginalImageByImageId_failure_1() {
			when(imageMapper.findImageByImageId(5L)).thenReturn(Optional.empty());

			ApiException expect = new ApiException(ErrorCode.NOT_FOUND_IMAGE);

			ApiException result = assertThrows(ApiException.class,
				() -> imageService.getOriginalImageByImageId(testUserId, 5L));

			assertEquals(expect.getMessage(), result.getMessage());

			verify(imageMapper).findImageByImageId(testImageId);
			verify(imageS3Service, never()).generatePreSignedGetUrl(anyString(), any(), anyString());
		}

		@Nested
		@DisplayName("유저 ID로 다건 조회 테스트")
		class getImagesByUserIdV2Test {
			@Test
			@DisplayName("성공")
			void getImageListByUserId_success() {
				ImageSearchByUserIdDto searchDto = ImageSearchByUserIdDto.builder()
					.userId(testUserId)
					.pageNum(1)
					.pageSize(10)
					.excludeAlbumId(null)
					.build();

				ImageInfoForThumbnailDto thumbnailDto = new ImageInfoForThumbnailDto();
				ReflectionTestUtils.setField(thumbnailDto, "imageId", testImageId);
				ReflectionTestUtils.setField(thumbnailDto, "userId", testUserId);
				ReflectionTestUtils.setField(thumbnailDto, "imageName", testImage1.getImageName());

				when(imageMapper.findImageThumbnailsByUserId(searchDto)).thenReturn(List.of(thumbnailDto));
				when(imageS3Service.generatePreSignedGetUrl(anyString(), eq(testUserId), eq("thumbnail"))).thenReturn(
					testPresignedUrl);

				PageInfo<ImageThumbnailDto> pageInfo = imageService.getImageListByUserId(searchDto);

				assertNotNull(pageInfo);
				assertFalse(pageInfo.getList().isEmpty());
				ImageThumbnailDto result = pageInfo.getList().get(0);

				assertEquals(testImageId, result.getImageId());
				assertEquals(testPresignedUrl, result.getThumbnailUrl());

				verify(imageMapper).findImageThumbnailsByUserId(searchDto);
				verify(imageS3Service).generatePreSignedGetUrl(testImage1.getImageName(), testUserId, "thumbnail");
			}

			@Test
			@DisplayName("성공 - 결과 데이터 없음")
			void getImageListByUserId_emptyResult() {
				ImageSearchByUserIdDto searchDto = ImageSearchByUserIdDto.builder()
					.userId(testUserId)
					.pageNum(1)
					.pageSize(10)
					.excludeAlbumId(null)
					.build();

				when(imageMapper.findImageThumbnailsByUserId(searchDto)).thenReturn(List.of());

				PageInfo<ImageThumbnailDto> pageInfo = imageService.getImageListByUserId(searchDto);

				assertNotNull(pageInfo);
				assertTrue(pageInfo.getList().isEmpty());

				verify(imageMapper).findImageThumbnailsByUserId(searchDto);
			}
		}

		@Test
		@DisplayName("실패 - 권한 없음 (비공개 이미지)")
		void getOriginalImageByImageId_forbidden() {
			Image privateImage = Image.builder()
				.imageName("비공개 이미지")
				.imageFile("images/private.jpg")
				.isPublic(0)
				.userId(2L) // 다른 사용자
				.build();
			ReflectionTestUtils.setField(privateImage, "imageId", testImageId);

			when(imageMapper.findImageByImageId(testImageId)).thenReturn(Optional.of(privateImage));

			assertThatThrownBy(() -> imageService.getOriginalImageByImageId(testUserId, testImageId))
				.isInstanceOf(ApiException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);

			verify(imageMapper).findImageByImageId(testImageId);
			verify(imageS3Service, never()).generatePreSignedGetUrl(anyString(), any(), anyString());
		}

		@Test
		@DisplayName("성공 - 다른 사용자의 공개 이미지 조회")
		void getOriginalImageByImageId_publicImage() {
			Image publicImage = Image.builder()
				.coordPoint(testImage1.getCoordPoint())
				.imageName("공개 이미지")
				.imageFile("images/public.jpg")
				.isPublic(1)
				.userId(2L) // 다른 사용자
				.imageContent("공개 이미지 설명")
				.imageDate(LocalDateTime.now())
				.imageRegDate(LocalDateTime.now())
				.build();
			ReflectionTestUtils.setField(publicImage, "imageId", testImageId);

			when(imageMapper.findImageByImageId(testImageId)).thenReturn(Optional.of(publicImage));
			when(imageS3Service.generatePreSignedGetUrl(anyString(), eq(testUserId), eq("original"))).thenReturn(
				testPresignedUrl);

			ImageDto result = imageService.getOriginalImageByImageId(testUserId, testImageId);

			assertNotNull(result);
			assertEquals(testImageId, result.getImageId());
			assertEquals(2L, result.getUserId());
			assertEquals("공개 이미지", result.getImageName());

			verify(imageMapper).findImageByImageId(testImageId);
			verify(imageS3Service).generatePreSignedGetUrl(publicImage.getImageName(), testUserId, "original");
		}
	}

	@Nested
	@DisplayName("시도별 이미지 조회 테스트")
	class GetImagesBySidoTest {
		private final Long testUserId = 1L;

		@Test
		@DisplayName("성공 - 시도별 이미지 목록 반환")
		void getImagesBySido_success() {
			Long sidoId = 11L;
			Image testImage = Image.builder()
				.imageName("테스트 이미지")
				.imageFile("images/test.jpg")
				.userId(testUserId)
				.build();
			Long testImageId = 5L;
			ReflectionTestUtils.setField(testImage, "imageId", testImageId);

			when(imageMapper.findImagesBySidoIdAndUserId(sidoId, testUserId)).thenReturn(List.of(testImage));
			String testPresignedUrl = "testPresignedUrl";
			when(imageS3Service.generatePreSignedGetUrl(anyString(), eq(testUserId), eq("thumbnail"))).thenReturn(
				testPresignedUrl);

			List<ImageThumbnailDto> result = imageService.getImagesBySido(sidoId, testUserId);

			assertNotNull(result);
			assertEquals(1, result.size());
			assertEquals(testImageId, result.get(0).getImageId());
			assertEquals(testPresignedUrl, result.get(0).getThumbnailUrl());

			verify(imageMapper).findImagesBySidoIdAndUserId(sidoId, testUserId);
			verify(imageS3Service).generatePreSignedGetUrl(testImage.getImageName(), testUserId, "thumbnail");
		}

		@Test
		@DisplayName("성공 - 빈 결과")
		void getImagesBySido_emptyResult() {
			Long sidoId = 11L;
			when(imageMapper.findImagesBySidoIdAndUserId(sidoId, testUserId)).thenReturn(List.of());

			List<ImageThumbnailDto> result = imageService.getImagesBySido(sidoId, testUserId);

			assertNotNull(result);
			assertTrue(result.isEmpty());
			verify(imageMapper).findImagesBySidoIdAndUserId(sidoId, testUserId);
		}
	}

	@Nested
	@DisplayName("시군구별 이미지 조회 테스트")
	class GetImagesBySigunguTest {
		private final Long testUserId = 1L;

		@Test
		@DisplayName("성공 - 시군구별 이미지 목록 반환")
		void getImagesBySigungu_success() {
			Long sigunguId = 11200L;
			Image testImage = Image.builder()
				.imageName("테스트 이미지")
				.imageFile("images/test.jpg")
				.userId(testUserId)
				.build();
			Long testImageId = 5L;
			ReflectionTestUtils.setField(testImage, "imageId", testImageId);

			when(imageMapper.findImagesBySigunguIdAndUserId(sigunguId, testUserId)).thenReturn(List.of(testImage));
			String testPresignedUrl = "testPresignedUrl";
			when(imageS3Service.generatePreSignedGetUrl(anyString(), eq(testUserId), eq("thumbnail"))).thenReturn(
				testPresignedUrl);

			List<ImageThumbnailDto> result = imageService.getImagesBySigungu(sigunguId, testUserId);

			assertNotNull(result);
			assertEquals(1, result.size());
			assertEquals(testImageId, result.get(0).getImageId());
			assertEquals(testPresignedUrl, result.get(0).getThumbnailUrl());

			verify(imageMapper).findImagesBySigunguIdAndUserId(sigunguId, testUserId);
			verify(imageS3Service).generatePreSignedGetUrl(testImage.getImageName(), testUserId, "thumbnail");
		}

		@Test
		@DisplayName("성공 - 빈 결과")
		void getImagesBySigungu_emptyResult() {
			Long sigunguId = 11200L;
			when(imageMapper.findImagesBySigunguIdAndUserId(sigunguId, testUserId)).thenReturn(List.of());

			List<ImageThumbnailDto> result = imageService.getImagesBySigungu(sigunguId, testUserId);

			assertNotNull(result);
			assertTrue(result.isEmpty());
			verify(imageMapper).findImagesBySigunguIdAndUserId(sigunguId, testUserId);
		}
	}

	@Nested
	@DisplayName("S3 Presigned URL 생성 테스트")
	class FetchS3PresignedUrlByImageIdTest {
		private final Long testImageId = 5L;

		@Test
		@DisplayName("성공 - S3 Presigned URL 반환")
		void fetchS3PresignedUrlByImageId_success() {
			Long testUserId = 1L;
			Image testImage = Image.builder()
				.imageName("테스트 이미지")
				.imageFile("images/test.jpg")
				.userId(testUserId)
				.build();
			ReflectionTestUtils.setField(testImage, "imageId", testImageId);

			when(imageMapper.findImageByImageId(testImageId)).thenReturn(Optional.of(testImage));
			String testPresignedUrl = "testPresignedUrl";
			when(imageS3Service.generatePreSignedGetUrl(anyString(), eq(testUserId), eq("original"))).thenReturn(
				testPresignedUrl);

			String result = imageService.fetchS3PresignedUrlByImageId(testImageId);

			assertEquals(testPresignedUrl, result);
			verify(imageMapper).findImageByImageId(testImageId);
			verify(imageS3Service).generatePreSignedGetUrl(testImage.getImageFile(), testUserId, "original");
		}

		@Test
		@DisplayName("실패 - 이미지 존재하지 않음")
		void fetchS3PresignedUrlByImageId_imageNotFound() {
			when(imageMapper.findImageByImageId(testImageId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> imageService.fetchS3PresignedUrlByImageId(testImageId))
				.isInstanceOf(ApiException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_IMAGE);

			verify(imageMapper).findImageByImageId(testImageId);
			verify(imageS3Service, never()).generatePreSignedGetUrl(anyString(), any(), anyString());
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
			org.locationtech.jts.geom.GeometryFactory geometryFactory = new org.locationtech.jts.geom.GeometryFactory(precisionModel, 4326);
			Point testPoint = geometryFactory.createPoint(coordinate);

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
			when(imageS3Service.generatePreSignedGetUrl(anyString(), eq(userId), eq("original"))).thenReturn(
				"test-url");

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

			when(imageMapper.findImageByImageId(imageId)).thenReturn(Optional.of(existingImage));
			when(imageMapper.updateImageMetadata(eq(imageId), eq("수정된 이미지"), eq("수정된 설명"), isNull(), eq(1)))
				.thenReturn(1);
			when(locationService.updateCoordinatePoint(eq(1L), any())).thenReturn(1);
			when(imageS3Service.generatePreSignedGetUrl(anyString(), eq(userId), eq("original"))).thenReturn(
				"test-url");

			ImageDto result = imageService.updateImageMetadata(imageId, userId, updateRequest);

			assertNotNull(result);
			verify(imageMapper, times(2)).findImageByImageId(imageId);
			verify(imageMapper).updateImageMetadata(eq(imageId), eq("수정된 이미지"), eq("수정된 설명"), isNull(), eq(1));
			verify(locationService).updateCoordinatePoint(eq(1L), any());
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
