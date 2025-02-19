package com.trackery.trackerybackapiserver.domain.home.controller;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.trackery.trackerybackapiserver.domain.home.service.ImageService;

/**
 *packageName    : com.trackery.trackerybackapiserver.domain.home.controller
 * fileName       : LandingImagesControllerTest
 * author         : inari
 * date           : 25. 2. 14.
 * description    : LandingImagesController의 테스트 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 14.        inari       최초 생성
 */
class LandingImagesControllerTest {

	private MockMvc mockMvc;
	private ImageService imageService;
	private LandingImagesController landingImagesController;

	@BeforeEach
	void setUp() {
		imageService = mock(ImageService.class);
		landingImagesController = new LandingImagesController(imageService);
		mockMvc = MockMvcBuilders
			.standaloneSetup(landingImagesController)
			.build();
	}

	@Test
	@DisplayName("공개 이미지 URL 목록을 정상적으로 반환하는지 테스트")
	void getPublicImages_ShouldReturnImageUrls() throws Exception {
		// Given
		List<String> mockUrls = Arrays.asList(
			"http://example.com/image1.jpg",
			"http://example.com/image2.jpg"
		);
		when(imageService.getPublicImageUrls()).thenReturn(mockUrls);

		// When & Then
		mockMvc.perform(get("/api/home/images"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value(200))
			.andExpect(jsonPath("$.message").value("Ok"))
			.andExpect(jsonPath("$.data.imageUrls").isArray())
			.andExpect(jsonPath("$.data.imageUrls[0]").value("http://example.com/image1.jpg"))
			.andExpect(jsonPath("$.data.imageUrls[1]").value("http://example.com/image2.jpg"));
	}
}
