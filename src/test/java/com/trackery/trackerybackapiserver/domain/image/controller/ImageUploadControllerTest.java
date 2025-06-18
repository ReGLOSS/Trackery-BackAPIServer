package com.trackery.trackerybackapiserver.domain.image.controller;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
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
 * 25. 6. 18.		inari		    Spring-Rest-Docs api문서 추가
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
			.andExpect(jsonPath("$.data").value("PresignedURL.com"))
			.andDo(document("get-presigned-url-success",
				queryParameters(
					parameterWithName("imageFileName").description("업로드할 이미지 파일명")
				),
				responseFields(
					fieldWithPath("code").description("상태 코드"),
					fieldWithPath("message").description("응답 메시지"),
					fieldWithPath("data").description("S3 presigned URL")
				)
			));
	}

	@Test
	void saveImageMetaDataSuccess() throws Exception {
		ImageUploadDto imageUploadDto = new ImageUploadDto();
		// Reflection을 사용해서 필드값 설정 (NoArgsConstructor만 있으므로)
		org.springframework.test.util.ReflectionTestUtils.setField(imageUploadDto, "imageName", "test-image.jpg");
		org.springframework.test.util.ReflectionTestUtils.setField(imageUploadDto, "imageType", "JPEG");
		org.springframework.test.util.ReflectionTestUtils.setField(imageUploadDto, "description", "테스트 이미지 설명");
		org.springframework.test.util.ReflectionTestUtils.setField(imageUploadDto, "tags", "여행,서울");
		org.springframework.test.util.ReflectionTestUtils.setField(imageUploadDto, "longitude", 127.0276);
		org.springframework.test.util.ReflectionTestUtils.setField(imageUploadDto, "latitude", 37.4979);
		org.springframework.test.util.ReflectionTestUtils.setField(imageUploadDto, "dateString", "2025-01-01T10:30:00");
		org.springframework.test.util.ReflectionTestUtils.setField(imageUploadDto, "isPublic", true);

		when(imageUploadService.saveImage(customUserDetails.getUserId(), imageUploadDto)).thenReturn(mock(Image.class));

		ResultActions result = mockMvc.perform(post("/api/images")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(imageUploadDto))
			.with(user(customUserDetails))
			.with(csrf()));

		result
			.andExpect(status().isOk())
			.andDo(document("save-image-metadata-success",
				requestFields(
					fieldWithPath("imageName").description("이미지 파일명"),
					fieldWithPath("imageType").description("이미지 타입 (JPEG, PNG 등)"),
					fieldWithPath("description").description("이미지 설명"),
					fieldWithPath("tags").description("이미지 태그 (쉼표로 구분)"),
					fieldWithPath("longitude").description("경도"),
					fieldWithPath("latitude").description("위도"),
					fieldWithPath("dateString").description("이미지 촬영 일시 (ISO 형식)"),
					fieldWithPath("public").description("공개 여부 (true: 공개, false: 비공개)")
				),
				responseFields(
					fieldWithPath("code").description("상태 코드"),
					fieldWithPath("message").description("응답 메시지")
				)
			));
	}
}