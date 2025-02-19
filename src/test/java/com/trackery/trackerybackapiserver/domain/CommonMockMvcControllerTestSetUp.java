package com.trackery.trackerybackapiserver.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackery.trackerybackapiserver.config.SecurityConfig;
import com.trackery.trackerybackapiserver.domain.common.util.JwtUtil;

/**
 *packageName    : com.trackery.trackerybackapiserver.domain

 fileName       : CommonMockMvcControllerTestSetUp
 author         : durururuk
 date           : 25. 2. 19.
 description    : 컨트롤러 테스트를 위한 mockMvc를 사용할 때 필요한 필수 bean을 불러오는 추상 클래스입니다.
 ===========================================================
 DATE              AUTHOR             NOTE
 -----------------------------------------------------------
 25. 2. 19.        durururuk       최초 생성
 */

@Import(SecurityConfig.class)
@AutoConfigureMockMvc
@AutoConfigureRestDocs
public abstract class CommonMockMvcControllerTestSetUp {
	@MockitoBean
	protected JwtUtil jwtUtil;

	protected ObjectMapper objectMapper = new ObjectMapper();
}
