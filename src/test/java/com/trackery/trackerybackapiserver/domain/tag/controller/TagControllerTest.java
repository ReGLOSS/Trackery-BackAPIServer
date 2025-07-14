package com.trackery.trackerybackapiserver.domain.tag.controller;

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

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.config.CommonMockMvcControllerTestSetUp;
import com.trackery.trackerybackapiserver.domain.image.service.ImageService;
import com.trackery.trackerybackapiserver.domain.tag.dto.TagCreateRequestDto;
import com.trackery.trackerybackapiserver.domain.tag.dto.TagNameResponseDto;
import com.trackery.trackerybackapiserver.domain.tag.dto.TagResponseDto;
import com.trackery.trackerybackapiserver.domain.tag.dto.TagSeasonRequestDto;
import com.trackery.trackerybackapiserver.domain.tag.enums.TagType;
import com.trackery.trackerybackapiserver.domain.tag.service.TagService;
import com.trackery.trackerybackapiserver.domain.user.entity.CustomUserDetails;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.tag.controller
 * fileName       : TagControllerTest
 * author         : inari
 * date           : 25. 7. 8.
 * description    : TagController 테스트코드
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 7. 8.        inari       최초 생성
 * 25. 7. 9.        inari       테스트코드 수정
 * 25. 7. 14.       inari       테스트 코드 수정
 */
@WebMvcTest(TagController.class)
class TagControllerTest extends CommonMockMvcControllerTestSetUp {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private TagService tagService;

	@MockitoBean
	private ImageService imageService;

	private TagResponseDto tagResponseDto;
	private TagCreateRequestDto createRequestDto;
	private TagSeasonRequestDto defaultRequestDto;

	@BeforeEach
	void setUp() {
		tagResponseDto = TagResponseDto.builder()
			.tagId(1L)
			.tagName("테스트태그")
			.tagType(TagType.CUSTOM)
			.tagUseCount(0L)
			.createdAt(LocalDateTime.now())
			.build();

		createRequestDto = TagCreateRequestDto.builder()
			.tagName("새태그")
			.tagType(TagType.CUSTOM.getCode())
			.build();

		defaultRequestDto = new TagSeasonRequestDto("2024/7/15");
	}

	@Nested
	@DisplayName("태그 생성 API 테스트")
	class CreateTagTest {

		@Test
		@DisplayName("태그 생성 성공")
		void createTag_Success() throws Exception {
			// given
			when(tagService.createTag(any(TagCreateRequestDto.class))).thenReturn(tagResponseDto);

			// when & then
			mockMvc.perform(post("/api/tags")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(createRequestDto))
					.with(csrf())
					.with(user(createTestUser())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.data.tagId").value(1L))
				.andExpect(jsonPath("$.data.tagName").value("테스트태그"))
				.andExpect(jsonPath("$.data.tagType").value("CUSTOM"))
				.andExpect(jsonPath("$.data.tagUseCount").value(0L))
				.andDo(document("tag-create",
					requestFields(
						fieldWithPath("tagName").description("생성할 태그명"),
						fieldWithPath("tagType").description("태그 타입 코드 (0: CUSTOM, 1: SIDO, 2: SIGUNGU, 3: SEASON, 4: TIME, 5: WEATHER)")
					),
					relaxedResponseFields(
						fieldWithPath("code").description("응답 코드"),
						fieldWithPath("message").description("응답 메시지"),
						fieldWithPath("data.tagId").description("태그 ID"),
						fieldWithPath("data.tagName").description("태그명"),
						fieldWithPath("data.tagType").description("태그 타입(CUSTOM, SIDO, SIGUNGU 등)"),
						fieldWithPath("data.tagUseCount").description("태그 사용 횟수"),
						fieldWithPath("data.createdAt").description("태그 생성 시간")
					)
				));

			verify(tagService).createTag(any(TagCreateRequestDto.class));
		}
	}

	@Nested
	@DisplayName("태그 조회 API 테스트")
	class GetTagsTest {

		@Test
		@DisplayName("모든 태그 조회 성공")
		void getAllTags_Success() throws Exception {
			// given
			List<TagResponseDto> tags = List.of(tagResponseDto);
			when(tagService.getAllTags()).thenReturn(tags);

			// when & then
			mockMvc.perform(get("/api/tags")
					.with(user(createTestUser())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.data").isArray())
				.andExpect(jsonPath("$.data[0].tagId").value(1L))
				.andExpect(jsonPath("$.data[0].tagName").value("테스트태그"))
				.andDo(document("tag-get-all",
					relaxedResponseFields(
						fieldWithPath("code").description("응답 코드"),
						fieldWithPath("message").description("응답 메시지"),
						fieldWithPath("data[].tagId").description("태그 ID"),
						fieldWithPath("data[].tagName").description("태그명"),
						fieldWithPath("data[].tagType").description("태그 타입(CUSTOM, SIDO, SIGUNGU 등)"),
						fieldWithPath("data[].tagUseCount").description("태그 사용 횟수"),
						fieldWithPath("data[].createdAt").description("태그 생성 시간")
					)
				));

			verify(tagService).getAllTags();
		}

		@Test
		@DisplayName("시스템 태그 조회 성공")
		void getSystemTags_Success() throws Exception {
			// given
			List<TagResponseDto> systemTags = List.of(tagResponseDto);
			when(tagService.getSystemTags()).thenReturn(systemTags);

			// when & then
			mockMvc.perform(get("/api/tags/system")
					.with(user(createTestUser())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.data").isArray())
				.andExpect(jsonPath("$.data[0].tagId").value(1L))
				.andDo(document("tag-get-system",
					relaxedResponseFields(
						fieldWithPath("code").description("응답 코드"),
						fieldWithPath("message").description("응답 메시지"),
						fieldWithPath("data[].tagId").description("태그 ID"),
						fieldWithPath("data[].tagName").description("태그명"),
						fieldWithPath("data[].tagType").description("태그 타입(CUSTOM, SIDO, SIGUNGU 등)"),
						fieldWithPath("data[].tagUseCount").description("태그 사용 횟수"),
						fieldWithPath("data[].createdAt").description("태그 생성 시간")
					)
				));

			verify(tagService).getSystemTags();
		}

	}


	@Nested
	@DisplayName("태그 관리자 기능 API 테스트")
	class AdminTagTest {

		@Test
		@DisplayName("관리자 태그 삭제 성공")
		void deleteTagByAdmin_Success() throws Exception {
			// given
			Long tagId = 1L;
			when(tagService.deleteTagByAdmin(tagId)).thenReturn(true);

			// when & then
			mockMvc.perform(delete("/api/tags/admin/{tagId}", tagId)
					.with(csrf())
					.with(user(createTestUser())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(200))
				.andDo(document("tag-delete-by-admin",
					pathParameters(
						parameterWithName("tagId").description("삭제할 태그 ID")
					),
					relaxedResponseFields(
						fieldWithPath("code").description("응답 코드"),
						fieldWithPath("message").description("응답 메시지")
					)
				));

			verify(tagService).deleteTagByAdmin(tagId);
		}

		@Test
		@DisplayName("관리자 태그 삭제 실패")
		void deleteTagByAdmin_Failure() throws Exception {
			// given
			Long tagId = 1L;
			when(tagService.deleteTagByAdmin(tagId)).thenReturn(false);

			// when & then
			mockMvc.perform(delete("/api/tags/admin/{tagId}", tagId)
					.with(csrf())
					.with(user(createTestUser())))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value(400));

			verify(tagService).deleteTagByAdmin(tagId);
		}
	}

	@Nested
	@DisplayName("계절 태그 생성 API 테스트")
	class SeasonTagTest {

		@Test
		@DisplayName("계절 태그 생성 성공")
		void getSeasonTags_Success() throws Exception {
			// given
			List<TagNameResponseDto> defaultTags = List.of(
				TagNameResponseDto.builder().tagName("여름").build()
			);
			when(tagService.createSeasonTags("2024/7/15")).thenReturn(defaultTags);

			// when & then
			mockMvc.perform(post("/api/tags/season")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(defaultRequestDto))
					.with(csrf())
					.with(user(createTestUser())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.data").isArray())
				.andExpect(jsonPath("$.data[0].tagName").value("여름"))
				.andDo(document("tag-create-season",
					requestFields(
						fieldWithPath("date").description("날짜 문자열 (예: 2024/7/15)")
					),
					relaxedResponseFields(
						fieldWithPath("code").description("응답 코드"),
						fieldWithPath("message").description("응답 메시지"),
						fieldWithPath("data[].tagName").description("생성된 계절 태그명")
					)
				));

			verify(tagService).createSeasonTags("2024/7/15");
		}

		@Test
		@DisplayName("잘못된 날짜 형식으로 계절 태그 생성 요청 실패")
		void getSeasonTags_InvalidDate() throws Exception {
			// given
			TagSeasonRequestDto invalidRequestDto = new TagSeasonRequestDto("invalid-date");
			when(tagService.createSeasonTags("invalid-date"))
				.thenThrow(new ApiException(ErrorCode.BAD_REQUEST));

			// when & then
			mockMvc.perform(post("/api/tags/season")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(invalidRequestDto))
					.with(csrf())
					.with(user(createTestUser())))
				.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("빈 날짜로 계절 태그 생성 요청 실패")
		void getSeasonTags_EmptyDate() throws Exception {
			// given
			TagSeasonRequestDto emptyRequestDto = new TagSeasonRequestDto("");

			// when & then
			mockMvc.perform(post("/api/tags/season")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(emptyRequestDto))
					.with(csrf())
					.with(user(createTestUser())))
				.andExpect(status().isBadRequest());
		}
	}

	@Nested
	@DisplayName("태그 생성 검증 테스트")
	class TagCreateValidationTest {

		@Test
		@DisplayName("빈 태그명으로 태그 생성 성공")
		void createTag_EmptyTagName() throws Exception {
			// given
			TagCreateRequestDto requestDto = TagCreateRequestDto.builder()
				.tagName("")
				.tagType(TagType.CUSTOM.getCode())
				.build();

			TagResponseDto responseDto = TagResponseDto.builder()
				.tagId(1L)
				.tagName("")
				.tagType(TagType.CUSTOM)
				.tagUseCount(0L)
				.build();

			when(tagService.createTag(any(TagCreateRequestDto.class)))
				.thenReturn(responseDto);

			// when & then
			mockMvc.perform(post("/api/tags")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(requestDto))
					.with(csrf())
					.with(user(createTestUser())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.tagName").value(""));
		}

		@Test
		@DisplayName("null 태그명으로 태그 생성 성공")
		void createTag_NullTagName() throws Exception {
			// given
			TagCreateRequestDto requestDto = TagCreateRequestDto.builder()
				.tagName(null)
				.tagType(TagType.CUSTOM.getCode())
				.build();

			TagResponseDto responseDto = TagResponseDto.builder()
				.tagId(2L)
				.tagName(null)
				.tagType(TagType.CUSTOM)
				.tagUseCount(0L)
				.build();

			when(tagService.createTag(any(TagCreateRequestDto.class)))
				.thenReturn(responseDto);

			// when & then
			mockMvc.perform(post("/api/tags")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(requestDto))
					.with(csrf())
					.with(user(createTestUser())))
				.andExpect(status().isOk());
		}
	}

	@Nested
	@DisplayName("태그 조회 추가 테스트")
	class AdditionalTagRetrievalTest {

		@Test
		@DisplayName("빈 태그 목록 조회 시 빈 배열 반환")
		void getAllTags_EmptyList() throws Exception {
			// given
			when(tagService.getAllTags()).thenReturn(List.of());

			// when & then
			mockMvc.perform(get("/api/tags")
					.with(user(createTestUser())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.data").isArray())
				.andExpect(jsonPath("$.data").isEmpty());

			verify(tagService).getAllTags();
		}

		@Test
		@DisplayName("여러 태그 조회 시 올바른 순서로 반환")
		void getAllTags_MultipleTagsOrdered() throws Exception {
			// given
			List<TagResponseDto> tags = List.of(
				TagResponseDto.builder()
					.tagId(1L)
					.tagName("서울특별시")
					.tagType(TagType.SIDO)
					.tagUseCount(5L)
					.createdAt(LocalDateTime.now())
					.build(),
				TagResponseDto.builder()
					.tagId(2L)
					.tagName("강남구")
					.tagType(TagType.SIGUNGU)
					.tagUseCount(3L)
					.createdAt(LocalDateTime.now())
					.build(),
				TagResponseDto.builder()
					.tagId(3L)
					.tagName("여름")
					.tagType(TagType.SEASON)
					.tagUseCount(10L)
					.createdAt(LocalDateTime.now())
					.build()
			);
			when(tagService.getAllTags()).thenReturn(tags);

			// when & then
			mockMvc.perform(get("/api/tags")
					.with(user(createTestUser())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.data").isArray())
				.andExpect(jsonPath("$.data").hasJsonPath())
				.andExpect(jsonPath("$.data[0].tagName").value("서울특별시"))
				.andExpect(jsonPath("$.data[0].tagType").value("SIDO"))
				.andExpect(jsonPath("$.data[1].tagName").value("강남구"))
				.andExpect(jsonPath("$.data[1].tagType").value("SIGUNGU"))
				.andExpect(jsonPath("$.data[2].tagName").value("여름"))
				.andExpect(jsonPath("$.data[2].tagType").value("SEASON"));

			verify(tagService).getAllTags();
		}
	}

	private CustomUserDetails createTestUser() {
		return CustomUserDetails.builder().userId(1L).userName("testuser").roleId(1L).build();
	}
}
