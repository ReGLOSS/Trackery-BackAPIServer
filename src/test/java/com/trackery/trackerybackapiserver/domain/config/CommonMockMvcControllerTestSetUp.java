package com.trackery.trackerybackapiserver.domain.config;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

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

@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import({MockMvcUnitTestSecurityConfig.class, RestDocsConfiguration.class})
@ExtendWith(RestDocumentationExtension.class)
public abstract class CommonMockMvcControllerTestSetUp {
	@Autowired
	protected MockMvc mockMvc;

	protected ObjectMapper objectMapper = new ObjectMapper();
}
