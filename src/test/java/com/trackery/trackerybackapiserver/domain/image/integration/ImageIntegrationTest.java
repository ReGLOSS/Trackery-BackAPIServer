package com.trackery.trackerybackapiserver.domain.image.integration;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.trackery.trackerybackapiserver.domain.aws.service.SqsMessageConsumer;
import com.trackery.trackerybackapiserver.domain.image.dto.ImageSigunguCoverageResponseDto;
import com.trackery.trackerybackapiserver.domain.image.service.ImageService;
import com.trackery.trackerybackapiserver.domain.user.entity.CustomUserDetails;

import lombok.extern.slf4j.Slf4j;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.image.integration
 * fileName       : ImageIntegrationTest
 * author         : durururuk
 * date           : 25. 9. 7.
 * description    : 통합 테스트 기본 템플릿
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 7.      durururuk       최초 생성
 */
@Slf4j
@SpringBootTest(properties = "spring.profiles.active=dev")
@AutoConfigureMockMvc(addFilters = false)
class ImageIntegrationTest {
	@Autowired
	private ImageService imageService;
	@Autowired
	MockMvc mockMvc;

	@MockitoBean
	private SqsMessageConsumer sqsMessageConsumer;

	@Test
	void sggCoverageLoad() {
		log.info("ImageIntegrationTest sggCoverageLoad");

		ImageSigunguCoverageResponseDto result = imageService.getImageSigunguCoverageData(2L, 31L);

		log.info("result : {}", result);
		log.info("result set Size : {}", result.getHavingImagesSigunguIdList().size());
		log.info("result set : {}", result.getHavingImagesSigunguIdList());
	}

	@Test
	void sggCoverageApiTest() throws Exception {
		log.info("ImageIntegrationTest sggCoverageApiTest");
		CustomUserDetails userDetails = CustomUserDetails.builder().userId(2L).roleId(1L).build();
		String validJwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL3RyYWNrZXJ5LmJva2t1cmluLmNvbSIsInN1YiI6IjIiLCJ1c2VybmFtZSI6ImR1cnVydXJ1ayIsInJvbGUiOjEsIm5iZiI6MTc1NzM4NzM4NCwiaWF0IjoxNzU3Mzg3Mzg0LCJleHAiOjE3NTczOTA5ODQsImp0aSI6IjA4OWVmMWJmLWJhMzQtNDgzMS1hNzQ4LTViYTc4MTk0NmYzNiJ9.gFgqwF9-snh8lctMKIiS2GjdTg3p9X1LAx-Y-bCVgeQ";
		mockMvc.perform(
				get("/api/images/me/coverage/sido/31/sigungu").header("Authorization", "Bearer " + validJwtToken).with(user(userDetails)))
			.andDo(print());
	}
}