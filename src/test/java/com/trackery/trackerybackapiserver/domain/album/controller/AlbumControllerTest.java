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

import com.github.pagehelper.PageInfo;
import com.trackery.trackerybackapiserver.domain.album.dto.request.AlbumCreateRequestDto;
import com.trackery.trackerybackapiserver.domain.album.dto.request.AlbumImageEditRequestDto;
import com.trackery.trackerybackapiserver.domain.album.dto.request.AlbumUpdateRequestDto;
import com.trackery.trackerybackapiserver.domain.album.dto.response.AlbumCreateResponseDto;
import com.trackery.trackerybackapiserver.domain.album.dto.response.AlbumDetailedResponseDto;
import com.trackery.trackerybackapiserver.domain.album.dto.response.AlbumImageEditResponseDto;
import com.trackery.trackerybackapiserver.domain.album.dto.response.AlbumSimpledResponseDto;
import com.trackery.trackerybackapiserver.domain.album.dto.response.MyAlbumResponseDto;
import com.trackery.trackerybackapiserver.domain.album.service.AlbumService;
import com.trackery.trackerybackapiserver.domain.common.util.PaginationDocumentationUtils;
import com.trackery.trackerybackapiserver.domain.config.CommonMockMvcControllerTestSetUp;
import com.trackery.trackerybackapiserver.domain.image.dto.ImageThumbnailDto;
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
 * 25. 5. 29.		durururuk		AlbumController 단위 테스트 작성
 * 25. 6. 11.		durururuk		앨범 이미지 삭제 기능 구현
 * 25. 6. 12.		durururuk		AlbumControllerTest 새로운 API 유닛테스트 작성
 * 25. 6. 13.		durururuk		앨범 생성, 앨범에 이미지 추가 API 문서화 코드 작성
 * 25. 6. 13.		durururuk		앨범 관련 API 컨트롤러 테스트 코드에 문서화 코드 작성
 * 25. 6. 16.		durururuk		문서에서 필드 타입 NULL로 나오는 일부 필드들 문제 수정
 * 25. 6. 16.		durururuk		엔드포인트에 맞게 컨트롤러 테스트코드, API 문서 수정
 * 25. 6. 23.		durururuk		수정된 로직에 맞게 Controller 테스트 코드 수정
 * 25. 6. 23.		durururuk		deprecated된 앨범 상세 정보 조회API 관련 코드 삭제
 * 25. 6. 24.		durururuk		getAlbumMetadata 컨트롤러 테스트 코드, 문서화 코드 작성
 * 25. 6. 24.		durururuk		앨범 이미지 조회 API 테스트코드, 문서화 코드 작성
 * 25. 6. 24.		durururuk		쿼리 파라미터도 문서화 클래스에서 받아올 수 있게 수정
 * 25. 7. 1.		durururuk		변경된 서비스 로직에 맞게 테스트코드 수정
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
	void getAlbumMetadataSuccess() throws Exception {
		final Long ALBUM_ID = 16L;
		final Long USER_ID = 1L;
		final String TITLE = "부산 여행";
		final String DESCRIPTION = "서면-광안리-해운대 여행";
		final int IS_PUBLIC = 1;
		final int IMAGE_COUNT = 3;
		AlbumDetailedResponseDto albumDetailedResponseDto = AlbumDetailedResponseDto
			.builder()
			.albumId(ALBUM_ID)
			.createdUserId(USER_ID)
			.albumTitle(TITLE)
			.albumDescription(DESCRIPTION)
			.isPublic(IS_PUBLIC)
			.imageCount(IMAGE_COUNT)
			.build();

		when(albumService.getAlbumMetadata(USER_ID, ALBUM_ID)).thenReturn(albumDetailedResponseDto);

		ResultActions result = mockMvc.perform(get("/api/albums/{albumId}", ALBUM_ID)
			.with(user(customUserDetails)));

		result.andExpect(status().isOk());

		result.andDo(document("get-album-metadata",
				pathParameters(
					parameterWithName("albumId").description("앨범 ID")
				),

				responseFields(
					fieldWithPath("code").description("응답 코드"),
					fieldWithPath("message").description("응답 메시지"),
					fieldWithPath("data.albumId").description("앨범 ID"),
					fieldWithPath("data.createdUserId").description("앨범 생성한 유저 ID"),
					fieldWithPath("data.albumTitle").description("앨범 제목"),
					fieldWithPath("data.albumDescription").description("앨범 설명"),
					fieldWithPath("data.isPublic").description("공개 여부"),
					fieldWithPath("data.imageCount").description("이미지 장수")
				)
			)
		);
	}

	@Test
	void getAlbumImagesSuccess() throws Exception {
		ImageThumbnailDto imageThumbnailDto = ImageThumbnailDto.builder().imageId(49L).thumbnailUrl("https://s3.thumbnailImage.jpg").build();
		List<ImageThumbnailDto> imageThumbnailDtoList = List.of(imageThumbnailDto);

		PageInfo<ImageThumbnailDto> pageInfo = new PageInfo<>(imageThumbnailDtoList);
		when(albumService.getAlbumImages(16L, 1L, 1, 10)).thenReturn(pageInfo);

		ResultActions result = mockMvc.perform(get("/api/albums/{albumId}/images", 16L)
			.with(user(customUserDetails))
			.queryParam("pageNum", "1")
			.queryParam("pageSize", "10")
		);

		result.andExpect(status().isOk());

		result.andDo(document("get-album-images",
			queryParameters(
				PaginationDocumentationUtils.getPageableQueryParameters()
			),

			responseFields(
				fieldWithPath("code").description("응답 코드"),
				fieldWithPath("message").description("응답 메시지"),
				fieldWithPath("data.list[]").description("이미지 썸네일 목록"),
				fieldWithPath("data.list[].imageId").description("이미지 ID"),
				fieldWithPath("data.list[].thumbnailUrl").description("이미지 썸네일 주소")
			).andWithPrefix("", PaginationDocumentationUtils.getPageableResponseFields())
		));
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