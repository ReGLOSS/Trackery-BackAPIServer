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

import com.trackery.trackerybackapiserver.domain.config.CommonMockMvcControllerTestSetUp;
import com.trackery.trackerybackapiserver.domain.tag.dto.TagCreateRequestDto;
import com.trackery.trackerybackapiserver.domain.tag.dto.TagDefaultRequestDto;
import com.trackery.trackerybackapiserver.domain.tag.dto.TagNameResponseDto;
import com.trackery.trackerybackapiserver.domain.tag.dto.TagResponseDto;
import com.trackery.trackerybackapiserver.domain.tag.dto.TagUpdateRequestDto;
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
 */
@WebMvcTest(TagController.class)
class TagControllerTest extends CommonMockMvcControllerTestSetUp {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private TagService tagService;

	private TagResponseDto tagResponseDto;
	private TagCreateRequestDto createRequestDto;
	private TagUpdateRequestDto updateRequestDto;
	private TagDefaultRequestDto defaultRequestDto;

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

		updateRequestDto = new TagUpdateRequestDto("수정된태그");

		defaultRequestDto = new TagDefaultRequestDto("2024/7/15 14:30:25");
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
						fieldWithPath("tagType").description("태그 타입 코드 (0: CUSTOM, 1: LOCATION, 2: SEASON, 3: TIME, 4: WEATHER)")
					),
					relaxedResponseFields(
						fieldWithPath("code").description("응답 코드"),
						fieldWithPath("message").description("응답 메시지"),
						fieldWithPath("data.tagId").description("태그 ID"),
						fieldWithPath("data.tagName").description("태그명"),
						fieldWithPath("data.tagType").description("태그 타입(CUSTOM, LOCATION 등)"),
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
						fieldWithPath("data[].tagType").description("태그 타입(CUSTOM, LOCATION 등)"),
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
						fieldWithPath("data[].tagType").description("태그 타입(CUSTOM, LOCATION 등)"),
						fieldWithPath("data[].tagUseCount").description("태그 사용 횟수"),
						fieldWithPath("data[].createdAt").description("태그 생성 시간")
					)
				));

			verify(tagService).getSystemTags();
		}

		@Test
		@DisplayName("이미지별 태그 조회 성공")
		void getTagsByImageId_Success() throws Exception {
			// given
			Long imageId = 1L;
			List<TagResponseDto> tags = List.of(tagResponseDto);
			when(tagService.getTagsByImageId(imageId)).thenReturn(tags);

			// when & then
			mockMvc.perform(get("/api/tags/image/{imageId}", imageId)
					.with(user(createTestUser())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.data").isArray())
				.andExpect(jsonPath("$.data[0].tagId").value(1L))
				.andDo(document("tag-get-by-image",
					pathParameters(
						parameterWithName("imageId").description("이미지 ID")
					),
					relaxedResponseFields(
						fieldWithPath("code").description("응답 코드"),
						fieldWithPath("message").description("응답 메시지"),
						fieldWithPath("data[].tagId").description("태그 ID"),
						fieldWithPath("data[].tagName").description("태그명"),
						fieldWithPath("data[].tagType").description("태그 타입(CUSTOM, LOCATION 등)"),
						fieldWithPath("data[].tagUseCount").description("태그 사용 횟수"),
						fieldWithPath("data[].createdAt").description("태그 생성 시간")
					)
				));

			verify(tagService).getTagsByImageId(imageId);
		}
	}

	@Nested
	@DisplayName("이미지-태그 연결 API 테스트")
	class ImageTagConnectionTest {

		@Test
		@DisplayName("이미지에 태그 추가 성공")
		void addTagToImage_Success() throws Exception {
			// given
			Long imageId = 1L;
			Long tagId = 1L;
			doNothing().when(tagService).addTagToImage(imageId, tagId);

			// when & then
			mockMvc.perform(post("/api/tags/image/{imageId}/tag/{tagId}", imageId, tagId)
					.with(csrf())
					.with(user(createTestUser())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(200))
				.andDo(document("tag-add-to-image",
					pathParameters(
						parameterWithName("imageId").description("이미지 ID"),
						parameterWithName("tagId").description("태그 ID")
					),
					relaxedResponseFields(
						fieldWithPath("code").description("응답 코드"),
						fieldWithPath("message").description("응답 메시지")
					)
				));

			verify(tagService).addTagToImage(imageId, tagId);
		}

		@Test
		@DisplayName("이미지에서 태그 제거 성공")
		void removeTagFromImage_Success() throws Exception {
			// given
			Long imageId = 1L;
			Long tagId = 1L;
			doNothing().when(tagService).removeTagFromImage(imageId, tagId);

			// when & then
			mockMvc.perform(delete("/api/tags/image/{imageId}/tag/{tagId}", imageId, tagId)
					.with(csrf())
					.with(user(createTestUser())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(200))
				.andDo(document("tag-remove-from-image",
					pathParameters(
						parameterWithName("imageId").description("이미지 ID"),
						parameterWithName("tagId").description("태그 ID")
					),
					relaxedResponseFields(
						fieldWithPath("code").description("응답 코드"),
						fieldWithPath("message").description("응답 메시지")
					)
				));

			verify(tagService).removeTagFromImage(imageId, tagId);
		}

		@Test
		@DisplayName("이미지 태그 수정 성공")
		void updateImageTag_Success() throws Exception {
			// given
			Long imageId = 1L;
			Long tagId = 1L;
			TagResponseDto updatedTag = TagResponseDto.builder()
				.tagId(2L)
				.tagName("수정된태그")
				.tagType(TagType.CUSTOM)
				.tagUseCount(1L)
				.createdAt(LocalDateTime.now())
				.build();

			when(tagService.updateImageTag(imageId, tagId, "수정된태그")).thenReturn(updatedTag);

			// when & then
			mockMvc.perform(put("/api/tags/image/{imageId}/tag/{tagId}", imageId, tagId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(updateRequestDto))
					.with(csrf())
					.with(user(createTestUser())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.data.tagId").value(2L))
				.andExpect(jsonPath("$.data.tagName").value("수정된태그"))
				.andDo(document("tag-update-image-tag",
					pathParameters(
						parameterWithName("imageId").description("이미지 ID"),
						parameterWithName("tagId").description("기존 태그 ID")
					),
					requestFields(
						fieldWithPath("newTagName").description("새로운 태그명")
					),
					relaxedResponseFields(
						fieldWithPath("code").description("응답 코드"),
						fieldWithPath("message").description("응답 메시지"),
						fieldWithPath("data.tagId").description("새로운 태그 ID"),
						fieldWithPath("data.tagName").description("새로운 태그명"),
						fieldWithPath("data.tagType").description("태그 타입(CUSTOM, LOCATION 등)"),
						fieldWithPath("data.tagUseCount").description("태그 사용 횟수"),
						fieldWithPath("data.createdAt").description("태그 생성 시간")
					)
				));

			verify(tagService).updateImageTag(imageId, tagId, "수정된태그");
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
	@DisplayName("기본 태그 생성 API 테스트")
	class DefaultTagTest {

		@Test
		@DisplayName("기본 태그 생성 성공")
		void getDefaultTags_Success() throws Exception {
			// given
			List<TagNameResponseDto> defaultTags = List.of(
				TagNameResponseDto.builder().tagName("여름").build(),
				TagNameResponseDto.builder().tagName("오후").build()
			);
			when(tagService.createDefaultTags("2024/7/15 14:30:25")).thenReturn(defaultTags);

			// when & then
			mockMvc.perform(post("/api/tags/default")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(defaultRequestDto))
					.with(csrf())
					.with(user(createTestUser())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.data").isArray())
				.andExpect(jsonPath("$.data[0].tagName").value("여름"))
				.andExpect(jsonPath("$.data[1].tagName").value("오후"))
				.andDo(document("tag-create-default",
					requestFields(
						fieldWithPath("dateTime").description("날짜/시간 문자열 (예: 2024/7/15 14:30:25)")
					),
					relaxedResponseFields(
						fieldWithPath("code").description("응답 코드"),
						fieldWithPath("message").description("응답 메시지"),
						fieldWithPath("data[].tagName").description("생성된 기본 태그명")
					)
				));

			verify(tagService).createDefaultTags("2024/7/15 14:30:25");
		}
	}

	private CustomUserDetails createTestUser() {
		return CustomUserDetails.builder().userId(1L).userName("testuser").roleId(1L).build();
	}
}
