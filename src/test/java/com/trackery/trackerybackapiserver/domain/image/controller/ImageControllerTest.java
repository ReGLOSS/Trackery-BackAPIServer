package com.trackery.trackerybackapiserver.domain.image.controller;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import com.github.pagehelper.PageInfo;
import com.trackery.trackerybackapiserver.domain.config.CommonMockMvcControllerTestSetUp;
import com.trackery.trackerybackapiserver.domain.image.dto.ImageDto;
import com.trackery.trackerybackapiserver.domain.image.service.ImageService;
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
 * 25. 6. 18.		inari		    Spring-Rest-Docs api문서 추가
 */
@WebMvcTest(ImageController.class)
class ImageControllerTest extends CommonMockMvcControllerTestSetUp {
	@MockitoBean
	private ImageService imageService;

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
		when(imageService.getImageByImageId(IMAGE_ID)).thenReturn(imageDto);

		ResultActions result = mockMvc.perform(get("/api/images")
			.with(user(userDetails)) // 인증된 사용자 정보 추가 (필요하다면)
			.queryParam("imageId", String.valueOf(IMAGE_ID)));

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
				queryParameters(
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
					fieldWithPath("data.imageUrl").description("이미지 URL")
				)
			));

		verify(imageService, times(1)).getImageByImageId(IMAGE_ID);
	}

	@Test
	@DisplayName("인증된 사용자의 이미지 목록 조회 성공")
	void getMyImagesSuccess() throws Exception {
		List<ImageDto> imageDtoList = List.of(imageDto);
		when(imageService.getImageListByUserId(userDetails.getUserId())).thenReturn(imageDtoList);

		ResultActions result = mockMvc.perform(get("/api/images/me")
			.with(user(userDetails)));

		result
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value(200))
			.andExpect(jsonPath("$.message").value("Ok"))
			.andExpect(jsonPath("$.data").isArray())
			.andExpect(jsonPath("$.data.length()").value(imageDtoList.size()))
			.andExpect(jsonPath("$.data[0].imageId").value(imageDto.getImageId()))
			.andExpect(jsonPath("$.data[0].userId").value(imageDto.getUserId()))
			.andExpect(jsonPath("$.data[0].sdName").value(imageDto.getSdName()))
			.andExpect(jsonPath("$.data[0].sggName").value(imageDto.getSggName()))
			.andExpect(jsonPath("$.data[0].latitude").value(imageDto.getLatitude()))
			.andExpect(jsonPath("$.data[0].longitude").value(imageDto.getLongitude()))
			.andExpect(jsonPath("$.data[0].imageName").value(imageDto.getImageName()))
			.andExpect(jsonPath("$.data[0].imageContent").value(imageDto.getImageContent()))
			.andExpect(jsonPath("$.data[0].imageDate").value(imageDto.getImageDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
			.andExpect(jsonPath("$.data[0].imageUrl").value(imageDto.getImageUrl()))
			.andDo(document("get-my-images-success",
				responseFields(
					fieldWithPath("code").description("상태 코드"),
					fieldWithPath("message").description("응답 메시지"),
					fieldWithPath("data[]").description("이미지 목록"),
					fieldWithPath("data[].imageId").description("이미지 ID"),
					fieldWithPath("data[].userId").description("이미지 업로드 사용자 ID"),
					fieldWithPath("data[].imageRegDate").description("이미지 등록 일시"),
					fieldWithPath("data[].sdName").description("시도명"),
					fieldWithPath("data[].sggName").description("시군구명"),
					fieldWithPath("data[].latitude").description("위도"),
					fieldWithPath("data[].longitude").description("경도"),
					fieldWithPath("data[].imageName").description("이미지 파일명"),
					fieldWithPath("data[].imageContent").description("이미지 설명"),
					fieldWithPath("data[].imageDate").description("이미지 촬영 일시"),
					fieldWithPath("data[].isPublic").description("공개 여부 (true: 공개, false: 비공개, null: 미설정)"),
					fieldWithPath("data[].imageUrl").description("이미지 URL")
				)
			));

		verify(imageService, times(1)).getImageListByUserId(userDetails.getUserId());
	}

	@Test
	@DisplayName("인증된 사용자의 이미지 목록 조회 페이지네이션 성공")
	void getMyImagesV2Success() throws Exception {
		List<ImageDto> imageDtoList = List.of(imageDto);
		PageInfo<ImageDto> pageInfo = new PageInfo<>(imageDtoList);
		when(imageService.getImageListByUserIdV2(USER_ID, 1, 10)).thenReturn(pageInfo);

		ResultActions result = mockMvc.perform(get("/api/images/v2/me")
				.with(user(userDetails))
				.queryParam("pageNum", "1")
				.queryParam("pageSize", "10")
			);

		result
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value(200))
			.andExpect(jsonPath("$.message").value("Ok"))
			.andDo(document("get-my-images-v2",
				queryParameters(
					parameterWithName("pageNum").description("페이지 번호 (1부터 시작, 기본값 : 1)"),
					parameterWithName("pageSize").description("페이지 크기 (기본값 : 10)")
				),

				responseFields(
					fieldWithPath("code").description("응답 코드"),
					fieldWithPath("message").description("응답 메시지"),
					fieldWithPath("data").description("페이지네이션된 이미지 데이터"),
					fieldWithPath("data.total").description("전체 이미지 개수"),
					fieldWithPath("data.list").description("이미지 목록"),
					fieldWithPath("data.list[].imageId").description("이미지 ID"),
					fieldWithPath("data.list[].userId").description("사용자 ID"),
					fieldWithPath("data.list[].imageRegDate").description("이미지 등록일시"),
					fieldWithPath("data.list[].sdName").description("시도명"),
					fieldWithPath("data.list[].sggName").description("시군구명"),
					fieldWithPath("data.list[].latitude").description("위도"),
					fieldWithPath("data.list[].longitude").description("경도"),
					fieldWithPath("data.list[].imageName").description("이미지 파일명"),
					fieldWithPath("data.list[].imageContent").description("이미지 설명"),
					fieldWithPath("data.list[].imageDate").description("이미지 촬영일시"),
					fieldWithPath("data.list[].isPublic").description("공개 여부").optional(),
					fieldWithPath("data.list[].imageUrl").description("이미지 URL"),
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

		verify(imageService, times(1)).getImageListByUserIdV2(USER_ID, 1, 10);
	}
}