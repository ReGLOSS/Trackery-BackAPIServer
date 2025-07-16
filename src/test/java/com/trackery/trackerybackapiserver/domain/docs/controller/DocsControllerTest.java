package com.trackery.trackerybackapiserver.domain.docs.controller;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;

import com.trackery.trackerybackapiserver.domain.config.CommonMockMvcControllerTestSetUp;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.docs.controller
 * fileName       : DocsControllerTest
 * author         : Nari-Lee
 * date           : 25. 6. 30.
 * description    : DocsController의 테스트 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 6. 30.		Nari-Lee		최초 생성
 * 25. 6. 30.		Nari-Lee		docs 컨트롤러 테스트코드 및 api문서 추가
 * 25. 6. 30.		Nari-Lee		테스트코드 수정
 * 25. 7. 8.		Nari-Lee		테스트코드 추가 및 문서화
 * 25. 7. 16.		Nari-Lee		DocsControllerTest 테스트 코드 작성
 */
@WebMvcTest(DocsController.class)
@DisplayName("DocsController 테스트")
class DocsControllerTest extends CommonMockMvcControllerTestSetUp {

	@Nested
	@DisplayName("API 문서 조회")
	class GetDocs {

		@Test
		@DisplayName("성공 - index.html 경로로 요청")
		void getDocs_Success_IndexHtml() throws Exception {
			try (MockedConstruction<ClassPathResource> mockConstruction = mockConstruction(ClassPathResource.class,
				(mock, context) -> {
					when(mock.exists()).thenReturn(true);
					File mockFile = mock(File.class);
					Path mockPath = mock(Path.class);
					when(mock.getFile()).thenReturn(mockFile);
					when(mockFile.toPath()).thenReturn(mockPath);
				});
				MockedStatic<Files> mockFiles = mockStatic(Files.class)) {
				
				mockFiles.when(() -> Files.readString(any(Path.class))).thenReturn("<html><body>Test HTML</body></html>");

				mockMvc.perform(get("/api/docs/index.html"))
					.andExpect(status().isOk())
					.andExpect(content().contentType(MediaType.TEXT_HTML));
			}
		}

		@Test
		@DisplayName("성공 - 루트 경로로 요청")
		void getDocs_Success_Root() throws Exception {
			try (MockedConstruction<ClassPathResource> mockConstruction = mockConstruction(ClassPathResource.class,
				(mock, context) -> {
					when(mock.exists()).thenReturn(true);
					File mockFile = mock(File.class);
					Path mockPath = mock(Path.class);
					when(mock.getFile()).thenReturn(mockFile);
					when(mockFile.toPath()).thenReturn(mockPath);
				});
				MockedStatic<Files> mockFiles = mockStatic(Files.class)) {
				
				mockFiles.when(() -> Files.readString(any(Path.class))).thenReturn("<html><body>Test HTML</body></html>");

				mockMvc.perform(get("/api/docs"))
					.andExpect(status().isOk())
					.andExpect(content().contentType(MediaType.TEXT_HTML));
			}
		}

		@Test
		@DisplayName("실패 - IOException 발생")
		void getDocs_Fail_IOException() throws Exception {
			try (MockedConstruction<ClassPathResource> mockConstruction = mockConstruction(ClassPathResource.class,
				(mock, context) -> {
					when(mock.exists()).thenReturn(true);
					File mockFile = mock(File.class);
					Path mockPath = mock(Path.class);
					when(mock.getFile()).thenReturn(mockFile);
					when(mockFile.toPath()).thenReturn(mockPath);
				});
				MockedStatic<Files> mockFiles = mockStatic(Files.class)) {
				
				mockFiles.when(() -> Files.readString(any(Path.class))).thenThrow(new IOException("파일 읽기 실패"));

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
