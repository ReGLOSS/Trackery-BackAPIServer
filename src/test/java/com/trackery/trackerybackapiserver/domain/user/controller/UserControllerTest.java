package com.trackery.trackerybackapiserver.domain.user.controller;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.config.CommonMockMvcControllerTestSetUp;
import com.trackery.trackerybackapiserver.domain.jwt.dto.AuthTokenDto;
import com.trackery.trackerybackapiserver.domain.user.dto.update.UpdatePasswordDto;
import com.trackery.trackerybackapiserver.domain.user.dto.DetailedUserInfoDto;
import com.trackery.trackerybackapiserver.domain.user.dto.UserLoginDto;
import com.trackery.trackerybackapiserver.domain.user.dto.UserNameAvailabilityResponseDto;
import com.trackery.trackerybackapiserver.domain.user.dto.UserRegisterDto;
import com.trackery.trackerybackapiserver.domain.user.entity.CustomUserDetails;
import com.trackery.trackerybackapiserver.domain.user.entity.OAuth;
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
 25. 4. 09.		   durururuk	   유저 상세정보 조회 API 단위테스트 코드 작성
 25. 4. 10.		   durururuk	   인증 기반 비밀번호 변경 컨트롤러 mockMvc 테스트 작성
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

	@Nested
	@DisplayName("유저 상세 정보 조회 테스트")
	class getDetailedUserInfoTest {
		private OAuth oAuth;

		@BeforeEach
		void setUp() {
			oAuth = OAuth.builder()
				.userId(1L)
				.providerUserId("155788848")
				.provider("KAKAO")
				.build();
			ReflectionTestUtils.setField(oAuth, "oauthId", 1L);
		}

		@Test
		@DisplayName("성공")
		void success() throws Exception {
			CustomUserDetails customUserDetails = CustomUserDetails.builder()
				.userId(1L).roleId(1L).build();

			DetailedUserInfoDto dto = new DetailedUserInfoDto(1L, 1L,
				"abcdefg", "김커피", "a@a.com", List.of(oAuth));

			when(userService.getDetailedUserInfoByUserId(1L)).thenReturn(dto);

			ResultActions result = mockMvc.perform(get("/api/users/details")
				.with(user(customUserDetails)));

			result
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Ok"))
				.andExpect(jsonPath("$.data.userId").value(1))
				.andExpect(jsonPath("$.data.userRoleId").value(1))
				.andExpect(jsonPath("$.data.userName").value("abcdefg"))
				.andExpect(jsonPath("$.data.nickname").value("김커피"))
				.andExpect(jsonPath("$.data.email").value("a@a.com"))
				.andExpect(jsonPath("$.data.OAuthList[0].oauthId").value(1))
				.andExpect(jsonPath("$.data.OAuthList[0].userId").value(1))
				.andExpect(jsonPath("$.data.OAuthList[0].provider").value("KAKAO"))
				.andExpect(jsonPath("$.data.OAuthList[0].providerUserId").value("155788848"));
		}
	}

	@Nested
	@DisplayName("인증 기반 비밀번호 변경 API 테스트")
	class patchPasswordByAuthenticationTest {
		private CustomUserDetails customUserDetails;
		private final UpdatePasswordDto changePasswordDto = new UpdatePasswordDto();

		@BeforeEach
		void setUp() {
			customUserDetails = CustomUserDetails.builder().userId(1L).userName("abcdefg").roleId(1L).build();
		}

		@Test
		@DisplayName("성공")
		void success() throws Exception {
			ReflectionTestUtils.setField(changePasswordDto, "newPassword", "Qwerasdf1234!!!!!!!!");

			doNothing().when(userService).updatePasswordByAuthentication(eq(1L), any(UpdatePasswordDto.class));

			ResultActions result = mockMvc.perform(patch("/api/users/me/password")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(changePasswordDto))
				.with(user(customUserDetails))
				.with(csrf()));

			result.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Ok"));
		}

		@Test
		@DisplayName("실패 - 비밀번호 규격과 맞지 않는 경우")
		void failure_1() throws Exception {
			ReflectionTestUtils.setField(changePasswordDto, "newPassword", "short");

			ResultActions result = mockMvc.perform(patch("/api/users/me/password")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(changePasswordDto))
				.with(user(customUserDetails))
				.with(csrf()));

			result.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Validation 실패"))
				.andExpect(jsonPath("$.data").value("비밀번호는 최소 16자리이며, 대문자, 소문자, 숫자, 밑줄(_)을 제외한 특수문자를 포함해야 합니다."));
		}

		@Test
		@DisplayName("실패 - 기존 비밀번호가 DB에 저장된 유저의 비밀번호와 다른 경우")
		void failure_2() throws Exception {
			ReflectionTestUtils.setField(changePasswordDto, "newPassword", "Qwerasdf1234!!!!!");

			doThrow(new ApiException(ErrorCode.BAD_REQUEST_INVALID_PASSWORD))
				.when(userService).updatePasswordByAuthentication(eq(1L), any(UpdatePasswordDto.class));

			ResultActions result = mockMvc.perform(patch("/api/users/me/password")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(changePasswordDto))
				.with(user(customUserDetails))
				.with(csrf()));

			result.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value(ErrorCode.BAD_REQUEST_INVALID_PASSWORD.getMessage()));
		}
	}
}