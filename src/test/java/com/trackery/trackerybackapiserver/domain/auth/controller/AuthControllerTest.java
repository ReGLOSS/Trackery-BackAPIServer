package com.trackery.trackerybackapiserver.domain.auth.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import com.trackery.trackerybackapiserver.domain.CommonMockMvcControllerTestSetUp;
import com.trackery.trackerybackapiserver.domain.auth.dto.AuthUserDto;
import com.trackery.trackerybackapiserver.domain.auth.service.AuthService;

import jakarta.servlet.http.Cookie;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.auth.controller
 * fileName       : AuthControllerTest
 * author         : durururuk
 * date           : 25. 3. 26.
 * description    : AuthController 단위테스트코드
 * ===========================================================
 * DATE             AUTHOR            NOTE
 * -----------------------------------------------------------
 * 25. 3. 26.       durururuk		최초 생성
 * 25. 3. 26.		durururuk		api/auth/me 테스트 코드 작성
 * 25. 3. 26.		durururuk		바뀐 로직에 맞게 테스트 코드 수정
 */
@WebMvcTest(AuthController.class)
class AuthControllerTest extends CommonMockMvcControllerTestSetUp {
	@MockitoBean
	private AuthService authService;

	@Test
	void 인증_확인_성공() throws Exception {
		AuthUserDto authUserDto = new AuthUserDto(1L, "abcdefg", 1L);
		when(authService.toAuthUserDto(any())).thenReturn(authUserDto);

		ResultActions result = mockMvc.perform(get("/api/auth/me")
			.cookie(new Cookie("accessToken", "JwtToken")));

		result
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.userId").value(1))
			.andExpect(jsonPath("$.data.userName").value("abcdefg"))
			.andExpect(jsonPath("$.data.userRoleId").value(1));
	}
}
