package com.trackery.trackerybackapiserver.domain.user.controller;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.trackery.trackerybackapiserver.domain.config.CommonMockMvcControllerTestSetUp;
import com.trackery.trackerybackapiserver.domain.jwt.dto.AuthTokenDto;
import com.trackery.trackerybackapiserver.domain.user.dto.UserLoginDto;
import com.trackery.trackerybackapiserver.domain.user.dto.UserNameAvailabilityResponseDto;
import com.trackery.trackerybackapiserver.domain.user.dto.UserRegisterDto;
import com.trackery.trackerybackapiserver.domain.user.service.UserService;

import jakarta.servlet.http.Cookie;

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
 25. 2. 14.        durururuk       로그인 컨트롤러 테스트 코드 작성
 */

@WebMvcTest(UserController.class)
class UserControllerTest extends CommonMockMvcControllerTestSetUp {
	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@Spy
	private UserRegisterDto registerDto;

	@Spy
	private UserLoginDto loginDto;

	@Test
	void 회원가입_성공() throws Exception {
		ReflectionTestUtils.setField(registerDto, "nickname", "김커피");
		ReflectionTestUtils.setField(registerDto, "password", "Qwerasdf1234!!asdf");

		String accessToken = "accessToken";
		String refreshToken = "refreshToken";
		AuthTokenDto authTokenDto = new AuthTokenDto(accessToken, refreshToken);
		String emailToken = "emailJwt";
		String userNameToken = "userNameJwt";

		when(userService.registerUser(eq(emailToken), eq(userNameToken), any(UserRegisterDto.class))).thenReturn(
			authTokenDto);

		ResultActions result = mockMvc
			.perform(post("/api/users/register")
				.contentType(MediaType.APPLICATION_JSON)
				.cookie(new Cookie("emailToken", emailToken))
				.cookie(new Cookie("userNameToken", userNameToken))
				.content(objectMapper.writeValueAsString(registerDto))
				.with(csrf())
			);

		result
			.andExpect(status().isCreated())
			.andExpect(cookie().exists("accessToken"))
			.andExpect(cookie().httpOnly("accessToken", true))
			.andExpect(cookie().value("accessToken", accessToken))
			.andExpect(cookie().exists("refreshToken"))
			.andExpect(cookie().httpOnly("refreshToken", true))
			.andExpect(cookie().value("refreshToken", refreshToken));
	}

	@Test
	void 유저명_중복체크_성공() throws Exception {
		UserNameAvailabilityResponseDto dto = new UserNameAvailabilityResponseDto(true, "jwt");
		when(userService.checkUsernameAvailability(anyString())).thenReturn(dto);

		ResultActions result = mockMvc
			.perform(get("/api/users/exists/username")
				.queryParam("value", "abcdefg"));

		result.andExpect(status().isOk())
			.andExpect(cookie().exists("userNameToken"))
			.andExpect(cookie().value("userNameToken", "jwt"))
			.andExpect(jsonPath("$.data").value(true));
	}

	@Test
	void 로그인_테스트() throws Exception {
		ReflectionTestUtils.setField(loginDto, "userName", "abcdefg");
		ReflectionTestUtils.setField(loginDto, "password", "Qwerasdf1234!");

		String accessToken = "accessToken";
		String refreshToken = "refreshToken";
		AuthTokenDto authTokenDto = new AuthTokenDto(accessToken, refreshToken);

		when(userService.login(any())).thenReturn(authTokenDto);

		ResultActions result = mockMvc
			.perform(post("/api/users/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginDto))
				.with(csrf()));

		result
			.andExpect(status().isOk())
			.andExpect(cookie().exists("accessToken"))
			.andExpect(cookie().httpOnly("accessToken", true))
			.andExpect(cookie().value("accessToken", accessToken))
			.andExpect(cookie().exists("refreshToken"))
			.andExpect(cookie().httpOnly("refreshToken", true))
			.andExpect(cookie().value("refreshToken", refreshToken));
	}
}