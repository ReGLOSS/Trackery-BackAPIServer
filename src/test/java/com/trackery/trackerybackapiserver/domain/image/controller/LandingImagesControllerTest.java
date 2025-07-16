package com.trackery.trackerybackapiserver.domain.image.controller;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.trackery.trackerybackapiserver.domain.image.service.ImageService;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.image.controller
 * fileName       : LandingImagesControllerTest
 * author         : inari
 * date           : 25. 2. 14.
 * description    : LandingImagesController의 테스트 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 14.		inari		최초 생성
 * 25. 2. 14.		inari		랜덤이미지 가져오기 구현
 * 25. 2. 18.		inari		dev 병합후 랜딩페이지 엔드포인트 수정
 * 25. 2. 18.		inari		랜딩페이지 테스트코드 복구
 * 25. 4. 18.		durururuk		이미지 업로드 기능 구현 전 공간 관련 기능 정리
 * 25. 6. 11.		inari		adoc 문서 추가
 */
@ExtendWith(RestDocumentationExtension.class)
class LandingImagesControllerTest {

	private MockMvc mockMvc;
	private ImageService imageService;
	private LandingImagesController landingImagesController;

	@BeforeEach
	void setUp(RestDocumentationContextProvider restDocumentation) {
		imageService = mock(ImageService.class);
		landingImagesController = new LandingImagesController(imageService);
		mockMvc = MockMvcBuilders
			.standaloneSetup(landingImagesController)
			.apply(documentationConfiguration(restDocumentation))
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
			.andExpect(jsonPath("$.data.imageUrls[1]").value("http://example.com/image2.jpg"))
			.andDo(document("get-public-images",
				responseFields(
					fieldWithPath("code").description("응답 코드"),
					fieldWithPath("message").description("응답 메시지"),
					fieldWithPath("data.imageUrls[]").description("공개 이미지 URL 목록")
				)
			));
	}
}
