package com.trackery.trackerybackapiserver.domain.mail.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.ResultActions;

import com.trackery.trackerybackapiserver.domain.config.CommonMockMvcControllerTestSetUp;
import com.trackery.trackerybackapiserver.domain.common.dto.VerifyEmailDto;
import com.trackery.trackerybackapiserver.domain.mail.service.MailService;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.common.controller
 * fileName       : MailControllerTest
 * author         : durururuk
 * date           : 25. 3. 5.
 * description    : 메일 컨트롤러 테스트
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 5.        durururuk      최초 생성
 * 25. 3. 5.		durururuk	   컨트롤러 테스트 코드 작성
 * 25. 6. 18.      inari          Spring REST Docs API 문서 추가
 */
@WithMockUser
@WebMvcTest(MailController.class)
@AutoConfigureMockMvc(addFilters = false)
class MailControllerTest extends CommonMockMvcControllerTestSetUp {
	@MockitoBean
	private MailService mailService;

	@Spy
	private VerifyEmailDto verifyEmailDto;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(verifyEmailDto, "email", "a@a.com");
		ReflectionTestUtils.setField(verifyEmailDto, "authNumber", "123456");
	}

	@Test
	void 인증_메일_요청_테스트() throws Exception {
		doNothing().when(mailService).sendEmailVerifyMail(anyString());

		ResultActions result = mockMvc.perform(
			post("/api/mail/request-verify/email").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(verifyEmailDto))
				.with(csrf()));

		result
			.andExpect(status().isOk())
			.andDo(document("request-email-verification",
				requestFields(
					fieldWithPath("email").description("인증을 요청할 이메일 주소"),
					fieldWithPath("authNumber").description("인증번호 (요청 시에는 무시됨)").optional()
				),
				responseFields(
					fieldWithPath("code").description("상태 코드"),
					fieldWithPath("message").description("응답 메시지")
				)
			));
	}

	@Test
	void 메일_검증_테스트() throws Exception {
		String testJwt = "jwt";
		when(mailService.verifyEmail(anyString(), anyString())).thenReturn(testJwt);

		ResultActions result = mockMvc.perform(post("/api/mail/verify/email")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(verifyEmailDto))
			.with(csrf()));

		result
			.andExpect(status().isOk())
			.andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("emailToken=")))
			.andDo(document("verify-email",
				requestFields(
					fieldWithPath("email").description("인증할 이메일 주소"),
					fieldWithPath("authNumber").description("이메일로 받은 인증번호")
				),
				responseFields(
					fieldWithPath("code").description("상태 코드"),
					fieldWithPath("message").description("응답 메시지")
				)
			));
	}

	@Test
	void 유저명_찾기_메일_테스트() throws Exception {
		doNothing().when(mailService).sendUserNameMail(anyString());

		ResultActions result = mockMvc.perform(
			post("/api/mail/find-username").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(verifyEmailDto))
				.with(csrf()));

		result
			.andExpect(status().isOk())
			.andDo(document("find-username-by-email",
				requestFields(
					fieldWithPath("email").description("사용자명을 찾을 이메일 주소"),
					fieldWithPath("authNumber").description("인증번호 (이 API에서는 무시됨)").optional()
				),
				responseFields(
					fieldWithPath("code").description("상태 코드"),
					fieldWithPath("message").description("응답 메시지")
				)
			));
	}
}
