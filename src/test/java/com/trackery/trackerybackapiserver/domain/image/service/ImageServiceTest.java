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
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
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
import com.trackery.trackerybackapiserver.domain.tag.service.TagService;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.image.service
 * fileName       : ImageServiceTest
 * author         : inari
 * date           : 25. 2. 14.
 * description    : ImageService의 테스트 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 14.		durururuk		최초 생성
 * 25. 2. 14.		inari		랜덤이미지 가져오기 구현
 * 25. 2. 17.		inari		테스트 코드 수정
 * 25. 2. 18.		inari		dev 병합후 랜딩페이지 엔드포인트 수정
 * 25. 2. 18.		inari		랜딩페이지 테스트코드 복구
 * 25. 2. 19.		inari		테스트코드 수정
 * 25. 3. 14.		durururuk		테스트 코드 작성
 * 25. 4. 18.		durururuk		이미지 업로드 기능 구현 전 공간 관련 기능 정리
 * 25. 5. 15.		durururuk		이미지 조회 기능 구현
 * 25. 5. 19.		durururuk		ImageServiceTest 단위 테스트 작성
 * 25. 6. 17.		durururuk		getImageListByUserIdV2 서비스 테스트 코드 작성
 * 25. 6. 20.		inari		이미지 수정 및 삭제기능 추가
 * 25. 6. 22.		inari		테스트파일 수정
 * 25. 7. 1.		durururuk		변경된 서비스 로직에 맞게 테스트코드 수정
 * 25. 7. 1.		durururuk		더 이상 쓰이지 않는 변수 정리, 재사용 가능한 지역변수 전역변수화
 * 25. 7. 8.		durururuk		내 이미지 조회 시 조회 결과에서 제외될 앨범 ID 파라미터 추가
 * 25. 7. 8.		durururuk		변경된 로직에 맞게 테스트 코드 수정
 * 25. 7. 10.		inari		이미지 단건 조회시 태그 추가
 * 25. 7. 10.		durururuk		변경된 로직에 맞게 테스트 코드 수정
 * 25. 7. 14.		inari		테스트코드 수정 및 adoc 변경
 * 25. 7. 15.		inari		필요없는 eq 삭제
 * 25. 7. 15.		durururuk		이벤트 publish 관련 모킹 추가
 * 25. 7. 15.		inari		ImageServiceTest 테스트 코드 작성
 */
@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

	@Mock
	private ImageMapper imageMapper;
	@Mock
	private ImageS3Service imageS3Service;
	@Mock
	private LocationService locationService;
	@Mock
	private TagService tagService;
	@Mock
	private ApplicationEventPublisher applicationEventPublisher;
	@InjectMocks
	private ImageService imageService;

	@Test
	@DisplayName("공개 이미지 URL 목록을 정상적으로 조회하는지 테스트")
	void getPublicImageUrls_ShouldReturnImageUrls() {
		// Given
		List<String> expectedUrls = Arrays.asList("http://example.com/image1.jpg", "http://example.com/image2.jpg");
		when(imageMapper.selectPublicImageFiles()).thenReturn(expectedUrls);

		// When
		List<String> actualUrls = imageService.getPublicImageUrls();

		// Then
		assertThat(actualUrls).isNotNull().hasSize(2).containsExactlyElementsOf(expectedUrls);
		verify(imageMapper).selectPublicImageFiles();
	}

	@Test
	@DisplayName("이미지가 없을 경우 NOT_FOUND_IMAGE를 반환하는지 테스트")
	void getPublicImageUrls_ShouldReturnEmptyList_WhenNoImages() {
		// Given
		when(imageMapper.selectPublicImageFiles()).thenReturn(null);

		// When & Then
		assertThatThrownBy(() -> imageService.getPublicImageUrls()).isInstanceOf(ApiException.class)
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
			org.locationtech.jts.geom.GeometryFactory geometryFactory = new org.locationtech.jts.geom.GeometryFactory(
				precisionModel, 4326);
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
				.isPublic(1)
				.isDeleted(false)
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
			Image privateImage = Image.builder().imageName("비공개 이미지").isPublic(0).userId(2L) // 다른 사용자
				.build();
			ReflectionTestUtils.setField(privateImage, "imageId", testImageId);

			when(imageMapper.findImageByImageId(testImageId)).thenReturn(Optional.of(privateImage));

			assertThatThrownBy(() -> imageService.getOriginalImageByImageId(testUserId, testImageId)).isInstanceOf(
				ApiException.class).hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);

			verify(imageMapper).findImageByImageId(testImageId);
			verify(imageS3Service, never()).generatePreSignedGetUrl(anyString(), any(), anyString());
		}

		@Test
		@DisplayName("성공 - 다른 사용자의 공개 이미지 조회")
		void getOriginalImageByImageId_publicImage() {
			Image publicImage = Image.builder()
				.coordPoint(testImage1.getCoordPoint())
				.imageName("공개 이미지")
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
			Image testImage = Image.builder().imageName("테스트 이미지").userId(testUserId).build();
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
			Image testImage = Image.builder().imageName("테스트 이미지").userId(testUserId).build();
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
			Image testImage = Image.builder().imageName("테스트 이미지").userId(testUserId).build();
			ReflectionTestUtils.setField(testImage, "imageId", testImageId);

			when(imageMapper.findImageByImageId(testImageId)).thenReturn(Optional.of(testImage));
			String testPresignedUrl = "testPresignedUrl";
			when(imageS3Service.generatePreSignedGetUrl(anyString(), eq(testUserId), eq("original"))).thenReturn(
				testPresignedUrl);

			String result = imageService.fetchS3PresignedUrlByImageId(testImageId);

			assertEquals(testPresignedUrl, result);
			verify(imageMapper).findImageByImageId(testImageId);
			verify(imageS3Service).generatePreSignedGetUrl(testImage.getImageName(), testUserId, "original");
		}

		@Test
		@DisplayName("실패 - 이미지 존재하지 않음")
		void fetchS3PresignedUrlByImageId_imageNotFound() {
			when(imageMapper.findImageByImageId(testImageId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> imageService.fetchS3PresignedUrlByImageId(testImageId)).isInstanceOf(
				ApiException.class).hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_IMAGE);

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
			org.locationtech.jts.geom.GeometryFactory geometryFactory = new org.locationtech.jts.geom.GeometryFactory(
				precisionModel, 4326);
			Point testPoint = geometryFactory.createPoint(coordinate);

			CoordinatePoint coordPoint = new CoordinatePoint();
			ReflectionTestUtils.setField(coordPoint, "coordinatePointId", 1L);
			ReflectionTestUtils.setField(coordPoint, "coordinatePointName", "테스트 좌표");
			ReflectionTestUtils.setField(coordPoint, "coordinatePointPoint", testPoint);
			ReflectionTestUtils.setField(coordPoint, "sigungu", sigungu);

			existingImage = Image.builder()
				.coordPoint(coordPoint)
				.imageName("기존 이미지")
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
			when(imageMapper.updateImageMetadata(imageId, "수정된 이미지", "수정된 설명", null, 1)).thenReturn(1);
			doNothing().when(locationService).updateImageLocation(1L, null, null, (imageId));
			doNothing().when(tagService).processTagRemoval(imageId, updateRequest);
			doNothing().when(tagService).processTagAddition(imageId, updateRequest);
			when(tagService.getTagsForImageDisplay(imageId)).thenReturn(List.of());
			when(imageS3Service.generatePreSignedGetUrl(anyString(), eq(userId), eq("original"))).thenReturn(
				"test-url");

			ImageDto result = imageService.updateImageMetadata(imageId, userId, updateRequest);

			assertNotNull(result);
			verify(imageMapper, times(2)).findImageByImageId(imageId);
			verify(imageMapper).updateImageMetadata(imageId, "수정된 이미지", "수정된 설명", null, 1);
			verify(locationService).updateImageLocation(1L, null, null, imageId);
			verify(tagService).processTagRemoval(imageId, updateRequest);
			verify(tagService).processTagAddition(imageId, updateRequest);
		}

		@Test
		@DisplayName("실패 - 이미지 존재하지 않음")
		void updateImageMetadata_imageNotFound() {
			ImageUpdateRequestDto updateRequest = ImageUpdateRequestDto.builder().imageName("수정된 이미지").build();

			when(imageMapper.findImageByImageId(imageId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> imageService.updateImageMetadata(imageId, userId, updateRequest)).isInstanceOf(
				ApiException.class).hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_IMAGE);

			verify(imageMapper).findImageByImageId(imageId);
			verify(imageMapper, never()).updateImageMetadata(any(), any(), any(), any(), any());
		}

		@Test
		@DisplayName("실패 - 권한 없음 (다른 사용자)")
		void updateImageMetadata_forbidden() {
			ImageUpdateRequestDto updateRequest = ImageUpdateRequestDto.builder().imageName("수정된 이미지").build();

			when(imageMapper.findImageByImageId(imageId)).thenReturn(Optional.of(existingImage));

			assertThatThrownBy(
				() -> imageService.updateImageMetadata(imageId, otherUserId, updateRequest)).isInstanceOf(
				ApiException.class).hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);

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
			when(imageMapper.updateImageMetadata(imageId, "수정된 이미지", "수정된 설명", null, 1)).thenReturn(1);
			doNothing().when(locationService).updateImageLocation(1L, 37.5665, 126.978, imageId);
			doNothing().when(tagService).processTagRemoval(imageId, updateRequest);
			doNothing().when(tagService).processTagAddition(imageId, updateRequest);
			when(tagService.getTagsForImageDisplay(imageId)).thenReturn(List.of());
			when(imageS3Service.generatePreSignedGetUrl(anyString(), eq(userId), eq("original"))).thenReturn(
				"test-url");

			ImageDto result = imageService.updateImageMetadata(imageId, userId, updateRequest);

			assertNotNull(result);
			verify(imageMapper, times(2)).findImageByImageId(imageId);
			verify(imageMapper).updateImageMetadata(imageId, "수정된 이미지", "수정된 설명", null, 1);
			verify(locationService).updateImageLocation(1L, 37.5665, 126.978, imageId);
			verify(tagService).processTagRemoval(imageId, updateRequest);
			verify(tagService).processTagAddition(imageId, updateRequest);
			verify(tagService, times(1)).getTagsForImageDisplay(imageId);
		}

		@Test
		@DisplayName("성공 - 날짜 정보 포함 메타데이터 수정")
		void updateImageMetadata_withDate_success() {
			LocalDateTime newDate = LocalDateTime.of(2023, 6, 15, 12, 0);
			ImageUpdateRequestDto updateRequest = ImageUpdateRequestDto.builder()
				.imageName("수정된 이미지")
				.imageContent("수정된 설명")
				.isPublic(1)
				.imageDate(newDate)
				.build();

			when(imageMapper.findImageByImageId(imageId)).thenReturn(Optional.of(existingImage));
			when(imageMapper.updateImageMetadata(imageId, "수정된 이미지", "수정된 설명", newDate, 1)).thenReturn(1);
			doNothing().when(locationService).updateImageLocation(1L, null, null, imageId);
			doNothing().when(tagService).processTagRemoval(imageId, updateRequest);
			doNothing().when(tagService).processTagAddition(imageId, updateRequest);
			when(tagService.getTagsForImageDisplay(imageId)).thenReturn(List.of());
			when(imageS3Service.generatePreSignedGetUrl(anyString(), eq(userId), eq("original"))).thenReturn(
				"test-url");

			ImageDto result = imageService.updateImageMetadata(imageId, userId, updateRequest);

			assertNotNull(result);
			verify(imageMapper, times(2)).findImageByImageId(imageId);
			verify(imageMapper).updateImageMetadata(imageId, "수정된 이미지", "수정된 설명", newDate, 1);
			verify(locationService).updateImageLocation(1L, null, null, imageId);
			verify(tagService).processTagRemoval(imageId, updateRequest);
			verify(tagService).processTagAddition(imageId, updateRequest);
			verify(tagService, times(1)).getTagsForImageDisplay(imageId);
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
			existingImage = Image.builder().imageName("삭제할 이미지").userId(userId).build();
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

			assertThatThrownBy(() -> imageService.deleteImage(imageId, userId)).isInstanceOf(ApiException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_IMAGE);

			verify(imageMapper).findImageByImageId(imageId);
			verify(imageMapper, never()).deleteImage(any());
		}

		@Test
		@DisplayName("실패 - 권한 없음 (다른 사용자)")
		void deleteImage_forbidden() {
			when(imageMapper.findImageByImageId(imageId)).thenReturn(Optional.of(existingImage));

			assertThatThrownBy(() -> imageService.deleteImage(imageId, otherUserId)).isInstanceOf(ApiException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);

			verify(imageMapper).findImageByImageId(imageId);
			verify(imageMapper, never()).deleteImage(any());
		}

		@Test
		@DisplayName("성공 - 태그 연결된 이미지 삭제 시 태그 연결 해제")
		void deleteImage_WithTagsSuccess() {
			// given
			when(imageMapper.findImageByImageId(imageId)).thenReturn(Optional.of(existingImage));
			when(imageMapper.deleteImage(imageId)).thenReturn(1);
			doNothing().when(tagService).removeAllTagsFromImage(imageId);

			// when
			assertDoesNotThrow(() -> imageService.deleteImage(imageId, userId));

			// then
			verify(imageMapper).findImageByImageId(imageId);
			verify(tagService).removeAllTagsFromImage(imageId);
			verify(imageMapper).deleteImage(imageId);
		}
	}

	@Nested
	@DisplayName("convertImageToImageDto 메서드 테스트")
	class ConvertImageToImageDtoTest {

		@Test
		@DisplayName("성공 - 이미지 엔티티를 ImageDto로 변환")
		void convertImageToImageDto_success() {
			// given
			JusoSido sido = new JusoSido();
			ReflectionTestUtils.setField(sido, "sidoId", 1L);
			ReflectionTestUtils.setField(sido, "sidoName", "서울특별시");

			JusoSigungu sigungu = new JusoSigungu();
			ReflectionTestUtils.setField(sigungu, "sigunguId", 1L);
			ReflectionTestUtils.setField(sigungu, "sigunguName", "강남구");
			ReflectionTestUtils.setField(sigungu, "sido", sido);

			Coordinate coordinate = new Coordinate(127.0495556, 37.5398056);
			PrecisionModel precisionModel = new PrecisionModel(PrecisionModel.FLOATING);
			GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 4326);
			Point testPoint = geometryFactory.createPoint(coordinate);

			CoordinatePoint coordPoint = new CoordinatePoint();
			ReflectionTestUtils.setField(coordPoint, "coordinatePointId", 1L);
			ReflectionTestUtils.setField(coordPoint, "coordinatePointName", "테스트 좌표");
			ReflectionTestUtils.setField(coordPoint, "coordinatePointPoint", testPoint);
			ReflectionTestUtils.setField(coordPoint, "sigungu", sigungu);

			LocalDateTime testDate = LocalDateTime.now();
			Long userId = 2L;
			Image image = Image.builder()
				.imageName("테스트 이미지")
				.imageContent("테스트 내용")
				.imageDate(testDate)
				.imageRegDate(testDate)
				.isPublic(1)
				.userId(userId)
				.coordPoint(coordPoint)
				.build();
			Long imageId = 1L;
			ReflectionTestUtils.setField(image, "imageId", imageId);

			String testPresignedUrl = "http://test.url";
			when(imageS3Service.generatePreSignedGetUrl(image.getImageName(), userId, "original")).thenReturn(
				testPresignedUrl);
			when(tagService.getTagsForImageDisplay(imageId)).thenReturn(List.of());

			// when
			ImageDto result = imageService.convertImageToImageDto(image, userId);

			// then
			assertNotNull(result);
			assertEquals(imageId, result.getImageId());
			assertEquals(userId, result.getUserId());
			assertEquals("테스트 이미지", result.getImageName());
			assertEquals("테스트 내용", result.getImageContent());
			assertEquals(testDate, result.getImageDate());
			assertEquals(testDate, result.getImageRegDate());
			assertEquals(1, result.getIsPublic());
			assertEquals(testPresignedUrl, result.getImageUrl());
			assertEquals("서울특별시", result.getSdName());
			assertEquals("강남구", result.getSggName());
			assertEquals(127.0495556, result.getLatitude());
			assertEquals(37.5398056, result.getLongitude());
			assertNotNull(result.getTags());

			verify(imageS3Service).generatePreSignedGetUrl(image.getImageName(), userId, "original");
			verify(tagService).getTagsForImageDisplay(imageId);
		}
	}

	@Nested
	@DisplayName("이미지 태그 관련 테스트")
	class ImageTagTest {
		private final Long imageId = 1L;
		private final Long userId = 2L;

		@Test
		@DisplayName("성공 - 이미지 단건 조회 시 태그 포함")
		void getOriginalImageByImageId_WithTags() {
			// given
			JusoSido sido = new JusoSido();
			ReflectionTestUtils.setField(sido, "sidoId", 1L);
			ReflectionTestUtils.setField(sido, "sidoName", "서울특별시");

			JusoSigungu sigungu = new JusoSigungu();
			ReflectionTestUtils.setField(sigungu, "sigunguId", 1L);
			ReflectionTestUtils.setField(sigungu, "sigunguName", "강남구");
			ReflectionTestUtils.setField(sigungu, "sido", sido);

			Coordinate coordinate = new Coordinate(127.0495556, 37.5398056);
			PrecisionModel precisionModel = new PrecisionModel(PrecisionModel.FLOATING);
			GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 4326);
			Point testPoint = geometryFactory.createPoint(coordinate);

			CoordinatePoint coordPoint = new CoordinatePoint();
			ReflectionTestUtils.setField(coordPoint, "coordinatePointId", 1L);
			ReflectionTestUtils.setField(coordPoint, "coordinatePointName", "테스트 좌표");
			ReflectionTestUtils.setField(coordPoint, "coordinatePointPoint", testPoint);
			ReflectionTestUtils.setField(coordPoint, "sigungu", sigungu);

			Image image = Image.builder().imageName("테스트 이미지").userId(userId).coordPoint(coordPoint).build();
			ReflectionTestUtils.setField(image, "imageId", imageId);

			when(imageMapper.findImageByImageId(imageId)).thenReturn(Optional.of(image));
			when(tagService.getTagsForImageDisplay(imageId)).thenReturn(List.of());
			when(imageS3Service.generatePreSignedGetUrl(any(), any(), any())).thenReturn("http://test.url");

			// when
			ImageDto result = imageService.getOriginalImageByImageId(userId, imageId);

			// then
			assertNotNull(result);
			assertEquals(imageId, result.getImageId());
			assertNotNull(result.getTags());
			verify(tagService).getTagsForImageDisplay(imageId);
		}

		@Test
		@DisplayName("성공 - 이미지 목록 조회 시 태그 포함")
		void getImageListByUserId_WithTags() {
			// given
			ImageSearchByUserIdDto searchDto = ImageSearchByUserIdDto.builder().userId(userId).build();

			ImageInfoForThumbnailDto thumbnailDto = new ImageInfoForThumbnailDto();
			ReflectionTestUtils.setField(thumbnailDto, "imageId", 1L);
			ReflectionTestUtils.setField(thumbnailDto, "userId", userId);
			ReflectionTestUtils.setField(thumbnailDto, "imageName", "테스트 이미지");

			when(imageMapper.findImageThumbnailsByUserId(any())).thenReturn(List.of(thumbnailDto));
			when(imageS3Service.generatePreSignedGetUrl(any(), any(), any())).thenReturn("http://test.url");

			// when
			PageInfo<ImageThumbnailDto> result = imageService.getImageListByUserId(searchDto);

			// then
			assertNotNull(result);
			assertNotNull(result.getList());
			verify(imageMapper).findImageThumbnailsByUserId(any());
		}

		@Test
		@DisplayName("성공 - 이미지 업데이트 시 태그 추가/삭제")
		void updateImageMetadata_WithTagOperations() {
			// given
			JusoSido sido = new JusoSido();
			ReflectionTestUtils.setField(sido, "sidoId", 1L);
			ReflectionTestUtils.setField(sido, "sidoName", "서울특별시");

			JusoSigungu sigungu = new JusoSigungu();
			ReflectionTestUtils.setField(sigungu, "sigunguId", 1L);
			ReflectionTestUtils.setField(sigungu, "sigunguName", "강남구");
			ReflectionTestUtils.setField(sigungu, "sido", sido);

			Coordinate coordinate = new Coordinate(127.0495556, 37.5398056);
			PrecisionModel precisionModel = new PrecisionModel(PrecisionModel.FLOATING);
			GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 4326);
			Point testPoint = geometryFactory.createPoint(coordinate);

			CoordinatePoint coordPoint = new CoordinatePoint();
			ReflectionTestUtils.setField(coordPoint, "coordinatePointId", 1L);
			ReflectionTestUtils.setField(coordPoint, "coordinatePointName", "테스트 좌표");
			ReflectionTestUtils.setField(coordPoint, "coordinatePointPoint", testPoint);
			ReflectionTestUtils.setField(coordPoint, "sigungu", sigungu);

			Image existingImage = Image.builder().imageName("기존 이미지").userId(userId).coordPoint(coordPoint).build();
			ReflectionTestUtils.setField(existingImage, "imageId", imageId);

			ImageUpdateRequestDto updateRequest = ImageUpdateRequestDto.builder()
				.imageName("수정된 이미지")
				.tagsToAdd(List.of("새태그1", "새태그2"))
				.tagsToRemove(List.of(1L, 2L))
				.build();

			when(imageMapper.findImageByImageId(imageId)).thenReturn(Optional.of(existingImage));
			when(imageMapper.updateImageMetadata(any(), any(), any(), any(), any())).thenReturn(1);
			when(tagService.getTagsForImageDisplay(imageId)).thenReturn(List.of());
			when(imageS3Service.generatePreSignedGetUrl(any(), any(), any())).thenReturn("http://test.url");
			doNothing().when(tagService).processTagRemoval(any(), any());
			doNothing().when(tagService).processTagAddition(any(), any());

			// when
			ImageDto result = imageService.updateImageMetadata(imageId, userId, updateRequest);

			// then
			assertNotNull(result);
			verify(tagService).processTagRemoval(imageId, updateRequest);
			verify(tagService).processTagAddition(imageId, updateRequest);
		}

		@Test
		@DisplayName("성공 - 좌표 변경 시 위치 기반 태그 자동 생성")
		void updateImageMetadata_WithLocationChange() {
			// given
			JusoSido sido = new JusoSido();
			ReflectionTestUtils.setField(sido, "sidoId", 1L);
			ReflectionTestUtils.setField(sido, "sidoName", "서울특별시");

			JusoSigungu sigungu = new JusoSigungu();
			ReflectionTestUtils.setField(sigungu, "sigunguId", 1L);
			ReflectionTestUtils.setField(sigungu, "sigunguName", "강남구");
			ReflectionTestUtils.setField(sigungu, "sido", sido);

			Coordinate coordinate = new Coordinate(127.0495556, 37.5398056);
			PrecisionModel precisionModel = new PrecisionModel(PrecisionModel.FLOATING);
			GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 4326);
			Point testPoint = geometryFactory.createPoint(coordinate);

			CoordinatePoint coordPoint = new CoordinatePoint();
			ReflectionTestUtils.setField(coordPoint, "coordinatePointId", 1L);
			ReflectionTestUtils.setField(coordPoint, "coordinatePointName", "테스트 좌표");
			ReflectionTestUtils.setField(coordPoint, "coordinatePointPoint", testPoint);
			ReflectionTestUtils.setField(coordPoint, "sigungu", sigungu);

			Image existingImage = Image.builder().imageName("기존 이미지").userId(userId).coordPoint(coordPoint).build();
			ReflectionTestUtils.setField(existingImage, "imageId", imageId);

			ImageUpdateRequestDto updateRequest = ImageUpdateRequestDto.builder()
				.latitude(37.5665)
				.longitude(126.978)
				.build();

			when(imageMapper.findImageByImageId(imageId)).thenReturn(Optional.of(existingImage));
			when(imageMapper.updateImageMetadata(any(), any(), any(), any(), any())).thenReturn(1);
			doNothing().when(locationService).updateImageLocation(1L, 37.5665, 126.978, imageId);
			doNothing().when(tagService).processTagRemoval(imageId, updateRequest);
			doNothing().when(tagService).processTagAddition(imageId, updateRequest);
			when(tagService.getTagsForImageDisplay(imageId)).thenReturn(List.of());
			when(imageS3Service.generatePreSignedGetUrl(any(), any(), any())).thenReturn("http://test.url");

			// when
			ImageDto result = imageService.updateImageMetadata(imageId, userId, updateRequest);

			// then
			assertNotNull(result);
			verify(locationService).updateImageLocation(1L, 37.5665, 126.978, imageId);
			verify(tagService).processTagRemoval(imageId, updateRequest);
			verify(tagService).processTagAddition(imageId, updateRequest);
		}
	}
}
