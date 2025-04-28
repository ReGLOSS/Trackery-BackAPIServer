package com.trackery.trackerybackapiserver.domain.image.controller;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import com.trackery.trackerybackapiserver.domain.config.CommonMockMvcControllerTestSetUp;
import com.trackery.trackerybackapiserver.domain.image.dto.upload.ImageUploadDto;
import com.trackery.trackerybackapiserver.domain.image.entity.Image;
import com.trackery.trackerybackapiserver.domain.image.service.ImageUploadService;
import com.trackery.trackerybackapiserver.domain.user.entity.CustomUserDetails;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.image.controller
 * fileName       : ImageUploadControllerTest
 * author         : durururuk
 * date           : 25. 4. 23.
 * description    : 이미지 업로드 컨트롤러 mockMvc 단위테스트 클래스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 23.		durururuk		최초 생성
 */
@WebMvcTest(ImageUploadController.class)
class ImageUploadControllerTest extends CommonMockMvcControllerTestSetUp {
	@MockitoBean
	ImageUploadService imageUploadService;

	CustomUserDetails customUserDetails;

	@BeforeEach
	void setUp() {
		customUserDetails = CustomUserDetails.builder().userId(1L).userName("abcdefg").roleId(1L).build();
	}

	@Test
	void requestPreSignedPutUrlSuccess() throws Exception {
		String imageFileName = "image.jpg";
		when(imageUploadService.getPresignedPutUrl(imageFileName)).thenReturn("PresignedURL.com");

		ResultActions result = mockMvc.perform(get("/api/images/presigned-url/put")
			.queryParam("imageFileName", imageFileName)
			.with(user(customUserDetails)));

		result
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data").value("PresignedURL.com"));
	}

	@Test
	void saveImageMetaDataSuccess() throws Exception {
		ImageUploadDto imageUploadDto = new ImageUploadDto();

		when(imageUploadService.saveImage(customUserDetails.getUserId(), imageUploadDto)).thenReturn(mock(Image.class));

		ResultActions result = mockMvc.perform(post("/api/images")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(imageUploadDto))
			.with(user(customUserDetails))
			.with(csrf()));

		result.andExpect(status().isOk());
	}
}