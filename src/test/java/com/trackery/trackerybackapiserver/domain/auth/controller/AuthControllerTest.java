package com.trackery.trackerybackapiserver.domain.auth.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.ResultActions;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.trackery.trackerybackapiserver.domain.CommonMockMvcControllerTestSetUp;

import jakarta.servlet.http.Cookie;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.auth.controller
 * fileName       : AuthControllerTest
 * author         : durururuk
 * date           : 25. 3. 26.
 * description    :
 * ===========================================================
 * DATE             AUTHOR            NOTE
 * -----------------------------------------------------------
 * 25. 3. 26.       durururuk		최초 생성
 * 25. 3. 26.		durururuk		api/auth/me 테스트 코드 작성
 */
@WebMvcTest(AuthController.class)
class AuthControllerTest extends CommonMockMvcControllerTestSetUp {

	@Test
	void 인증_확인_성공() throws Exception {
		String jwtToken = "eyjasfsafsafaf";

		DecodedJWT decodedJWT = mock(DecodedJWT.class);
		Claim claim = mock(Claim.class);
		when(jwtUtil.verifyJwt(anyString())).thenReturn(decodedJWT);
		when(decodedJWT.getSubject()).thenReturn("1");
		when(decodedJWT.getClaim("role")).thenReturn(claim);
		when(claim.asLong()).thenReturn(1L);

		ResultActions result = mockMvc.perform(get("/api/auth/me")
			.cookie(new Cookie("accessToken", jwtToken)));

		result
			.andExpect(status().isOk());
	}

	@Test
	void 인증_확인_실패() throws Exception {
		ResultActions result = mockMvc.perform(get("/api/auth/me"));

		result.andExpect(status().isUnauthorized());
	}
}
