package com.trackery.trackerybackapiserver.domain.user.controller;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.trackery.trackerybackapiserver.domain.CommonMockMvcControllerTestSetUp;
import com.trackery.trackerybackapiserver.domain.user.dto.OAuthLoginDto;
import com.trackery.trackerybackapiserver.domain.user.dto.OAuthResponseDto;
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
 */
@WithMockUser
@WebMvcTest(OAuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class OAuthControllerTest extends CommonMockMvcControllerTestSetUp {
	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OAuthService oAuthService;

	@Test
	void 카카오_로그인_성공() throws Exception {
		// given
		OAuthLoginDto oAuthLoginDto = OAuthLoginDto.builder()
			.provider("KAKAO")
			.code("auth_code")
			.linkAccount(false)
			.build();

		OAuthResponseDto responseDto = OAuthResponseDto.builder()
			.isExistingEmail(false)
			.build();

		OAuthService.OAuthLoginResult result = new OAuthService.OAuthLoginResult(responseDto,"jwt");

		when(oAuthService.processOAuthLogin(any(OAuthLoginDto.class))).thenReturn(result);

		// when
		ResultActions resultActions = mockMvc
			.perform(get("/api/users/oauth/login/kakao")
				.queryParam("code", "auth_code")
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
	void 구글_로그인_성공() throws Exception {
		// given
		OAuthLoginDto oAuthLoginDto = OAuthLoginDto.builder()
			.provider("GOOGLE")
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
			.perform(get("/api/users/oauth/login/google")
				.queryParam("code", "auth_code")
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
	void 깃허브_로그인_성공() throws Exception {
		// given
		OAuthLoginDto oAuthLoginDto = OAuthLoginDto.builder()
			.provider("GITHUB")
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
			.perform(get("/api/users/oauth/login/github")
				.queryParam("code", "auth_code")
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
	void 계정_연동_성공_테스트() throws Exception {
		// given
		OAuthResponseDto responseDto = OAuthResponseDto.builder()
			.isExistingEmail(false)
			.build();

		OAuthService.OAuthLoginResult result = new OAuthService.OAuthLoginResult(responseDto, "jwt");

		when(oAuthService.processOAuthLogin(any(OAuthLoginDto.class))).thenReturn(result);

		// when
		ResultActions resultActions = mockMvc
			.perform(get("/api/users/oauth/login/kakao")
				.queryParam("code", "auth_code")
				.queryParam("link_account", "true")
				.contentType(MediaType.APPLICATION_JSON));

		// then
		resultActions
			.andExpect(status().isOk())
			.andExpect(cookie().exists("accessToken"))
			.andExpect(cookie().httpOnly("accessToken", true))
			.andExpect(cookie().value("accessToken", "jwt"))
			.andExpect(jsonPath("$.data.existingEmail").value(false));
	}
}
