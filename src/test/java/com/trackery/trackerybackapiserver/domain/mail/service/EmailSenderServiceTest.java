package com.trackery.trackerybackapiserver.domain.mail.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.mail.service
 * fileName       : EmailSenderServiceTest
 * author         : durururuk
 * date           : 25. 3. 20.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 20.        durururuk      최초 생성
 */
@ExtendWith(MockitoExtension.class)
class EmailSenderServiceTest {
	@Mock
	JavaMailSender javaMailSender;

	@Mock
	TemplateEngine templateEngine;

	@InjectMocks
	EmailSenderService emailSenderService;

	@Test
	void 이메일_템플릿_생성_테스트() {
		String htmlTitle = "테스트 제목";
		String htmlContents = "테스트 본문";

		String expectedHtml = "테스트 html";

		when(templateEngine.process(anyString(), any(Context.class))).thenReturn(expectedHtml);

		String resultHtml = emailSenderService.generateEmailContents(htmlTitle, htmlContents);

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

		emailSenderService.sendEmail(email, subject, content, logTitle);

		verify(javaMailSender, times(1)).createMimeMessage();
		verify(javaMailSender, times(1)).send(mimeMessage);
	}
}