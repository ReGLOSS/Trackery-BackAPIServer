package com.trackery.trackerybackapiserver.domain.image.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.image.mapper.ImageMapper;

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
 */
class ImageServiceTest {

	private ImageMapper imageMapper;
	private CacheManager cacheManager;
	private ImageService imageService;

	@BeforeEach
	void setUp() {
		imageMapper = mock(ImageMapper.class);
		ImageS3Service imageS3Service = mock(ImageS3Service.class);
		imageService = new ImageService(imageMapper, imageS3Service);
	}

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
}
