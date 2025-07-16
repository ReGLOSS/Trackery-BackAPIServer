package com.trackery.trackerybackapiserver.domain.common.util;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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

import com.trackery.trackerybackapiserver.domain.config.CommonMockMvcControllerTestSetUp;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.user.controller.UserController;
import com.trackery.trackerybackapiserver.domain.user.dto.UserRegisterDto;
import com.trackery.trackerybackapiserver.domain.user.service.UserService;

import jakarta.servlet.http.Cookie;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.common.util
 * fileName       : GlobalExceptionHandlerTest
 * author         : durururuk
 * date           : 25. 2. 15.
 * description    : GlobalExceptionHandler 테스트
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 02. 17.		durururuk		최초 생성
 * 25. 2. 17.		durururuk		GlobalExceptionHandler 테스트 코드 작성
 * 25. 2. 17.		Nari-Lee		공통응답 테스트코드 추가
 * 25. 2. 18.		durururuk		GlobalExceptionHandlerTest에서 테스트용 API를 사용하지 않고 기존 API를 활용하게 수정
 * 25. 2. 18.		Nari-Lee		dev 병합후 랜딩페이지 엔드포인트 수정
 * 25. 2. 18.		Nari-Lee		GlobalExceptionHandlerTest에 캐시설정 추가
 * 25. 2. 19.		durururuk		MockMvc 테스트코드 작성을 도와주는 추상 클래스 추가
 * 25. 2. 19.		durururuk		코딩 컨벤션에 맞게 정리
 * 25. 2. 19.		durururuk		테스트 하고자 하는 컨트롤러만 로드하게 수정
 * 25. 2. 20.		durururuk		변경된 로직에 맞게 테스트코드 수정
 * 25. 2. 20.		durururuk		변경된 로직에 맞게 테스트코드 수정
 * 25. 2. 21.		durururuk		CustomWebSecurityConfig으로 퍼블릭 uri 관리 일원화
 * 25. 2. 24.		durururuk		회원가입 시 인증 헤더 -> http-only 쿠키 방식으로 변경
 * 25. 3. 14.		durururuk		수정된 로직에 맞게 테스트 코드 수정
 * 25. 3. 27.		Nari-Lee		상세 주석 추가 및 테스트코드 수정
 * 25. 3. 29.		durururuk		mockMvc 단위 테스트용 필터 없는 테스트 컨픽 작성
 */
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerTest extends CommonMockMvcControllerTestSetUp {
	@Autowired
	private MockMvc mockMvc;

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
		ReflectionTestUtils.setField(userRegisterDto, "nickname", "");
		ReflectionTestUtils.setField(userRegisterDto, "password", "Qwerasdf1234!!asdf");

		when(userService.registerUser(any(), any(), any())).thenReturn(null);

		ResultActions result = mockMvc
			.perform(post("/api/users/register")
				.with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(userRegisterDto))
				.cookie(new Cookie("emailToken", "emailToken"))
				.cookie(new Cookie("userNameToken", "userNameToken")));

		result
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value(ErrorCode.BAD_REQUEST_VALIDATION_FAILED.getMessage()));
	}

	@Test
	@WithMockUser
	void HttpMessageNotReadableException_처리_테스트() throws Exception {
		when(userService.registerUser(any(), any(), any())).thenReturn(null);
		ResultActions result = mockMvc
			.perform(post("/api/users/register")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.cookie(new Cookie("emailToken", "emailToken"))
				.cookie(new Cookie("userNameToken", "userNameToken")));

		result
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value(ErrorCode.BAD_REQUEST_INVALID_REQUEST_BODY.getMessage()));
	}
}
