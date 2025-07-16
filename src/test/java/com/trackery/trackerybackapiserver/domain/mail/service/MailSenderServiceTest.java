package com.trackery.trackerybackapiserver.domain.mail.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;

import jakarta.mail.internet.MimeMessage;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.mail.service
 * fileName       : MailSenderServiceTest
 * author         : durururuk
 * date           : 25. 3. 20.
 * description    : EmailSenderService 단위 테스트
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 20.		durururuk		최초 생성
 * 25. 3. 20.		durururuk		수정 된 로직에 맞게 테스트 코드 작성
 * 25. 3. 25.		durururuk		유저명 찾기 기능 구현, 테스트 코드 작성
 * 25. 3. 25.		durururuk		이메일 발송 실패 예외 처리 테스트 코드 작성
 * 25. 3. 25.		durururuk		invocation -> when.thenReturn으로 수정
 */
@ExtendWith(MockitoExtension.class)
class MailSenderServiceTest {
	@Mock
	JavaMailSender javaMailSender;

	@Mock
	TemplateEngine templateEngine;

	@InjectMocks
	MailSenderService mailSenderService;

	@Test
	void 이메일_템플릿_생성_테스트() {
		String htmlTitle = "테스트 제목";
		String htmlContents = "테스트 본문";

		String expectedHtml = "테스트 html";

		when(templateEngine.process(anyString(), any(Context.class))).thenReturn(expectedHtml);

		String resultHtml = mailSenderService.generateEmailContents(htmlTitle, htmlContents);

		assertNotNull(resultHtml);
		assertEquals(expectedHtml, resultHtml);

		verify(templateEngine, times(1)).process(anyString(), any(Context.class));
	}

	@Test
	void 메일_전송_테스트() {
		String email = "a@a.com";
		String subject = "제목";
		String content = "내용";
		String logTitle = "테스트 메일";
		MimeMessage mimeMessage = mock(MimeMessage.class);

		when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
		doNothing().when(javaMailSender).send(any(MimeMessage.class));

		mailSenderService.sendEmail(email, subject, content, logTitle);

		verify(javaMailSender, times(1)).createMimeMessage();
		verify(javaMailSender, times(1)).send(mimeMessage);
	}

	@Test
	void 메일_전송_실패_테스트() {
		String email = "wrongAddress@email.com";
		String subject = "테스트 제목";
		String content = "테스트 내용";
		String logTitle = "테스트 로그";

		MimeMessage mimeMessage = mock(MimeMessage.class);

		when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
		doThrow(new MailException("테스트 예외") {
		}).when(javaMailSender).send(any(MimeMessage.class));

		ApiException exception = assertThrows(ApiException.class,
			() -> mailSenderService.sendEmail(email, subject, content, logTitle));

		assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, exception.getErrorCode());

		verify(javaMailSender, times(1)).createMimeMessage();
		verify(javaMailSender, times(1)).send(mimeMessage);
	}
}