package com.trackery.trackerybackapiserver.domain.location.controller;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import com.trackery.trackerybackapiserver.domain.config.CommonMockMvcControllerTestSetUp;
import com.trackery.trackerybackapiserver.domain.location.dto.CoordinateDto;
import com.trackery.trackerybackapiserver.domain.location.service.LocationService;
import com.trackery.trackerybackapiserver.domain.user.entity.CustomUserDetails;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.location.controller
 * fileName       : LocationControllerTest
 * author         : durururuk
 * date           : 25. 4. 23.
 * description    : 
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 23.		durururuk		최초 생성
 */
@WebMvcTest(LocationController.class)
public class LocationControllerTest extends CommonMockMvcControllerTestSetUp {
	@MockitoBean
	LocationService locationService;

	CustomUserDetails customUserDetails;

	@BeforeEach
	void setUp() {
		customUserDetails = CustomUserDetails.builder().userId(1L).userName("abcdefg").roleId(1L).build();
	}

	@Nested
	@DisplayName("좌표 -> 주소명 api 테스트")
	class getLocationNameTest {
		@Test
		void success() throws Exception {
			CoordinateDto coordinateDto = new CoordinateDto(31.31, 123.123);

			when(locationService.getLocationNameByCoord(coordinateDto)).thenReturn("서울특별시 동작구");

			ResultActions result = mockMvc.perform(post("/api/location/name")
				.with(user(customUserDetails))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(coordinateDto)));

			result
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data").value("서울특별시 동작구"));
		}
	}

}
