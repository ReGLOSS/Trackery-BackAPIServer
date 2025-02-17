package com.trackery.trackerybackapiserver.domain.common.util;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackery.trackerybackapiserver.domain.testFeature.TestControllerConfig;
import com.trackery.trackerybackapiserver.domain.testFeature.ValidationTestDto;

/**
 *packageName    : com.trackery.trackerybackapiserver.domain.common.util

 fileName       : GlobalExceptionHandlerTest
 author         : durururuk
 date           : 25. 2. 15.
 description    : GlobalExceptionHandler 테스트
 ===========================================================
 DATE              AUTHOR             NOTE
 -----------------------------------------------------------
 25. 2. 15.        durururuk       최초 생성*/
@WebMvcTest(controllers = {TestControllerConfig.Controller.class})
@Import(TestControllerConfig.class)
class GlobalExceptionHandlerTest {

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	@WithMockUser
	void ApiException_처리_테스트() throws Exception {
		ResultActions result = mockMvc.perform(get("/test/api-exception"));

		result
			.andExpect(status().isBadRequest())
			.andDo(print());
	}

	@Test
	@WithMockUser
	void handleValidationException_처리_테스트() throws Exception {
		ValidationTestDto dto = new ValidationTestDto();
		dto.setString("");

		ResultActions result = mockMvc.perform(post("/test/validation-exception")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(dto))
			.with(csrf()));

		result
			.andExpect(status().isBadRequest())
			.andDo(print());
	}

	@Test
	@WithMockUser
	void handleHttpMessageNotReadableException_처리_테스트() throws Exception {
		ResultActions result = mockMvc.perform(post("/test/http-message-not-readable-exception")
			.with(csrf()));

		result
			.andExpect(status().isBadRequest())
			.andDo(print());
	}
}