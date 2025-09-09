package com.trackery.trackerybackapiserver.domain.image.controller;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageInfo;
import com.trackery.trackerybackapiserver.domain.config.CommonMockMvcControllerTestSetUp;
import com.trackery.trackerybackapiserver.domain.image.dto.ImageDto;
import com.trackery.trackerybackapiserver.domain.image.dto.ImageSidoCoverageResponseDto;
import com.trackery.trackerybackapiserver.domain.image.dto.ImageThumbnailDto;
import com.trackery.trackerybackapiserver.domain.image.dto.ImageUpdateRequestDto;
import com.trackery.trackerybackapiserver.domain.image.dto.internal.ImageSearchByUserIdDto;
import com.trackery.trackerybackapiserver.domain.image.service.ImageService;
import com.trackery.trackerybackapiserver.domain.tag.dto.TagResponseDto;
import com.trackery.trackerybackapiserver.domain.tag.dto.TagUpdateRequestDto;
import com.trackery.trackerybackapiserver.domain.tag.enums.TagType;
import com.trackery.trackerybackapiserver.domain.tag.service.TagService;
import com.trackery.trackerybackapiserver.domain.user.entity.CustomUserDetails;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.image.controller
 * fileName       : ImageControllerTest
 * author         : durururuk
 * date           : 25. 5. 22.
 * description    : ImageControlelr MockMvc 단위테스트코드
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 22.		durururuk		최초 생성
 * 25. 5. 22.		durururuk		ImageControllerTest 작성
 * 25. 5. 29.		durururuk		AlbumController 단위 테스트 작성
 * 25. 6. 18.		durururuk		getImageListByUserIdV2 컨트롤러 테스트 코드, 문서화 코드 작성
 * 25. 6. 18.		inari		모든 api 문서화
 * 25. 6. 20.		inari		이미지 수정 및 삭제기능 추가
 * 25. 6. 20.		inari		이미지 컨트롤러 테스트 수정
 * 25. 6. 23.		durururuk		imageController에서도 deprecated된 API 삭제
 * 25. 6. 25.		inari		문서화 추가
 * 25. 7. 1.		durururuk		변경된 서비스 로직에 맞게 테스트코드 수정
 * 25. 7. 8.		durururuk		내 이미지 조회 시 조회 결과에서 제외될 앨범 ID 파라미터 추가
 * 25. 7. 8.		durururuk		변경된 로직에 맞게 테스트 코드 수정
 * 25. 7. 8.		durururuk		이미지 단건 조회 api 엔드포인트 pathVariable 방식으로 수정
 * 25. 7. 9.		inari		테스트코드 수정 및 문서 작성
 * 25. 7. 10.		inari		이미지 단건 조회시 태그 추가
 * 25. 7. 10.		durururuk		변경된 로직에 맞게 테스트 코드 수정
 * 25. 7. 14.		inari		테스트코드 수정 및 adoc 변경
 * 25. 9. 5.		durururuk		이미지 시도 커버리지 조회 API 테스트코드, 문서화 코드 작성
 * 25. 9. 9.		durururuk		시도 커버리지 테스트 자료형 List로 수정
 */
@WebMvcTest(ImageController.class)
class ImageControllerTest extends CommonMockMvcControllerTestSetUp {
	@MockitoBean
	private ImageService imageService;

	@MockitoBean
	private TagService tagService;

	private static final Long IMAGE_ID = 1L;
	private static final Long USER_ID = 2L;
	private static final String SD_NAME = "서울특별시";
	private static final String SGG_NAME = "강남구";
	private static final Double LATITUDE = 15.57;
	private static final Double LONGITUDE = 121.45;
	private static final String IMAGE_NAME = "image.jpg";
	private static final String IMAGE_CONTENT = "테스트용 이미지";
	private static final String IMAGE_URL = "http://test.com/api/images/image.jpg";
	private static final LocalDateTime IMAGE_REG_DATE = LocalDateTime.of(2025, 5, 22, 0, 0);
	private static final LocalDateTime IMAGE_DATE = LocalDateTime.of(2000, 1, 1, 0, 0);

	private ImageDto imageDto;
	private CustomUserDetails userDetails;

	@BeforeEach
	void setUp() {
		imageDto = ImageDto.builder()
			.imageId(IMAGE_ID)
			.userId(USER_ID)
			.imageRegDate(IMAGE_REG_DATE)
			.sdName(SD_NAME)
			.sggName(SGG_NAME)
			.latitude(LATITUDE)
			.longitude(LONGITUDE)
			.imageName(IMAGE_NAME)
			.imageContent(IMAGE_CONTENT)
			.imageDate(IMAGE_DATE)
			.imageUrl(IMAGE_URL)
			.isPublic(0)
			.tags(List.of())
			.build();

		userDetails = CustomUserDetails.builder()
			.userId(USER_ID)
			.roleId(3L)
			.userName("abcdefg")
			.build();
	}

	@Test
	@DisplayName("이미지 단건 조회 성공")
	void getImageDtoSuccess() throws Exception {
		when(imageService.getOriginalImageByImageId(USER_ID, IMAGE_ID)).thenReturn(imageDto);

		ResultActions result = mockMvc.perform(get("/api/images/{imageId}", IMAGE_ID)
			.with(user(userDetails)));

		result
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value(200))
			.andExpect(jsonPath("$.message").value("Ok"))
			.andExpect(jsonPath("$.data.imageId").value(IMAGE_ID))
			.andExpect(jsonPath("$.data.userId").value(USER_ID))
			.andExpect(jsonPath("$.data.sdName").value(SD_NAME))
			.andExpect(jsonPath("$.data.sggName").value(SGG_NAME))
			.andExpect(jsonPath("$.data.latitude").value(LATITUDE))
			.andExpect(jsonPath("$.data.longitude").value(LONGITUDE))
			.andExpect(jsonPath("$.data.imageName").value(IMAGE_NAME))
			.andExpect(jsonPath("$.data.imageContent").value(IMAGE_CONTENT))
			.andExpect(jsonPath("$.data.imageDate").value(IMAGE_DATE.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
			.andExpect(jsonPath("$.data.imageUrl").value(IMAGE_URL))
			.andDo(document("get-image-by-id-success",
				pathParameters(
					parameterWithName("imageId").description("조회할 이미지 ID")
				),
				responseFields(
					fieldWithPath("code").description("상태 코드"),
					fieldWithPath("message").description("응답 메시지"),
					fieldWithPath("data.imageId").description("이미지 ID"),
					fieldWithPath("data.userId").description("이미지 업로드 사용자 ID"),
					fieldWithPath("data.imageRegDate").description("이미지 등록 일시"),
					fieldWithPath("data.sdName").description("시도명"),
					fieldWithPath("data.sggName").description("시군구명"),
					fieldWithPath("data.latitude").description("위도"),
					fieldWithPath("data.longitude").description("경도"),
					fieldWithPath("data.imageName").description("이미지 파일명"),
					fieldWithPath("data.imageContent").description("이미지 설명"),
					fieldWithPath("data.imageDate").description("이미지 촬영 일시"),
					fieldWithPath("data.isPublic").description("공개 여부 (true: 공개, false: 비공개, null: 미설정)"),
					fieldWithPath("data.imageUrl").description("이미지 URL"),
					fieldWithPath("data.tags").description("이미지와 연결된 태그 목록")
				)
			));

		verify(imageService, times(1)).getOriginalImageByImageId(USER_ID, IMAGE_ID);
	}

	@Test
	@DisplayName("인증된 사용자의 이미지 목록 조회 페이지네이션 성공")
	void getMyImagesV2Success() throws Exception {
		ImageThumbnailDto imageThumbnailDto = ImageThumbnailDto.builder().imageId(IMAGE_ID).thumbnailUrl("s3.thumbnail.image.webp").build();
		List<ImageThumbnailDto> imageThumbnailDtoList = List.of(imageThumbnailDto);
		PageInfo<ImageThumbnailDto> pageInfo = new PageInfo<>(imageThumbnailDtoList);
		when(imageService.getImageListByUserId(any(ImageSearchByUserIdDto.class))).thenReturn(pageInfo);

		ResultActions result = mockMvc.perform(get("/api/images/me")
			.with(user(userDetails))
			.queryParam("pageNum", "1")
			.queryParam("pageSize", "10")
		);

		result
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value(200))
			.andExpect(jsonPath("$.message").value("Ok"))
			.andDo(document("get-my-images-success",
				queryParameters(
					parameterWithName("pageNum").description("페이지 번호 (1부터 시작, 기본값 : 1)"),
					parameterWithName("pageSize").description("페이지 크기 (기본값 : 10)"),
					parameterWithName("excludeAlbumId").description("조회 결과에서 제외할 앨범 ID (선택사항)").optional()
				),

				responseFields(
					fieldWithPath("code").description("응답 코드"),
					fieldWithPath("message").description("응답 메시지"),
					fieldWithPath("data").description("페이지네이션된 이미지 데이터"),
					fieldWithPath("data.total").description("전체 이미지 개수"),
					fieldWithPath("data.list[]").description("이미지 썸네일 목록"),
					fieldWithPath("data.list[].imageId").description("이미지 ID"),
					fieldWithPath("data.list[].thumbnailUrl").description("이미지 썸네일 주소"),
					fieldWithPath("data.pageNum").description("현재 페이지 번호"),
					fieldWithPath("data.pageSize").description("페이지 크기"),
					fieldWithPath("data.size").description("현재 페이지의 데이터 개수"),
					fieldWithPath("data.startRow").description("시작 행 번호"),
					fieldWithPath("data.endRow").description("끝 행 번호"),
					fieldWithPath("data.pages").description("전체 페이지 수"),
					fieldWithPath("data.prePage").description("이전 페이지 번호"),
					fieldWithPath("data.nextPage").description("다음 페이지 번호"),
					fieldWithPath("data.isFirstPage").description("첫 번째 페이지 여부"),
					fieldWithPath("data.isLastPage").description("마지막 페이지 여부"),
					fieldWithPath("data.hasPreviousPage").description("이전 페이지 존재 여부"),
					fieldWithPath("data.hasNextPage").description("다음 페이지 존재 여부"),
					fieldWithPath("data.navigatePages").description("네비게이션 페이지 수"),
					fieldWithPath("data.navigatepageNums").description("네비게이션 페이지 번호 배열"),
					fieldWithPath("data.navigateFirstPage").description("네비게이션 첫 페이지"),
					fieldWithPath("data.navigateLastPage").description("네비게이션 마지막 페이지")
				)
			));

		verify(imageService, times(1)).getImageListByUserId(any(ImageSearchByUserIdDto.class));
	}

	@Test
	@DisplayName("이미지 메타데이터 수정 성공")
	void updateImageMetadataSuccess() throws Exception {
		ImageUpdateRequestDto updateRequest = ImageUpdateRequestDto.builder()
			.imageName("수정된 이미지")
			.imageContent("수정된 설명")
			.isPublic(1)
			.build();

		ImageDto updatedImageDto = ImageDto.builder()
			.imageId(IMAGE_ID)
			.userId(USER_ID)
			.imageRegDate(IMAGE_REG_DATE)
			.sdName(SD_NAME)
			.sggName(SGG_NAME)
			.latitude(LATITUDE)
			.longitude(LONGITUDE)
			.imageName("수정된 이미지")
			.imageContent("수정된 설명")
			.imageDate(IMAGE_DATE)
			.imageUrl(IMAGE_URL)
			.isPublic(1)
			.tags(List.of())
			.build();

		when(imageService.updateImageMetadata(eq(IMAGE_ID), eq(USER_ID), any(ImageUpdateRequestDto.class)))
			.thenReturn(updatedImageDto);

		ObjectMapper objectMapper = new ObjectMapper();
		String requestJson = objectMapper.writeValueAsString(updateRequest);

		ResultActions result = mockMvc.perform(patch("/api/images/{imageId}", IMAGE_ID)
			.with(user(userDetails))
			.contentType(APPLICATION_JSON)
			.content(requestJson));

		result
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value(200))
			.andExpect(jsonPath("$.message").value("Ok"))
			.andExpect(jsonPath("$.data.imageId").value(IMAGE_ID))
			.andExpect(jsonPath("$.data.imageName").value("수정된 이미지"))
			.andExpect(jsonPath("$.data.imageContent").value("수정된 설명"))
			.andExpect(jsonPath("$.data.isPublic").value(1))
			.andDo(document("update-image-metadata-success",
				pathParameters(
					parameterWithName("imageId").description("수정할 이미지 ID")
				),
				requestFields(
					fieldWithPath("imageName").description("수정할 이미지 이름 (선택사항)").optional(),
					fieldWithPath("imageContent").description("수정할 이미지 설명 (선택사항)").optional(),
					fieldWithPath("imageDate").description("수정할 이미지 촬영 날짜 (선택사항)").optional(),
					fieldWithPath("isPublic").description("수정할 공개 여부 (0: 비공개, 1: 공개, 선택사항)").optional(),
					fieldWithPath("latitude").description("수정할 위도 (33.0-43.0, 선택사항)").optional(),
					fieldWithPath("longitude").description("수정할 경도 (124.0-132.0, 선택사항)").optional(),
					fieldWithPath("tagsToRemove").description("삭제할 태그 ID 목록 (선택사항)").optional(),
					fieldWithPath("tagsToAdd").description("추가할 태그명 목록 (선택사항)").optional()
				),
				responseFields(
					fieldWithPath("code").description("상태 코드"),
					fieldWithPath("message").description("응답 메시지"),
					fieldWithPath("data.imageId").description("이미지 ID"),
					fieldWithPath("data.userId").description("이미지 업로드 사용자 ID"),
					fieldWithPath("data.imageRegDate").description("이미지 등록 일시"),
					fieldWithPath("data.sdName").description("시도명"),
					fieldWithPath("data.sggName").description("시군구명"),
					fieldWithPath("data.latitude").description("위도"),
					fieldWithPath("data.longitude").description("경도"),
					fieldWithPath("data.imageName").description("이미지 파일명"),
					fieldWithPath("data.imageContent").description("이미지 설명"),
					fieldWithPath("data.imageDate").description("이미지 촬영 일시"),
					fieldWithPath("data.isPublic").description("공개 여부"),
					fieldWithPath("data.imageUrl").description("이미지 URL"),
					fieldWithPath("data.tags").description("이미지와 연결된 태그 목록")
				)
			));

		verify(imageService, times(1)).updateImageMetadata(eq(IMAGE_ID), eq(USER_ID), any(ImageUpdateRequestDto.class));
	}

	@Test
	@DisplayName("이미지 지역 정보 포함 메타데이터 수정 성공")
	void updateImageMetadataWithLocationSuccess() throws Exception {
		ImageUpdateRequestDto updateRequest = ImageUpdateRequestDto.builder()
			.imageName("수정된 이미지")
			.imageContent("수정된 설명")
			.isPublic(1)
			.latitude(37.5665)
			.longitude(126.978)
			.build();

		ImageDto updatedImageDto = ImageDto.builder()
			.imageId(IMAGE_ID)
			.userId(USER_ID)
			.imageRegDate(IMAGE_REG_DATE)
			.sdName("서울특별시")
			.sggName("중구")
			.latitude(37.5665)
			.longitude(126.978)
			.imageName("수정된 이미지")
			.imageContent("수정된 설명")
			.imageDate(IMAGE_DATE)
			.imageUrl(IMAGE_URL)
			.isPublic(1)
			.tags(List.of())
			.build();

		when(imageService.updateImageMetadata(eq(IMAGE_ID), eq(USER_ID), any(ImageUpdateRequestDto.class)))
			.thenReturn(updatedImageDto);

		ObjectMapper objectMapper = new ObjectMapper();
		String requestJson = objectMapper.writeValueAsString(updateRequest);

		ResultActions result = mockMvc.perform(patch("/api/images/{imageId}", IMAGE_ID)
			.with(user(userDetails))
			.contentType(APPLICATION_JSON)
			.content(requestJson));

		result
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value(200))
			.andExpect(jsonPath("$.message").value("Ok"))
			.andExpect(jsonPath("$.data.imageId").value(IMAGE_ID))
			.andExpect(jsonPath("$.data.latitude").value(37.5665))
			.andExpect(jsonPath("$.data.longitude").value(126.978))
			.andExpect(jsonPath("$.data.sggName").value("중구"))
			.andDo(document("update-image-metadata-with-location-success",
				pathParameters(
					parameterWithName("imageId").description("수정할 이미지 ID")
				),
				requestFields(
					fieldWithPath("imageName").description("수정할 이미지 이름"),
					fieldWithPath("imageContent").description("수정할 이미지 설명"),
					fieldWithPath("imageDate").description("수정할 이미지 촬영 날짜").optional(),
					fieldWithPath("isPublic").description("수정할 공개 여부 (0: 비공개, 1: 공개)"),
					fieldWithPath("latitude").description("수정할 위도 (33.0-43.0)"),
					fieldWithPath("longitude").description("수정할 경도 (124.0-132.0)"),
					fieldWithPath("tagsToRemove").description("삭제할 태그 ID 목록 (선택사항)").optional(),
					fieldWithPath("tagsToAdd").description("추가할 태그명 목록 (선택사항)").optional()
				),
				responseFields(
					fieldWithPath("code").description("상태 코드"),
					fieldWithPath("message").description("응답 메시지"),
					fieldWithPath("data.imageId").description("이미지 ID"),
					fieldWithPath("data.userId").description("이미지 업로드 사용자 ID"),
					fieldWithPath("data.imageRegDate").description("이미지 등록 일시"),
					fieldWithPath("data.sdName").description("시도명 (수정된 위치 기준)"),
					fieldWithPath("data.sggName").description("시군구명 (수정된 위치 기준)"),
					fieldWithPath("data.latitude").description("수정된 위도"),
					fieldWithPath("data.longitude").description("수정된 경도"),
					fieldWithPath("data.imageName").description("수정된 이미지 파일명"),
					fieldWithPath("data.imageContent").description("수정된 이미지 설명"),
					fieldWithPath("data.imageDate").description("이미지 촬영 일시"),
					fieldWithPath("data.isPublic").description("수정된 공개 여부"),
					fieldWithPath("data.imageUrl").description("이미지 URL"),
					fieldWithPath("data.tags").description("이미지와 연결된 태그 목록")
				)
			));

		verify(imageService, times(1)).updateImageMetadata(eq(IMAGE_ID), eq(USER_ID), any(ImageUpdateRequestDto.class));
	}

	@Test
	@DisplayName("이미지 삭제 성공")
	void deleteImageSuccess() throws Exception {
		doNothing().when(imageService).deleteImage(IMAGE_ID, USER_ID);

		ResultActions result = mockMvc.perform(delete("/api/images/{imageId}", IMAGE_ID)
			.with(user(userDetails)));

		result
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value(200))
			.andExpect(jsonPath("$.message").value("Ok"))
			.andExpect(jsonPath("$.data").doesNotExist())
			.andDo(document("delete-image-success",
				pathParameters(
					parameterWithName("imageId").description("삭제할 이미지 ID")
				),
				responseFields(
					fieldWithPath("code").description("상태 코드"),
					fieldWithPath("message").description("응답 메시지")
				)
			));

		verify(imageService, times(1)).deleteImage(IMAGE_ID, USER_ID);
	}

	@Test
	@DisplayName("이미지별 태그 조회 성공")
	void getTagsByImageId_Success() throws Exception {
		// given
		TagResponseDto tagResponseDto = TagResponseDto.builder()
			.tagId(1L)
			.tagName("테스트태그")
			.tagType(TagType.CUSTOM)
			.tagUseCount(1L)
			.createdAt(LocalDateTime.now())
			.build();

		List<TagResponseDto> tags = List.of(tagResponseDto);
		when(tagService.getTagsByImageId(IMAGE_ID)).thenReturn(tags);

		// when & then
		mockMvc.perform(get("/api/images/{imageId}/tags", IMAGE_ID)
				.with(user(userDetails)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value(200))
			.andExpect(jsonPath("$.data").isArray())
			.andExpect(jsonPath("$.data[0].tagId").value(1L))
			.andExpect(jsonPath("$.data[0].tagName").value("테스트태그"))
			.andDo(document("image-get-tags",
				pathParameters(
					parameterWithName("imageId").description("이미지 ID")
				),
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

		verify(imageService).getOriginalImageByImageId(USER_ID, IMAGE_ID);
		verify(tagService).getTagsByImageId(IMAGE_ID);
	}

	@Test
	@DisplayName("이미지에 태그 추가 성공")
	void addTagToImage_Success() throws Exception {
		// given
		Long tagId = 1L;
		doNothing().when(tagService).addTagToImage(IMAGE_ID, tagId);

		// when & then
		mockMvc.perform(post("/api/images/{imageId}/tags/{tagId}", IMAGE_ID, tagId)
				.with(user(userDetails)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value(200))
			.andDo(document("image-add-tag",
				pathParameters(
					parameterWithName("imageId").description("이미지 ID"),
					parameterWithName("tagId").description("태그 ID")
				),
				relaxedResponseFields(
					fieldWithPath("code").description("응답 코드"),
					fieldWithPath("message").description("응답 메시지")
				)
			));

		verify(imageService).getOriginalImageByImageId(USER_ID, IMAGE_ID);
		verify(tagService).addTagToImage(IMAGE_ID, tagId);
	}

	@Test
	@DisplayName("이미지에서 태그 제거 성공")
	void removeTagFromImage_Success() throws Exception {
		// given
		Long tagId = 1L;
		doNothing().when(tagService).removeTagFromImage(IMAGE_ID, tagId);

		// when & then
		mockMvc.perform(delete("/api/images/{imageId}/tags/{tagId}", IMAGE_ID, tagId)
				.with(user(userDetails)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value(200))
			.andDo(document("image-remove-tag",
				pathParameters(
					parameterWithName("imageId").description("이미지 ID"),
					parameterWithName("tagId").description("태그 ID")
				),
				relaxedResponseFields(
					fieldWithPath("code").description("응답 코드"),
					fieldWithPath("message").description("응답 메시지")
				)
			));

		verify(imageService).getOriginalImageByImageId(USER_ID, IMAGE_ID);
		verify(tagService).removeTagFromImage(IMAGE_ID, tagId);
	}

	@Test
	@DisplayName("이미지 태그 수정 성공")
	void updateImageTag_Success() throws Exception {
		// given
		Long tagId = 1L;
		TagUpdateRequestDto updateRequest = new TagUpdateRequestDto("수정된태그");
		TagResponseDto updatedTag = TagResponseDto.builder()
			.tagId(2L)
			.tagName("수정된태그")
			.tagType(TagType.CUSTOM)
			.tagUseCount(1L)
			.createdAt(LocalDateTime.now())
			.build();

		when(tagService.updateImageTag(IMAGE_ID, tagId, "수정된태그")).thenReturn(updatedTag);

		// when & then
		ObjectMapper objectMapper = new ObjectMapper();
		String requestJson = objectMapper.writeValueAsString(updateRequest);

		mockMvc.perform(put("/api/images/{imageId}/tags/{tagId}", IMAGE_ID, tagId)
				.with(user(userDetails))
				.contentType(APPLICATION_JSON)
				.content(requestJson))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value(200))
			.andExpect(jsonPath("$.data.tagId").value(2L))
			.andExpect(jsonPath("$.data.tagName").value("수정된태그"))
			.andDo(document("image-update-tag",
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
					fieldWithPath("data.tagType").description("태그 타입(CUSTOM, SIDO, SIGUNGU 등)"),
					fieldWithPath("data.tagUseCount").description("태그 사용 횟수"),
					fieldWithPath("data.createdAt").description("태그 생성 시간")
				)
			));

		verify(imageService).getOriginalImageByImageId(USER_ID, IMAGE_ID);
		verify(tagService).updateImageTag(IMAGE_ID, tagId, "수정된태그");
	}

	@Test
	@DisplayName("시도별 이미지 커버리지 조회 성공")
	void getImageSidoCoverageSuccess() throws Exception {
		// Given
		List<Long> partialSidoIds = List.of(11L, 26L, 31L);
		List<Long> completeSidoIds = List.of(41L, 28L);
		ImageSidoCoverageResponseDto coverageData = new ImageSidoCoverageResponseDto(partialSidoIds, completeSidoIds);
		
		when(imageService.getImageSidoCoverageData(USER_ID)).thenReturn(coverageData);

		// When
		ResultActions result = mockMvc.perform(get("/api/images/me/coverage/sido")
			.with(user(userDetails)));

		// Then
		result
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value(200))
			.andExpect(jsonPath("$.message").value("Ok"))
			.andExpect(jsonPath("$.data.partiallyCoveredSidoIds").isArray())
			.andExpect(jsonPath("$.data.partiallyCoveredSidoIds.length()").value(3))
			.andExpect(jsonPath("$.data.completelyCoveredSidoIds").isArray())
			.andExpect(jsonPath("$.data.completelyCoveredSidoIds.length()").value(2))
			.andDo(document("get-image-sido-coverage-success",
				responseFields(
					fieldWithPath("code").description("응답 코드"),
					fieldWithPath("message").description("응답 메시지"),
					fieldWithPath("data").description("시도별 이미지 커버리지 데이터"),
					fieldWithPath("data.partiallyCoveredSidoIds").description("일부 시군구에만 이미지가 있는 시도 ID 배열"),
					fieldWithPath("data.partiallyCoveredSidoIds[]").description("PARTIAL 시도 ID"),
					fieldWithPath("data.completelyCoveredSidoIds").description("모든 시군구에 이미지가 있는 시도 ID 배열"),
					fieldWithPath("data.completelyCoveredSidoIds[]").description("COMPLETE 시도 ID")
				)
			));

		verify(imageService, times(1)).getImageSidoCoverageData(USER_ID);
	}
}
