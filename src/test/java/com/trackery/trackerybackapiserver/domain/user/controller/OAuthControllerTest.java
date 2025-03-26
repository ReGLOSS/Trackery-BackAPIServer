package com.trackery.trackerybackapiserver.domain.user.controller;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import com.trackery.trackerybackapiserver.domain.CommonMockMvcControllerTestSetUp;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.user.dto.OAuthLinkRequestDto;
import com.trackery.trackerybackapiserver.domain.user.dto.OAuthLoginDto;
import com.trackery.trackerybackapiserver.domain.user.dto.OAuthResponseDto;
import com.trackery.trackerybackapiserver.domain.user.service.OAuthLinkService;
import com.trackery.trackerybackapiserver.domain.user.service.OAuthService;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.controller
 * fileName       : OAuthControllerTest
 * author         : inari
 * date           : 25. 3. 3.
 * description    : 간편 로그인 관련 HTTP 요청을 처리하는 컨트롤러의 테스트 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 3.        inari       최초 생성
 * 25. 3. 26.       inari       계정 연동 토큰 테스트 추가
 */
@WithMockUser
@WebMvcTest(OAuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class OAuthControllerTest extends CommonMockMvcControllerTestSetUp {

	@MockitoBean
	private OAuthService oAuthService;

	@MockitoBean
	private OAuthLinkService oAuthLinkService;

	@ParameterizedTest
	@ValueSource(strings = {"KAKAO", "GOOGLE", "GITHUB"})
	void OAuth_로그인_성공(String provider) throws Exception {
		// given
		final String AUTH_CODE = "auth_code";
		final String API_PATH = "/api/users/oauth/login/";
		final String JWT = "jwt";

		OAuthLoginDto oAuthLoginDto = OAuthLoginDto.builder()
			.provider(provider)
			.code(AUTH_CODE)
			.linkAccount(false)
			.build();

		OAuthResponseDto responseDto = OAuthResponseDto.builder()
			.isExistingEmail(false)
			.build();

		OAuthService.OAuthLoginResult result = new OAuthService.OAuthLoginResult(responseDto, JWT);

		when(oAuthService.processOAuthLogin(any(OAuthLoginDto.class))).thenReturn(result);

		// when
		ResultActions resultActions = mockMvc
			.perform(get(API_PATH + provider.toLowerCase())
				.queryParam("code", AUTH_CODE)
				.contentType(MediaType.APPLICATION_JSON));

		// then
		resultActions
			.andExpect(cookie().value("accessToken", JWT))
			.andExpect(jsonPath("$.data.existingEmail").value(false));
	}

	@Test
	void 네이버_로그인_성공() throws Exception {
		// given
		OAuthLoginDto oAuthLoginDto = OAuthLoginDto.builder()
			.provider("NAVER")
			.code("auth_code")
			.linkAccount(false)
			.build();

		OAuthResponseDto responseDto = OAuthResponseDto.builder()
			.isExistingEmail(false)
			.build();

		OAuthService.OAuthLoginResult result = new OAuthService.OAuthLoginResult(responseDto, "jwt");

		when(oAuthService.processOAuthLogin(any(OAuthLoginDto.class))).thenReturn(result);

		// when
		ResultActions resultActions = mockMvc
			.perform(get("/api/users/oauth/login/naver")
				.queryParam("code", "auth_code")
				.queryParam("state", "state")
				.contentType(MediaType.APPLICATION_JSON));

		// then
		resultActions
			.andExpect(status().isOk())
			.andExpect(cookie().exists("accessToken"))
			.andExpect(cookie().httpOnly("accessToken", true))
			.andExpect(cookie().value("accessToken", "jwt"))
			.andExpect(jsonPath("$.data.existingEmail").value(false));
	}

	@Test
	void 기존_이메일_존재시_연동_미수행_테스트() throws Exception {
		// given
		OAuthResponseDto responseDto = OAuthResponseDto.builder()
			.isExistingEmail(true)
			.build();

		OAuthService.OAuthLoginResult result = new OAuthService.OAuthLoginResult(responseDto, null);

		when(oAuthService.processOAuthLogin(any(OAuthLoginDto.class))).thenReturn(result);

		// when
		ResultActions resultActions = mockMvc
			.perform(get("/api/users/oauth/login/kakao")
				.queryParam("code", "auth_code")
				.queryParam("link_account", "false")
				.contentType(MediaType.APPLICATION_JSON));

		// then
		resultActions
			.andExpect(status().isOk())
			.andExpect(cookie().doesNotExist("accessToken"))
			.andExpect(jsonPath("$.data.existingEmail").value(true));
	}

	@Test
	@DisplayName("링크 토큰을 통한 계정 연동 성공")
	void 링크_토큰을_통한_계정_연동_성공() throws Exception {
		// given
		final String AUTH_CODE = "auth_code";
		final String LINK_TOKEN = "valid-link-token";
		final String PROVIDER = "KAKAO";
		final String EMAIL = "test@example.com";
		final String JWT = "jwt_token";

		OAuthLinkRequestDto linkRequest = OAuthLinkRequestDto.builder()
			.provider(PROVIDER)
			.email(EMAIL)
			.linkAccount(true)
			.build();

		OAuthResponseDto responseDto = OAuthResponseDto.builder()
			.isExistingEmail(false)
			.build();

		OAuthService.OAuthLoginResult result = new OAuthService.OAuthLoginResult(responseDto, JWT);

		when(oAuthLinkService.validateToken(LINK_TOKEN)).thenReturn(linkRequest);
		when(oAuthService.processOAuthLogin(any(OAuthLoginDto.class))).thenReturn(result);

		// when
		ResultActions resultActions = mockMvc
			.perform(get("/api/users/oauth/login/kakao")
				.queryParam("code", AUTH_CODE)
				.queryParam("link_token", LINK_TOKEN)
				.contentType(MediaType.APPLICATION_JSON));

		// then
		resultActions
			.andExpect(status().isOk())
			.andExpect(cookie().exists("accessToken"))
			.andExpect(cookie().httpOnly("accessToken", true))
			.andExpect(cookie().value("accessToken", JWT))
			.andExpect(jsonPath("$.data.existingEmail").value(false));

		verify(oAuthLinkService).validateToken(LINK_TOKEN);
		verify(oAuthLinkService).deleteToken(LINK_TOKEN);
	}

	@Test
	@DisplayName("계정 연동 토큰 생성 성공")
	void 계정_연동_토큰_생성_성공() throws Exception {
		// given
		final String PROVIDER = "KAKAO";
		final String EMAIL = "test@example.com";
		final String TOKEN = "generated-token";

		OAuthLinkRequestDto requestDto = OAuthLinkRequestDto.builder()
			.provider(PROVIDER)
			.email(EMAIL)
			.build();

		when(oAuthLinkService.createLinkToken(PROVIDER, EMAIL)).thenReturn(TOKEN);

		// when
		ResultActions resultActions = mockMvc
			.perform(post("/api/users/oauth/link-account")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDto)));

		// then
		resultActions
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.token").value(TOKEN))
			.andExpect(jsonPath("$.data.provider").value(PROVIDER));

		verify(oAuthLinkService).createLinkToken(PROVIDER, EMAIL);
	}

	@Test
	@DisplayName("계정 연동 토큰 생성 실패 - 서버 오류")
	void 계정_연동_토큰_생성_실패_서버_오류() throws Exception {
		// given
		final String PROVIDER = "KAKAO";
		final String EMAIL = "test@example.com";

		OAuthLinkRequestDto requestDto = OAuthLinkRequestDto.builder()
			.provider(PROVIDER)
			.email(EMAIL)
			.build();

		when(oAuthLinkService.createLinkToken(PROVIDER, EMAIL))
			.thenThrow(new RuntimeException("서버 내부 오류"));

		// when
		ResultActions resultActions = mockMvc
			.perform(post("/api/users/oauth/link-account")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDto)));

		// then
		resultActions
			.andExpect(status().isInternalServerError());
	}

	@Test
	@DisplayName("계정 연동 토큰 생성 실패 - API 예외")
	void 계정_연동_토큰_생성_실패_API_예외() throws Exception {
		// given
		final String PROVIDER = "INVALID_PROVIDER";
		final String EMAIL = "test@example.com";

		OAuthLinkRequestDto requestDto = OAuthLinkRequestDto.builder()
			.provider(PROVIDER)
			.email(EMAIL)
			.build();

		when(oAuthLinkService.createLinkToken(PROVIDER, EMAIL))
			.thenThrow(new ApiException(ErrorCode.BAD_REQUEST));

		// when
		ResultActions resultActions = mockMvc
			.perform(post("/api/users/oauth/link-account")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDto)));

		// then
		resultActions
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("링크 토큰 검증 실패시 일반 로그인 처리")
	void 링크_토큰_검증_실패시_일반_로그인_처리() throws Exception {
		// given
		final String AUTH_CODE = "auth_code";
		final String INVALID_LINK_TOKEN = "invalid-token";
		final String JWT = "jwt_token";

		OAuthResponseDto responseDto = OAuthResponseDto.builder()
			.isExistingEmail(false)
			.build();

		OAuthService.OAuthLoginResult result = new OAuthService.OAuthLoginResult(responseDto, JWT);

		when(oAuthLinkService.validateToken(INVALID_LINK_TOKEN))
			.thenThrow(new ApiException(ErrorCode.UNAUTHORIZED));
		when(oAuthService.processOAuthLogin(any(OAuthLoginDto.class))).thenReturn(result);

		// when
		ResultActions resultActions = mockMvc
			.perform(get("/api/users/oauth/login/kakao")
				.queryParam("code", AUTH_CODE)
				.queryParam("link_token", INVALID_LINK_TOKEN)
				.contentType(MediaType.APPLICATION_JSON));

		// then
		resultActions
			.andExpect(status().isOk())
			.andExpect(cookie().exists("accessToken"))
			.andExpect(jsonPath("$.data.existingEmail").value(false));

		verify(oAuthLinkService).validateToken(INVALID_LINK_TOKEN);
		// 검증 실패 시에도 로그인 처리는 정상 진행
		verify(oAuthService).processOAuthLogin(any(OAuthLoginDto.class));
	}
}
