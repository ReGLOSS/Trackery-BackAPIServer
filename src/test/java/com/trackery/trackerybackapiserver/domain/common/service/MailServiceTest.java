package com.trackery.trackerybackapiserver.domain.common.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.javamail.JavaMailSender;

import com.trackery.trackerybackapiserver.domain.common.util.JwtUtil;

import jakarta.mail.internet.MimeMessage;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.common.service
 * fileName       : MailServiceTest
 * author         : durururuk
 * date           : 25. 3. 5.
 * description    : 이메일 서비스 테스트
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 5.        durururuk      최초 생성
 * 25. 3. 5.		durururuk      메일 서비스 단위 테스트 코드 작성
 * 25. 3. 14.		durururuk	   분리된 로직에 맞게 테스트 코드 재작성
 */
@ExtendWith(MockitoExtension.class)
class MailServiceTest {

	@Mock
	private JavaMailSender javaMailSender;

	@Mock
	private StringRedisTemplate redisTemplate;

	@Mock
	private JwtUtil jwtUtil;

	@Mock
	private ValueOperations<String, String> valueOperations;

	@Spy
	@InjectMocks
	private MailService mailService;

	@Test
	void 인증_메일_발송_테스트() {
		String email = "a@a.com";

		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		doNothing().when(mailService).sendEmail(anyString(), anyString(), anyString(), anyString());
		doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));

		mailService.sendEmailVerifyMail(email);

		verify(mailService, times(1)).sendEmail(anyString(), anyString(), anyString(), anyString());
		verify(redisTemplate, times(1)).opsForValue();
		verify(valueOperations, times(1)).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
	}


	@Test
	void 메일_검증_테스트() {
		String email = "a@a.com";
		String authNumber = "123456";
		String redisKey = "email:verify:" + email;
		String emailToken = "mockedEmailToken";

		when(valueOperations.get(redisKey)).thenReturn(authNumber);
		when(jwtUtil.generateTokenWithSubject(email)).thenReturn(emailToken);
		when(redisTemplate.delete(redisKey)).thenReturn(true);
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);

		String resultToken = mailService.verifyEmail(email, authNumber);

		assertEquals(emailToken, resultToken);
		verify(valueOperations, times(1)).get(redisKey);
		verify(jwtUtil, times(1)).generateTokenWithSubject(email);
		verify(redisTemplate, times(1)).delete(redisKey);
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

		mailService.sendEmail(email, subject, content, logTitle);

		verify(javaMailSender, times(1)).createMimeMessage();
		verify(javaMailSender, times(1)).send(mimeMessage);
	}
}