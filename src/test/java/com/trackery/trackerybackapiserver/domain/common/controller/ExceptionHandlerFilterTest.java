package com.trackery.trackerybackapiserver.domain.common.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trackery.trackerybackapiserver.config.JwtFilter;
import com.trackery.trackerybackapiserver.domain.CommonMockMvcControllerTestSetUp;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.common.controller
 * fileName       : ExceptionHandlerFilterTest
 * author         : durururuk
 * date           : 25. 3. 14.
 * description    : 예외 처리 필터 테스트
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 14.        durururuk      최초 생성
 */

@WebMvcTest(TestController.class)
@AutoConfigureMockMvc
class ExceptionHandlerFilterTest extends CommonMockMvcControllerTestSetUp {
	@MockitoBean
	private JwtFilter jwtFilter;

	@Test
	void JWT_필터_예외_발생_시_처리_테스트() throws Exception {
		doThrow(new ApiException(ErrorCode.UNAUTHORIZED_JWT_VERIFY_FAILED)).when(jwtFilter)
			.doFilter(any(), any(), any());

		ResultActions result = mockMvc.perform(get("/test"));

		result.andExpect(status().isUnauthorized())
			.andDo(print());
	}
}

//현재 인증이 필요한 API가 없어서 임시로 테스트 컨트롤러 사용했습니다.
@RestController
class TestController {
	@GetMapping("/test")
	public ResponseEntity<String> testApi() {
		return ResponseEntity.ok("테스트 실패");
	}
}