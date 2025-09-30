package com.trackery.trackerybackapiserver.domain.admin.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackery.trackerybackapiserver.domain.admin.service.AdminTagService;
import com.trackery.trackerybackapiserver.domain.config.CommonMockMvcControllerTestSetUp;
import com.trackery.trackerybackapiserver.domain.tag.dto.TagCreateRequestDto;
import com.trackery.trackerybackapiserver.domain.tag.entity.Tag;
import com.trackery.trackerybackapiserver.domain.tag.enums.TagType;
import com.trackery.trackerybackapiserver.domain.tag.mapper.TagMapper;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.admin.controller
 * fileName       : AdminTagControllerTest
 * author         : inari
 * date           : 25. 9. 28.
 * description    : 관리자 태그 관리 컨트롤러 테스트 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 28.		inari		최초 생성
 * 25. 9. 29.		inari		테스트 코드 추가
 */
@WebMvcTest(AdminTagController.class)
@DisplayName("AdminTagController 테스트")
class AdminTagControllerTest extends CommonMockMvcControllerTestSetUp {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private AdminTagService adminTagService;

	private TagMapper.TagStatistics testTagStatistics;
	private Tag testTag;

	@BeforeEach
	void setUp() {
		testTagStatistics = createTestTagStatistics();
		testTag = createTestTag();
	}

	@Nested
	@DisplayName("태그 목록 조회")
	class GetTagsTest {

		@Test
		@DisplayName("ADMIN이 태그 통계를 성공적으로 조회한다")
		void getTagsSuccess() throws Exception {
			// Given
			List<TagMapper.TagStatistics> tagStatistics = List.of(testTagStatistics);
			when(adminTagService.getTagStatistics(eq(3L))).thenReturn(tagStatistics);

			// When & Then
			mockMvc.perform(get("/api/admin/tags")
					.with(user(createAdminUserDetails())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Ok"))
				.andExpect(jsonPath("$.data").isArray())
				.andExpect(jsonPath("$.data[0].tagId").value(1))
				.andExpect(jsonPath("$.data[0].tagName").value("풍경"))
				.andExpect(jsonPath("$.data[0].tagType").value("SIDO"))
				.andExpect(jsonPath("$.data[0].createdAt").exists())
				.andDo(document("admin-tags-get",
					responseFields(
						fieldWithPath("code").description("응답 코드"),
						fieldWithPath("message").description("응답 메시지"),
						fieldWithPath("data[]").description("태그 통계 목록"),
						fieldWithPath("data[].tagId").description("태그 ID"),
						fieldWithPath("data[].tagName").description("태그명"),
						fieldWithPath("data[].tagType").description("태그 유형"),
						fieldWithPath("data[].usageCount").description("태그 사용 횟수"),
						fieldWithPath("data[].createdAt").description("생성일시")
					)
				));

			verify(adminTagService).getTagStatistics(3L);
		}

		@Test
		@DisplayName("MANAGER 권한으로는 접근할 수 없다")
		void getTagsForbiddenForManager() throws Exception {
			// When & Then
			mockMvc.perform(get("/api/admin/tags")
					.with(user(createManagerUserDetails())))
				.andExpect(status().isForbidden());

			verify(adminTagService, never()).getTagStatistics(anyLong());
		}
	}

	@Nested
	@DisplayName("시스템 태그 생성")
	class CreateTagTest {

		@Test
		@DisplayName("ADMIN이 시스템 태그를 성공적으로 생성한다")
		void createTagSuccess() throws Exception {
			// Given
			TagCreateRequestDto requestDto = TagCreateRequestDto.builder()
				.tagName("새로운태그")
				.tagType(TagType.CUSTOM.getCode())
				.build();

			when(adminTagService.createSystemTag(eq(3L), eq("새로운태그"), any(TagType.class)))
				.thenReturn(testTag);

			// When & Then
			mockMvc.perform(post("/api/admin/tags")
					.with(user(createAdminUserDetails()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(requestDto)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Created"))
				.andDo(document("admin-tag-create",
					requestFields(
						fieldWithPath("tagName").description("태그명"),
						fieldWithPath("tagType").description("태그 유형 (0: CUSTOM, 1: SIDO, 2: SIGUNGU, 3: SEASON, 4: TIME, 5: WEATHER)")
					),
					responseFields(
						fieldWithPath("code").description("응답 코드"),
						fieldWithPath("message").description("응답 메시지"),
						fieldWithPath("data.tagId").description("생성된 태그 ID").optional(),
						fieldWithPath("data.tagName").description("태그명"),
						fieldWithPath("data.tagType").description("태그 유형"),
						fieldWithPath("data.tagUseCount").description("태그 사용 횟수"),
						fieldWithPath("data.createdAt").description("생성일시")
					)
				));

			verify(adminTagService).createSystemTag(eq(3L), eq("새로운태그"), any(TagType.class));
		}

		@Test
		@DisplayName("잘못된 태그 유형으로 생성하면 실패한다")
		void createTagWithInvalidType() throws Exception {
			// Given
			TagCreateRequestDto requestDto = TagCreateRequestDto.builder()
				.tagName("새로운태그")
				.tagType(999) // 잘못된 코드
				.build();

			// When & Then
			mockMvc.perform(post("/api/admin/tags")
					.with(user(createAdminUserDetails()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(requestDto)))
				.andExpect(status().isInternalServerError()); // 500 오류

			verify(adminTagService, never()).createSystemTag(anyLong(), anyString(), any(TagType.class));
		}

		@Test
		@DisplayName("빈 태그명으로 생성하면 실패한다")
		void createTagWithEmptyName() throws Exception {
			// Given
			TagCreateRequestDto requestDto = TagCreateRequestDto.builder()
				.tagName("")
				.tagType(TagType.CUSTOM.getCode())
				.build();

			when(adminTagService.createSystemTag(eq(3L), eq(""), eq(TagType.CUSTOM)))
				.thenThrow(new IllegalArgumentException("태그명은 필수입니다"));

			// When & Then
			mockMvc.perform(post("/api/admin/tags")
					.with(user(createAdminUserDetails()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(requestDto)))
				.andExpect(status().isInternalServerError()); // 500 오류

			verify(adminTagService).createSystemTag(eq(3L), eq(""), eq(TagType.CUSTOM));
		}
	}

	@Nested
	@DisplayName("태그 삭제")
	class DeleteTagTest {

		@Test
		@DisplayName("ADMIN이 태그를 성공적으로 삭제한다")
		void deleteTagSuccess() throws Exception {
			// Given
			Long tagId = 1L;
			doNothing().when(adminTagService).deleteTag(eq(3L), eq(tagId));

			// When & Then
			mockMvc.perform(delete("/api/admin/tags/{tagId}", tagId)
					.with(user(createAdminUserDetails())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Ok"))
				.andDo(document("admin-tag-delete",
					pathParameters(
						parameterWithName("tagId").description("삭제할 태그 ID")
					),
					responseFields(
						fieldWithPath("code").description("응답 코드"),
						fieldWithPath("message").description("응답 메시지")
					)
				));

			verify(adminTagService).deleteTag(3L, tagId);
		}

		@Test
		@DisplayName("MANAGER 권한으로는 태그를 삭제할 수 없다")
		void deleteTagForbiddenForManager() throws Exception {
			// Given
			Long tagId = 1L;

			// When & Then
			mockMvc.perform(delete("/api/admin/tags/{tagId}", tagId)
					.with(user(createManagerUserDetails())))
				.andExpect(status().isForbidden());

			verify(adminTagService, never()).deleteTag(anyLong(), anyLong());
		}
	}

	private TagMapper.TagStatistics createTestTagStatistics() {
		return new TagMapper.TagStatistics(
			1L,
			"풍경",
			TagType.SIDO,
			150L,
			50L, // connectedImageCount
			LocalDateTime.now()
		);
	}

	private Tag createTestTag() {
		return Tag.builder()
			.tagName("풍경")
			.tagType(TagType.SIDO)
			.tagUseCount(0L)
			.createdAt(LocalDateTime.now())
			.build();
	}
}
