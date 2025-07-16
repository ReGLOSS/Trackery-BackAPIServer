package com.trackery.trackerybackapiserver.domain.auth.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import com.trackery.trackerybackapiserver.domain.config.CommonMockMvcControllerTestSetUp;
import com.trackery.trackerybackapiserver.domain.auth.dto.AuthUserDto;
import com.trackery.trackerybackapiserver.domain.auth.service.AuthService;
import com.trackery.trackerybackapiserver.domain.user.entity.CustomUserDetails;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.auth.controller
 * fileName       : AuthControllerTest
 * author         : durururuk
 * date           : 25. 3. 26.
 * description    : AuthController 단위테스트코드
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 26.		durururuk		최초 생성
 * 25. 3. 26.		durururuk		api/auth/me 테스트 코드 작성
 * 25. 3. 27.		durururuk		인증 api 단위테스트 수정
 * 25. 3. 28.		durururuk		리프레시 토큰 레디스 저장 기능 구현
 * 25. 3. 29.		durururuk		mockMvc 단위 테스트용 필터 없는 테스트 컨픽 작성
 * 25. 3. 29.		durururuk		mockMvc 단위 테스트용 필터 없는 테스트 컨픽 작성
 * 25. 6. 18.		Nari-Lee		Spring-Rest-Docs api문서 추가
 */
@WebMvcTest(AuthController.class)
class AuthControllerTest extends CommonMockMvcControllerTestSetUp {
	@MockitoBean
	private AuthService authService;

	@Test
	void 인증_확인_성공() throws Exception {
		CustomUserDetails customUserDetails = CustomUserDetails.builder()
			.userId(1L)
			.roleId(1L)
			.build();
		
		AuthUserDto authUserDto = new AuthUserDto(1L, "abcdefg", 1L);
		when(authService.toAuthUserDto(any())).thenReturn(authUserDto);

		ResultActions result = mockMvc.perform(get("/api/auth/me")
			.with(user(customUserDetails)));

		result
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.userId").value(1))
			.andExpect(jsonPath("$.data.userName").value("abcdefg"))
			.andExpect(jsonPath("$.data.userRoleId").value(1))
			.andDo(document("auth-check-success",
				responseFields(
					fieldWithPath("code").description("상태 코드"),
					fieldWithPath("message").description("응답 메시지"),
					fieldWithPath("data.userId").description("사용자 ID"),
					fieldWithPath("data.userName").description("사용자명"),
					fieldWithPath("data.userRoleId").description("사용자 역할 ID")
				)
			));
	}
}
