package com.trackery.trackerybackapiserver.domain.album.controller;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.ResultActions;

import com.trackery.trackerybackapiserver.domain.album.dto.request.AlbumCreateRequestDto;
import com.trackery.trackerybackapiserver.domain.album.dto.request.AlbumImageEditRequestDto;
import com.trackery.trackerybackapiserver.domain.album.dto.request.AlbumUpdateRequestDto;
import com.trackery.trackerybackapiserver.domain.album.dto.response.AlbumCreateResponseDto;
import com.trackery.trackerybackapiserver.domain.album.dto.response.AlbumDetailedResponseDto;
import com.trackery.trackerybackapiserver.domain.album.dto.response.AlbumImageEditResponseDto;
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
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data").exists())
			.andExpect(jsonPath("$.data.albumId").value(1))
			.andExpect(jsonPath("$.data.albumTitle").value("강릉 여행"))
			.andExpect(jsonPath("$.data.albumDescription").value("강릉 여행 기록"));
	}

	@Test
	void addImagesIntoAlbumSuccess() throws Exception {
		AlbumImageEditRequestDto requestDto = new AlbumImageEditRequestDto();
		ReflectionTestUtils.setField(requestDto, "albumId", 1L);
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

		ResultActions result = mockMvc.perform(post("/api/albums/images")
			.with(user(customUserDetails))
			.with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(requestDto)));

		result
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data").exists())
			.andExpect(jsonPath("$.data.albumId").value(1))
			.andExpect(jsonPath("$.data.succeededImageCount").value(1))
			.andExpect(jsonPath("$.data.failedImageCount").value(1))
			.andExpect(jsonPath("$.data.succeededImageIds").isArray())
			.andExpect(jsonPath("$.data.succeededImageIds").isNotEmpty())
			.andExpect(jsonPath("$.data.failedImageIds").exists())
			.andExpect(jsonPath("$.data.failedImageIds").isNotEmpty());
	}

	@Test
	void getAlbumDetailedInfoSuccess() throws Exception {
		Long albumId = 1L;

		AlbumDetailedResponseDto responseDto = AlbumDetailedResponseDto.builder()
			.albumId(albumId)
			.createdUserId(customUserDetails.getUserId())
			.albumTitle("강릉 여행")
			.albumDescription("강릉 여행 기록")
			.imageList(List.of())
			.build();

		when(albumService.getAlbumDetailedInfo(any(), eq(albumId))).thenReturn(responseDto);

		ResultActions result = mockMvc.perform(get("/api/albums")
			.with(user(customUserDetails))
			.with(csrf())
			.param("albumId", String.valueOf(1L)));

		result
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data").exists())
			.andExpect(jsonPath("$.data.albumId").value(1))
			.andExpect(jsonPath("$.data.createdUserId").value(1))
			.andExpect(jsonPath("$.data.albumTitle").value("강릉 여행"))
			.andExpect(jsonPath("$.data.albumDescription").value("강릉 여행 기록"))
			.andExpect(jsonPath("$.data.imageList").isArray())
			.andExpect(jsonPath("$.data.imageList").exists());
	}

	@Test
	void updateAlbumInfoSuccess() throws Exception {
		AlbumUpdateRequestDto requestDto = new AlbumUpdateRequestDto();
		ReflectionTestUtils.setField(requestDto, "albumId", 1L);

		doNothing().when(albumService).updateAlbumInfo(any(), any());

		ResultActions result = mockMvc.perform(patch("/api/albums")
			.with(user(customUserDetails))
			.with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(requestDto)));

		result
			.andDo(print())
			.andExpect(status().isOk());
	}
}