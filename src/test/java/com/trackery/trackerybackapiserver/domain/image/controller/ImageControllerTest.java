package com.trackery.trackerybackapiserver.domain.image.controller;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
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
			.andExpect(jsonPath("$.data.imageUrl").value(IMAGE_URL));

		verify(imageService, times(1)).getImageByImageId(IMAGE_ID);
	}

	@Test
	@DisplayName("인증된 사용자의 이미지 목록 조회 성공")
	void getMyImagesSuccess() throws Exception {
		// given
		List<ImageDto> imageDtoList = List.of(imageDto);
		when(imageService.getImageListByUserId(userDetails.getUserId())).thenReturn(imageDtoList);

		// when
		ResultActions result = mockMvc.perform(get("/api/images/me")
			.with(user(userDetails)));

		// then
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
			.andExpect(jsonPath("$.data[0].imageUrl").value(imageDto.getImageUrl()));

		verify(imageService, times(1)).getImageListByUserId(userDetails.getUserId());
	}
}