package com.trackery.trackerybackapiserver.domain.user.controller;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.trackery.trackerybackapiserver.domain.CommonMockMvcControllerTestSetUp;
import com.trackery.trackerybackapiserver.domain.user.dto.UserRegisterDto;
import com.trackery.trackerybackapiserver.domain.user.service.UserService;

/**
 *packageName    : com.trackery.trackerybackapiserver.domain.user.controller

 fileName       : UserControllerTest
 author         : durururuk
 date           : 25. 2. 14.
 description    : UserController 테스트코드
 ===========================================================
 DATE              AUTHOR             NOTE
 -----------------------------------------------------------
 25. 2. 14.        durururuk       최초 생성
 */

@WithMockUser
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest extends CommonMockMvcControllerTestSetUp {
	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@Spy
	private UserRegisterDto dto;

	@Test
	void 회원가입_성공() throws Exception {
		ReflectionTestUtils.setField(dto, "email", "a@a.com");
		ReflectionTestUtils.setField(dto, "userName", "abcdefg");
		ReflectionTestUtils.setField(dto, "nickname", "김커피");
		ReflectionTestUtils.setField(dto, "password", "Qwerasdf1234!!asdf");

		when(userService.registerUser(any())).thenReturn("Bearer jwt");

		ResultActions result = mockMvc
			.perform(post("/api/users/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto))
				.with(csrf())
			);

		result
			.andExpect(status().isCreated())
			.andExpect(header().exists("Authorization"))
			.andExpect(header().string("Authorization", "Bearer jwt"));
	}

	@Test
	void 유저명_중복체크_성공() throws Exception {
		when(userService.checkUsernameAvailability(anyString())).thenReturn(true);

		ResultActions result = mockMvc
			.perform(get("/api/users/exists/username")
				.queryParam("value", "abcdefg"));

		result.andExpect(status().isOk())
			.andExpect(jsonPath("$.data").value(true));
	}
}