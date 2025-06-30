package com.trackery.trackerybackapiserver.domain.docs.controller;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.trackery.trackerybackapiserver.domain.config.CommonMockMvcControllerTestSetUp;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.docs.controller
 * fileName       : DocsControllerTest
 * author         : Claude Code
 * date           : 25. 6. 30.
 * description    : DocsController의 테스트 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 6. 30.        inari       	최초 생성
 */
@WebMvcTest(DocsController.class)
@DisplayName("DocsController 테스트")
class DocsControllerTest extends CommonMockMvcControllerTestSetUp {

	@Autowired
	private MockMvc mockMvc;

	@Nested
	@DisplayName("API 문서 조회")
	class GetDocs {

		@Test
		@DisplayName("성공 - index.html 경로로 요청")
		void getDocs_Success_IndexHtml() throws Exception {
			// when & then
			ResultActions result = mockMvc.perform(get("/api/docs/index.html"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.TEXT_HTML));

			// REST Docs - HTML 응답이 길어서 헤더만 문서화
			result.andDo(document("docs-get-index-html",
				responseHeaders(
					headerWithName("Content-Type").description("응답 콘텐츠 타입 (text/html)")
				)));
		}

		@Test
		@DisplayName("성공 - 루트 경로로 요청")
		void getDocs_Success_Root() throws Exception {
			// when & then
			ResultActions result = mockMvc.perform(get("/api/docs"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.TEXT_HTML));

			// REST Docs - HTML 응답이 길어서 헤더만 문서화
			result.andDo(document("docs-get-root",
				responseHeaders(
					headerWithName("Content-Type").description("응답 콘텐츠 타입 (text/html)")
				)));
		}

		@Test
		@DisplayName("실패 - IOException 발생")
		void getDocs_Fail_IOException() throws Exception {
			try (MockedStatic<Files> mockFiles = mockStatic(Files.class)) {
				mockFiles.when(() -> Files.readString(any())).thenThrow(new IOException("파일 읽기 실패"));

				mockMvc.perform(get("/api/docs/index.html"))
					.andExpect(status().isInternalServerError());
			}
		}

		@Test
		@DisplayName("실패 - 파일이 존재하지 않음")
		void getDocs_Fail_FileNotExists() throws Exception {
			try (MockedConstruction<ClassPathResource> mockConstruction = mockConstruction(ClassPathResource.class,
				(mock, context) -> when(mock.exists()).thenReturn(false))) {

				mockMvc.perform(get("/api/docs/index.html"))
					.andExpect(status().isNotFound());
			}
		}

	}
}
