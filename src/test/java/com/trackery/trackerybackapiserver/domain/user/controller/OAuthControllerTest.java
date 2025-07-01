package com.trackery.trackerybackapiserver.domain.user.controller;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import com.trackery.trackerybackapiserver.domain.common.util.GlobalExceptionHandler;
import com.trackery.trackerybackapiserver.domain.config.CommonMockMvcControllerTestSetUp;
import com.trackery.trackerybackapiserver.domain.jwt.dto.AuthTokenDto;
import com.trackery.trackerybackapiserver.domain.user.dto.OAuthLinkRequestDto;
import com.trackery.trackerybackapiserver.domain.user.dto.OAuthLoginDto;
import com.trackery.trackerybackapiserver.domain.user.dto.OAuthResponseDto;
import com.trackery.trackerybackapiserver.domain.user.dto.OAuthUrlResponseDto;
import com.trackery.trackerybackapiserver.domain.user.entity.CustomUserDetails;
import com.trackery.trackerybackapiserver.domain.user.enums.OAuthProvider;
import com.trackery.trackerybackapiserver.domain.user.service.OAuthLinkTokenService;
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
 * 25. 3. 3.         inari       	최초 생성
 * 25. 3. 26.        inari       	계정 연동 토큰 테스트 추가
 * 25. 6. 17.		 inari			Spring-Rest-Docs api문서 추가
 * 25. 6. 23.        inari		 	기존 유저에 간편 로그인 연동 테스트 추가
 * 25. 6. 25.        inari		 	리프레시 토큰 발급 테스트 추가
 * 25. 6. 28.        inari		 	메서드 분리로 인한 테스트 코드 추가
 * 25. 7. 1.         inari		 	isNewUser 파라미터 테스트 코드 추가
 */
@WithMockUser
@WebMvcTest({OAuthController.class, GlobalExceptionHandler.class})
class OAuthControllerTest extends CommonMockMvcControllerTestSetUp {

	@MockitoBean
	private OAuthService oAuthService;

	@MockitoBean
	private OAuthLinkTokenService oAuthLinkTokenService;

	@ParameterizedTest
	@ValueSource(strings = {"KAKAO", "GOOGLE", "GITHUB"})
	void OAuth_로그인_성공(String provider) throws Exception {
		// given
		final String AUTH_CODE = "auth_code";
		final String API_PATH = "/api/users/oauth/login/";
		final String JWT = "jwt";

		OAuthResponseDto responseDto = OAuthResponseDto.builder()
			.isExistingEmail(false)
			.isNewUser(false)
			.build();

		AuthTokenDto authTokenDto = new AuthTokenDto(JWT, "refresh_token");
		OAuthService.OAuthLoginResult result = new OAuthService.OAuthLoginResult(responseDto, JWT, authTokenDto);

		when(oAuthService.processOAuthLogin(any(OAuthLoginDto.class))).thenReturn(result);

		// when
		ResultActions resultActions = mockMvc
			.perform(get(API_PATH + provider.toLowerCase())
				.queryParam("code", AUTH_CODE)
				.contentType(MediaType.APPLICATION_JSON));

		// then
		resultActions
			.andExpect(cookie().value("accessToken", JWT))
			.andExpect(jsonPath("$.data.existingEmail").value(false))
			.andExpect(jsonPath("$.data.newUser").value(false));
	}

	@Test
	void 네이버_로그인_성공() throws Exception {
		// given
		OAuthResponseDto responseDto = OAuthResponseDto.builder()
			.isExistingEmail(false)
			.isNewUser(false)
			.build();

		AuthTokenDto authTokenDto = new AuthTokenDto("jwt", "refresh_token");
		OAuthService.OAuthLoginResult result = new OAuthService.OAuthLoginResult(responseDto, "jwt", authTokenDto);

		when(oAuthService.processOAuthLogin(any(OAuthLoginDto.class))).thenReturn(result);

		// when
		ResultActions resultActions = mockMvc
			.perform(get("/api/users/oauth/login/{provider}", "naver")
				.queryParam("code", "auth_code")
				.queryParam("state", "state")
				.contentType(MediaType.APPLICATION_JSON));

		// then
		resultActions
			.andExpect(status().isOk())
			.andExpect(cookie().exists("accessToken"))
			.andExpect(cookie().httpOnly("accessToken", true))
			.andExpect(cookie().value("accessToken", "jwt"))
			.andExpect(jsonPath("$.data.existingEmail").value(false))
			.andExpect(jsonPath("$.data.newUser").value(false))
			.andDo(document("oauth-login-success",
				pathParameters(
					parameterWithName("provider").description("OAuth 제공자 (naver, kakao, google, github)")
				),
				queryParameters(
					parameterWithName("code").description("인증 코드"),
					parameterWithName("state").description("상태 값").optional()
				),
				responseFields(
					fieldWithPath("code").description("상태 코드"),
					fieldWithPath("message").description("응답 메시지"),
					fieldWithPath("data.email").description("사용자 이메일").optional(),
					fieldWithPath("data.existingEmail").description("기존 이메일 존재 여부"),
					fieldWithPath("data.newUser").description("신규 사용자 여부")
				)
			));
	}

	@Test
	void 기존_이메일_존재시_연동_미수행_테스트() throws Exception {
		// given
		OAuthResponseDto responseDto = OAuthResponseDto.builder()
			.isExistingEmail(true)
			.isNewUser(false)
			.build();

		OAuthService.OAuthLoginResult result = new OAuthService.OAuthLoginResult(responseDto, null, null);

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
			.andExpect(jsonPath("$.data.existingEmail").value(true))
			.andExpect(jsonPath("$.data.newUser").value(false));
	}

	@Test
	@DisplayName("링크 토큰을 통한 계정 연동 성공")
	void 링크_토큰을_통한_계정_연동_성공() throws Exception {
		// given
		final String AUTH_CODE = "auth_code";
		final String LINK_TOKEN = "valid-link-token";
		final Long USER_ID = 1L;
		final String JWT = "jwt_token";

		OAuthResponseDto responseDto = OAuthResponseDto.builder()
			.isExistingEmail(false)
			.isNewUser(false)
			.build();

		AuthTokenDto authTokenDto = new AuthTokenDto(JWT, "refresh_token");
		OAuthService.OAuthLoginResult result = new OAuthService.OAuthLoginResult(responseDto, JWT, authTokenDto);

		when(oAuthLinkTokenService.validateToken(LINK_TOKEN)).thenReturn(USER_ID);
		when(oAuthService.processOAuthLogin(any(OAuthLoginDto.class))).thenReturn(result);

		// when
		ResultActions resultActions = mockMvc
			.perform(get("/api/users/oauth/login/kakao")
				.queryParam("code", AUTH_CODE)
				.queryParam("linkToken", LINK_TOKEN)
				.contentType(MediaType.APPLICATION_JSON));

		// then
		resultActions
			.andExpect(status().isOk())
			.andExpect(cookie().exists("accessToken"))
			.andExpect(cookie().httpOnly("accessToken", true))
			.andExpect(cookie().value("accessToken", JWT))
			.andExpect(jsonPath("$.data.existingEmail").value(false))
			.andExpect(jsonPath("$.data.newUser").value(false));

		verify(oAuthLinkTokenService).validateToken(LINK_TOKEN);
		verify(oAuthLinkTokenService).deleteToken(LINK_TOKEN);
	}

	@Test
	@DisplayName("OAuth 계정 연동 토큰 생성 성공")
	void OAuth_계정_연동_토큰_생성_성공() throws Exception {
		// given
		final String PROVIDER = "KAKAO";
		final String EMAIL = "test@example.com";
		final String GENERATED_TOKEN = "generated-link-token";

		OAuthLinkRequestDto request = OAuthLinkRequestDto.builder()
			.provider(PROVIDER)
			.email(EMAIL)
			.build();

		when(oAuthLinkTokenService.createLinkToken(PROVIDER, EMAIL)).thenReturn(GENERATED_TOKEN);

		// when
		ResultActions resultActions = mockMvc
			.perform(post("/api/users/oauth/link-account")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.with(csrf()));

		// then
		resultActions
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.token").value(GENERATED_TOKEN))
			.andExpect(jsonPath("$.data.provider").value(PROVIDER))
			.andDo(document("oauth-link-success",
				requestFields(
					fieldWithPath("provider").description("OAuth 제공자 (KAKAO, GOOGLE, GITHUB, NAVER)"),
					fieldWithPath("email").description("연동할 이메일 주소"),
					fieldWithPath("linkAccount").description("계정 연동 여부").optional()
				),
				responseFields(
					fieldWithPath("code").description("상태 코드"),
					fieldWithPath("message").description("응답 메시지"),
					fieldWithPath("data.token").description("생성된 연동 토큰"),
					fieldWithPath("data.provider").description("OAuth 제공자")
				)
			));

		verify(oAuthLinkTokenService).createLinkToken(PROVIDER, EMAIL);
	}

	@Test
	@DisplayName("OAuth URL 생성 성공")
	void OAuth_URL_생성_성공() throws Exception {
		// given
		final String PROVIDER = "kakao";
		final Long USER_ID = 1L;
		final String LINK_TOKEN = "generated-link-token";
		final String AUTH_URL = "https://kauth.kakao.com/oauth/authorize?client_id=test&redirect_uri=test&state=link_" + LINK_TOKEN;

		CustomUserDetails customUserDetails = CustomUserDetails.builder()
			.userId(USER_ID)
			.userName("testuser")
			.roleId(1L)
			.build();

		OAuthUrlResponseDto urlResponse = OAuthUrlResponseDto.builder()
			.authUrl(AUTH_URL)
			.provider(PROVIDER.toUpperCase())
			.state("link_" + LINK_TOKEN)
			.build();

		when(oAuthLinkTokenService.createLinkToken(PROVIDER.toUpperCase(), USER_ID)).thenReturn(LINK_TOKEN);
		when(oAuthService.generateAuthUrlWithToken(OAuthProvider.KAKAO, LINK_TOKEN)).thenReturn(urlResponse);

		// when
		ResultActions resultActions = mockMvc
			.perform(post("/api/users/oauth/link/{provider}/url", PROVIDER)
				.contentType(MediaType.APPLICATION_JSON)
				.with(user(customUserDetails)));

		// then
		resultActions
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.authUrl").value(AUTH_URL))
			.andExpect(jsonPath("$.data.provider").value(PROVIDER.toUpperCase()))
			.andExpect(jsonPath("$.data.state").value("link_" + LINK_TOKEN))
			.andDo(document("oauth-url-generation",
				pathParameters(
					parameterWithName("provider").description("OAuth 제공자 (kakao, google, github, naver)")
				),
				responseFields(
					fieldWithPath("code").description("상태 코드"),
					fieldWithPath("message").description("응답 메시지"),
					fieldWithPath("data.authUrl").description("OAuth 인증 URL"),
					fieldWithPath("data.provider").description("OAuth 제공자"),
					fieldWithPath("data.state").description("상태 값")
				)
			));

		verify(oAuthLinkTokenService).createLinkToken(PROVIDER.toUpperCase(), USER_ID);
		verify(oAuthService).generateAuthUrlWithToken(OAuthProvider.KAKAO, LINK_TOKEN);
	}

	@Test
	@DisplayName("OAuth URL 생성 실패 - 서비스 예외")
	void OAuth_URL_생성_실패() throws Exception {
		// given
		final String PROVIDER = "kakao";
		final Long USER_ID = 1L;

		CustomUserDetails customUserDetails = CustomUserDetails.builder()
			.userId(USER_ID)
			.userName("testuser")
			.roleId(1L)
			.build();

		when(oAuthLinkTokenService.createLinkToken(PROVIDER.toUpperCase(), USER_ID))
			.thenThrow(new RuntimeException("서비스 에러"));

		// when & then
		mockMvc
			.perform(post("/api/users/oauth/link/{provider}/url", PROVIDER)
				.contentType(MediaType.APPLICATION_JSON)
				.with(user(customUserDetails)))
			.andExpect(status().is5xxServerError());
	}

	@Test
	@DisplayName("네이버 state에서 링크 토큰 추출 성공")
	void 네이버_state에서_링크_토큰_추출_성공() throws Exception {
		// given
		final String AUTH_CODE = "auth_code";
		final String LINK_TOKEN = "valid-link-token";
		final String STATE = "random_state_" + LINK_TOKEN;
		final Long USER_ID = 1L;
		final String JWT = "jwt_token";

		OAuthResponseDto responseDto = OAuthResponseDto.builder()
			.isExistingEmail(false)
			.isNewUser(false)
			.build();

		AuthTokenDto authTokenDto = new AuthTokenDto(JWT, "refresh_token");
		OAuthService.OAuthLoginResult result = new OAuthService.OAuthLoginResult(responseDto, JWT, authTokenDto);

		when(oAuthLinkTokenService.validateToken(LINK_TOKEN)).thenReturn(USER_ID);
		when(oAuthService.processOAuthLogin(any(OAuthLoginDto.class))).thenReturn(result);

		// when
		ResultActions resultActions = mockMvc
			.perform(get("/api/users/oauth/login/naver")
				.queryParam("code", AUTH_CODE)
				.queryParam("state", STATE)
				.contentType(MediaType.APPLICATION_JSON));

		// then
		resultActions
			.andExpect(status().isOk())
			.andExpect(cookie().exists("accessToken"))
			.andExpect(cookie().value("accessToken", JWT))
			.andExpect(jsonPath("$.data.existingEmail").value(false))
			.andExpect(jsonPath("$.data.newUser").value(false));

		verify(oAuthLinkTokenService).validateToken(LINK_TOKEN);
		verify(oAuthLinkTokenService).deleteToken(LINK_TOKEN);
	}

	@Test
	@DisplayName("네이버 잘못된 state 형식")
	void 네이버_잘못된_state_형식() throws Exception {
		// given
		final String AUTH_CODE = "auth_code";
		final String INVALID_STATE = "invalid_format";
		final String JWT = "jwt_token";

		OAuthResponseDto responseDto = OAuthResponseDto.builder()
			.isExistingEmail(false)
			.isNewUser(false)
			.build();

		AuthTokenDto authTokenDto = new AuthTokenDto(JWT, "refresh_token");
		OAuthService.OAuthLoginResult result = new OAuthService.OAuthLoginResult(responseDto, JWT, authTokenDto);

		when(oAuthService.processOAuthLogin(any(OAuthLoginDto.class))).thenReturn(result);

		// when
		ResultActions resultActions = mockMvc
			.perform(get("/api/users/oauth/login/naver")
				.queryParam("code", AUTH_CODE)
				.queryParam("state", INVALID_STATE)
				.contentType(MediaType.APPLICATION_JSON));

		// then
		resultActions
			.andExpect(status().isOk())
			.andExpect(cookie().exists("accessToken"))
			.andExpect(jsonPath("$.data.existingEmail").value(false))
			.andExpect(jsonPath("$.data.newUser").value(false));

		verify(oAuthLinkTokenService, never()).validateToken(any());
	}

	@Test
	@DisplayName("링크 토큰 검증 실패")
	void 링크_토큰_검증_실패() throws Exception {
		// given
		final String AUTH_CODE = "auth_code";
		final String INVALID_LINK_TOKEN = "invalid-link-token";
		final String JWT = "jwt_token";

		OAuthResponseDto responseDto = OAuthResponseDto.builder()
			.isExistingEmail(false)
			.isNewUser(false)
			.build();

		AuthTokenDto authTokenDto = new AuthTokenDto(JWT, "refresh_token");
		OAuthService.OAuthLoginResult result = new OAuthService.OAuthLoginResult(responseDto, JWT, authTokenDto);

		when(oAuthLinkTokenService.validateToken(INVALID_LINK_TOKEN))
			.thenThrow(new RuntimeException("토큰 검증 실패"));
		when(oAuthService.processOAuthLogin(any(OAuthLoginDto.class))).thenReturn(result);

		// when
		ResultActions resultActions = mockMvc
			.perform(get("/api/users/oauth/login/kakao")
				.queryParam("code", AUTH_CODE)
				.queryParam("linkToken", INVALID_LINK_TOKEN)
				.contentType(MediaType.APPLICATION_JSON));

		// then
		resultActions
			.andExpect(status().isOk())
			.andExpect(cookie().exists("accessToken"))
			.andExpect(jsonPath("$.data.existingEmail").value(false))
			.andExpect(jsonPath("$.data.newUser").value(false));

		verify(oAuthLinkTokenService).validateToken(INVALID_LINK_TOKEN);
		verify(oAuthLinkTokenService, never()).deleteToken(any());
	}

	@Test
	@DisplayName("기존 이메일 존재하고 계정 연동하는 경우")
	void 기존_이메일_존재_계정_연동_성공() throws Exception {
		// given
		final String AUTH_CODE = "auth_code";
		final String LINK_TOKEN = "valid-link-token";
		final Long USER_ID = 1L;
		final String JWT = "jwt_token";

		OAuthResponseDto responseDto = OAuthResponseDto.builder()
			.isExistingEmail(true)
			.isNewUser(false)
			.build();

		AuthTokenDto authTokenDto = new AuthTokenDto(JWT, "refresh_token");
		OAuthService.OAuthLoginResult result = new OAuthService.OAuthLoginResult(responseDto, JWT, authTokenDto);

		when(oAuthLinkTokenService.validateToken(LINK_TOKEN)).thenReturn(USER_ID);
		when(oAuthService.processOAuthLogin(any(OAuthLoginDto.class))).thenReturn(result);

		// when
		ResultActions resultActions = mockMvc
			.perform(get("/api/users/oauth/login/kakao")
				.queryParam("code", AUTH_CODE)
				.queryParam("linkToken", LINK_TOKEN)
				.contentType(MediaType.APPLICATION_JSON));

		// then
		resultActions
			.andExpect(status().isOk())
			.andExpect(cookie().exists("accessToken"))
			.andExpect(jsonPath("$.data.existingEmail").value(true))
			.andExpect(jsonPath("$.data.newUser").value(false));
	}

	@Test
	@DisplayName("AuthTokenDto가 null인 경우 - 레거시 JWT 토큰 사용")
	void AuthTokenDto가_null인_경우_레거시_JWT_사용() throws Exception {
		// given
		final String AUTH_CODE = "auth_code";
		final String JWT = "jwt_token";

		OAuthResponseDto responseDto = OAuthResponseDto.builder()
			.isExistingEmail(false)
			.isNewUser(false)
			.build();

		// AuthTokenDto가 null인 경우
		OAuthService.OAuthLoginResult result = new OAuthService.OAuthLoginResult(responseDto, JWT, null);

		when(oAuthService.processOAuthLogin(any(OAuthLoginDto.class))).thenReturn(result);

		// when
		ResultActions resultActions = mockMvc
			.perform(get("/api/users/oauth/login/kakao")
				.queryParam("code", AUTH_CODE)
				.contentType(MediaType.APPLICATION_JSON));

		// then
		resultActions
			.andExpect(status().isOk())
			.andExpect(cookie().exists("accessToken"))
			.andExpect(cookie().value("accessToken", JWT))
			.andExpect(jsonPath("$.data.existingEmail").value(false))
			.andExpect(jsonPath("$.data.newUser").value(false));
	}

	@Test
	@DisplayName("신규 사용자 OAuth 로그인 성공")
	void 신규_사용자_OAuth_로그인_성공() throws Exception {
		// given
		final String AUTH_CODE = "auth_code";
		final String JWT = "jwt_token";

		OAuthResponseDto responseDto = OAuthResponseDto.builder()
			.isExistingEmail(false)
			.isNewUser(true)
			.build();

		AuthTokenDto authTokenDto = new AuthTokenDto(JWT, "refresh_token");
		OAuthService.OAuthLoginResult result = new OAuthService.OAuthLoginResult(responseDto, JWT, authTokenDto);

		when(oAuthService.processOAuthLogin(any(OAuthLoginDto.class))).thenReturn(result);

		// when
		ResultActions resultActions = mockMvc
			.perform(get("/api/users/oauth/login/kakao")
				.queryParam("code", AUTH_CODE)
				.contentType(MediaType.APPLICATION_JSON));

		// then
		resultActions
			.andExpect(status().isOk())
			.andExpect(cookie().exists("accessToken"))
			.andExpect(cookie().value("accessToken", JWT))
			.andExpect(jsonPath("$.data.existingEmail").value(false))
			.andExpect(jsonPath("$.data.newUser").value(true));
	}
}
