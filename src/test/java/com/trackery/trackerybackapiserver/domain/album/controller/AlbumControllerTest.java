package com.trackery.trackerybackapiserver.domain.album.controller;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.ResultActions;

import com.trackery.trackerybackapiserver.domain.album.dto.request.AlbumCreateRequestDto;
import com.trackery.trackerybackapiserver.domain.album.dto.request.AlbumImageEditRequestDto;
import com.trackery.trackerybackapiserver.domain.album.dto.request.AlbumUpdateRequestDto;
import com.trackery.trackerybackapiserver.domain.album.dto.response.AlbumCreateResponseDto;
import com.trackery.trackerybackapiserver.domain.album.dto.response.AlbumImageEditResponseDto;
import com.trackery.trackerybackapiserver.domain.album.dto.response.AlbumSimpledResponseDto;
import com.trackery.trackerybackapiserver.domain.album.dto.response.MyAlbumResponseDto;
import com.trackery.trackerybackapiserver.domain.album.service.AlbumService;
import com.trackery.trackerybackapiserver.domain.config.CommonMockMvcControllerTestSetUp;
import com.trackery.trackerybackapiserver.domain.user.entity.CustomUserDetails;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.album.controller
 * fileName       : AlbumControllerTest
 * author         : durururuk
 * date           : 25. 5. 29.
 * description    : AlbumController MockMVC 테스트
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 29.		durururuk		최초 생성
 */
@WebMvcTest(AlbumController.class)
class AlbumControllerTest extends CommonMockMvcControllerTestSetUp {
	@MockitoBean
	private AlbumService albumService;

	private final CustomUserDetails customUserDetails = CustomUserDetails.builder()
		.userId(1L)
		.userName("abcdefg")
		.roleId(1L)
		.build();

	@Test
	void createAlbumSuccess() throws Exception {
		AlbumCreateRequestDto requestDto = new AlbumCreateRequestDto();
		ReflectionTestUtils.setField(requestDto, "albumTitle", "강릉 여행");
		ReflectionTestUtils.setField(requestDto, "albumDescription", "강릉 여행 기록");
		ReflectionTestUtils.setField(requestDto, "isPublic", 0);

		AlbumCreateResponseDto responseDto = AlbumCreateResponseDto.builder()
			.albumId(1L)
			.albumTitle("강릉 여행")
			.albumDescription("강릉 여행 기록")
			.build();

		when(albumService.insertAlbum(any(), any())).thenReturn(responseDto);

		ResultActions result = mockMvc.perform(post("/api/albums")
			.with(user(customUserDetails))
			.with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(requestDto)));

		result
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data").exists())
			.andExpect(jsonPath("$.data.albumId").value(1))
			.andExpect(jsonPath("$.data.albumTitle").value("강릉 여행"))
			.andExpect(jsonPath("$.data.albumDescription").value("강릉 여행 기록"));

		result.andDo(document("create-album",
			requestFields(
				fieldWithPath("albumTitle").description("앨범 제목(필수)").type(JsonFieldType.STRING),
				fieldWithPath("albumDescription").description("앨범 설명").type(JsonFieldType.STRING).optional(),
				fieldWithPath("isPublic").description("공개 여부(필수)").type(JsonFieldType.NUMBER)
			),

			responseFields(
				fieldWithPath("code").description("응답 코드"),
				fieldWithPath("message").description("응답 메시지"),
				fieldWithPath("data.albumId").description("생성된 앨범 ID"),
				fieldWithPath("data.albumTitle").description("앨범 제목"),
				fieldWithPath("data.albumDescription").description("앨범 설명")
			)));
	}

	@Test
	void addImagesIntoAlbumSuccess() throws Exception {
		AlbumImageEditRequestDto requestDto = new AlbumImageEditRequestDto();
		ReflectionTestUtils.setField(requestDto, "imageIdList", List.of(10L, 20L));

		HashMap<Long, String> failedImageMap = new HashMap<>();
		failedImageMap.put(20L, "이미지를 찾지 못했습니다.");

		AlbumImageEditResponseDto responseDto = AlbumImageEditResponseDto.builder()
			.albumId(1L)
			.succeededImageCount(1)
			.failedImageCount(1)
			.succeededImageIds(new HashSet<>(List.of(10L)))
			.failedImageIds(failedImageMap)
			.build();

		when(albumService.addImageIntoAlbum(any(), any(), any())).thenReturn(responseDto);

		ResultActions result = mockMvc.perform(post("/api/albums/{albumId}/images", 1L)
			.with(user(customUserDetails))
			.with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(requestDto)));

		result
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data").exists())
			.andExpect(jsonPath("$.data.albumId").value(1))
			.andExpect(jsonPath("$.data.succeededImageCount").value(1))
			.andExpect(jsonPath("$.data.failedImageCount").value(1))
			.andExpect(jsonPath("$.data.succeededImageIds").isArray())
			.andExpect(jsonPath("$.data.succeededImageIds").isNotEmpty())
			.andExpect(jsonPath("$.data.failedImageIds").exists())
			.andExpect(jsonPath("$.data.failedImageIds").isNotEmpty());

		result
			.andDo(document("add-images-into-album",
					pathParameters(
						parameterWithName("albumId").description("앨범 ID")
					),
					requestFields(
						fieldWithPath("imageIdList").description("앨범에 추가할 이미지 ID 리스트")
					),

					responseFields(
						fieldWithPath("code").description("응답 코드"),
						fieldWithPath("message").description("응답 메시지"),
						fieldWithPath("data.albumId").description("앨범 ID"),
						fieldWithPath("data.succeededImageCount").description("성공한 이미지 개수"),
						fieldWithPath("data.failedImageCount").description("실패한 이미지 개수"),
						fieldWithPath("data.succeededImageIds").description("성공한 이미지 ID 리스트"),
						fieldWithPath("data.failedImageIds").description("실패한 이미지 ID 맵"),
						fieldWithPath("data.failedImageIds.*").description("실패한 이미지 ID별 오류 메시지")
					)
				)
			);
	}

	@Test
	void updateAlbumInfoSuccess() throws Exception {
		AlbumUpdateRequestDto requestDto = AlbumUpdateRequestDto.builder()
			.albumTitle("앨범 제목 수정")
			.albumDescription("앨범 설명 수정")
			.isPublic(1)
			.build();

		doNothing().when(albumService).updateAlbumInfo(any(), any(), any());

		ResultActions result = mockMvc.perform(patch("/api/albums/{albumId}", 1L)
			.with(user(customUserDetails))
			.with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(requestDto)));

		result
			.andExpect(status().isOk());

		result.andDo(document("update-album-info",
			pathParameters(
				parameterWithName("albumId").description("앨범 ID")
			),
			requestFields(
				fieldWithPath("albumTitle").description("앨범 제목").type(JsonFieldType.STRING),
				fieldWithPath("albumDescription").description("앨범 설명").type(JsonFieldType.STRING),
				fieldWithPath("isPublic").description("공개 여부").type(JsonFieldType.NUMBER)
			),

			responseFields(
				fieldWithPath("code").description("응답 코드"),
				fieldWithPath("message").description("응답 메시지")
			)
		));
	}

	@Test
	void deleteImagesFromAlbumSuccess() throws Exception {
		AlbumImageEditRequestDto requestDto = new AlbumImageEditRequestDto();
		ReflectionTestUtils.setField(requestDto, "imageIdList", List.of(10L, 20L));

		HashMap<Long, String> failedImageMap = new HashMap<>();
		failedImageMap.put(20L, "이미지를 찾지 못했습니다.");

		AlbumImageEditResponseDto responseDto = AlbumImageEditResponseDto.builder()
			.albumId(1L)
			.succeededImageCount(1)
			.failedImageCount(1)
			.succeededImageIds(new HashSet<>(List.of(10L)))
			.failedImageIds(failedImageMap)
			.build();

		when(albumService.deleteImageFromAlbum(any(), any(), any())).thenReturn(responseDto);

		ResultActions result = mockMvc.perform(delete("/api/albums/{albumId}/images", 1L)
			.with(user(customUserDetails))
			.with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(requestDto)));

		result
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data").exists())
			.andExpect(jsonPath("$.data.albumId").value(1))
			.andExpect(jsonPath("$.data.succeededImageCount").value(1))
			.andExpect(jsonPath("$.data.failedImageCount").value(1))
			.andExpect(jsonPath("$.data.succeededImageIds").isArray())
			.andExpect(jsonPath("$.data.succeededImageIds").isNotEmpty())
			.andExpect(jsonPath("$.data.failedImageIds").exists())
			.andExpect(jsonPath("$.data.failedImageIds").isNotEmpty());

		result.andDo(document("delete-images-from-album",
				pathParameters(
					parameterWithName("albumId").description("앨범 ID")
				),
				requestFields(
					fieldWithPath("imageIdList").description("앨범 ID 리스트")
				),

				responseFields(
					fieldWithPath("code").description("응답 코드"),
					fieldWithPath("message").description("응답 메시지"),
					fieldWithPath("data.albumId").description("앨범 ID"),
					fieldWithPath("data.succeededImageCount").description("성공한 이미지 개수"),
					fieldWithPath("data.failedImageCount").description("실패한 이미지 개수"),
					fieldWithPath("data.succeededImageIds").description("성공한 이미지 ID 리스트"),
					fieldWithPath("data.failedImageIds").description("실패한 이미지 ID 맵"),
					fieldWithPath("data.failedImageIds.*").description("실패한 이미지 ID별 오류 메시지")
				)
			)
		);
	}

	@Test
	void getMyAlbumSimpleInfoSuccess() throws Exception {
		AlbumSimpledResponseDto albumSimpledResponseDto = AlbumSimpledResponseDto.builder()
			.albumId(1L)
			.albumTitle("앨범 제목")
			.albumImageCount(3)
			.isPublic(1)
			.albumThumbnailUrl("https://s3.album1.thumbnailImage.jpg")
			.build();

		MyAlbumResponseDto myAlbumResponseDto = MyAlbumResponseDto.builder()
			.userId(1L)
			.albumCount(1)
			.albumList(List.of(albumSimpledResponseDto))
			.build();

		when(albumService.getMyAlbumSimpleInfo(any())).thenReturn(myAlbumResponseDto);

		ResultActions result = mockMvc.perform(get("/api/albums/me")
			.with(user(customUserDetails))
			.with(csrf()));

		result
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data").exists())
			.andExpect(jsonPath("$.data.albumCount").value(1))
			.andExpect(jsonPath("$.data.albumList").isArray())
			.andExpect(jsonPath("$.data.albumList").isNotEmpty())
			.andExpect(jsonPath("$.data.albumList[0].albumId").value(1))
			.andExpect(jsonPath("$.data.albumList[0].albumImageCount").value(3))
			.andExpect(jsonPath("$.data.albumList[0].isPublic").value(1))
			.andExpect(jsonPath("$.data.albumList[0].albumThumbnailUrl").value("https://s3.album1.thumbnailImage.jpg"));

		result.andDo(document("get-my-album-simple-info",
				responseFields(
					fieldWithPath("code").description("응답 코드"),
					fieldWithPath("message").description("응답 메시지"),
					fieldWithPath("data.userId").description("요청 유저 ID"),
					fieldWithPath("data.albumCount").description("앨범 개수"),
					fieldWithPath("data.albumList").description("앨범 리스트"),
					fieldWithPath("data.albumList[].albumId").description("앨범 ID"),
					fieldWithPath("data.albumList[].albumTitle").description("앨범 제목"),
					fieldWithPath("data.albumList[].albumImageCount").description("앨범 이미지 개수"),
					fieldWithPath("data.albumList[].isPublic").description("공개 여부"),
					fieldWithPath("data.albumList[0].albumThumbnailUrl").description("앨범 썸네일 이미지 URL")
				)
			)
		);
	}

	@Test
	void deleteAlbumSuccess() throws Exception {
		Long albumId = 1L;

		doNothing().when(albumService).deleteAlbum(any(), eq(albumId));

		ResultActions result = mockMvc.perform(delete("/api/albums/{albumId}", albumId)
			.with(user(customUserDetails))
			.with(csrf()));

		result
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data").doesNotExist());

		result
			.andDo(document("delete-album",
					pathParameters(
						parameterWithName("albumId").description("앨범 ID")
					),
					responseFields(
						fieldWithPath("code").description("응답 코드"),
						fieldWithPath("message").description("응답 메시지")
					)
				)
			);
	}
}