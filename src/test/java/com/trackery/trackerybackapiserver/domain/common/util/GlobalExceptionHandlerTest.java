package com.trackery.trackerybackapiserver.domain.common.util;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.ResultActions;

import com.trackery.trackerybackapiserver.domain.CommonMockMvcControllerTestSetUp;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.user.controller.UserController;
import com.trackery.trackerybackapiserver.domain.user.dto.UserRegisterDto;
import com.trackery.trackerybackapiserver.domain.user.service.UserService;

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
class GlobalExceptionHandlerTest extends CommonMockMvcControllerTestSetUp {
	@MockitoBean
	private UserController userController;

	@Mock
	private UserService userService;

	@Spy
	UserRegisterDto userRegisterDto;

	@Test
	@WithMockUser
	void ApiException_처리_테스트() throws Exception {
		when(userController.checkUsernameAvailability(anyString()))
			.thenThrow(new ApiException(ErrorCode.BAD_GATEWAY));

		ResultActions result = mockMvc
			.perform(get("/api/users/exists/username")
				.queryParam("value", "abcdefg"));

		result
			.andExpect(status().isBadGateway())
			.andExpect(jsonPath("$.message").value(ErrorCode.BAD_GATEWAY.getMessage()));

	}

	@Test
	@WithMockUser
	void ValidationException_처리_테스트() throws Exception {
		ReflectionTestUtils.setField(userRegisterDto, "email", "a@a.com");
		ReflectionTestUtils.setField(userRegisterDto, "userName", "abcdefg");
		ReflectionTestUtils.setField(userRegisterDto, "nickname", "");
		ReflectionTestUtils.setField(userRegisterDto, "password", "Qwerasdf1234!!asdf");

		doNothing().when(userService).registerUser(any());

		ResultActions result = mockMvc
			.perform(post("/api/users/register")
				.with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(userRegisterDto)));

		result
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value(ErrorCode.BAD_REQUEST_VALIDATION_FAILED.getMessage()));
	}

	@Test
	@WithMockUser
	void HttpMessageNotReadableException_처리_테스트() throws Exception {
		doNothing().when(userService).registerUser(any());
		ResultActions result = mockMvc
			.perform(post("/api/users/register")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON));

		result
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value(ErrorCode.BAD_REQUEST_INVALID_REQUEST_BODY.getMessage()));
	}
}