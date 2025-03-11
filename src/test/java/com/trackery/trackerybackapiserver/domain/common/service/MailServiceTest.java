package com.trackery.trackerybackapiserver.domain.common.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

	@InjectMocks
	private MailService mailService;

	@Test
	void 메일_발송_테스트() {
		String email = "a@a.com";
		MimeMessage mimeMessage = mock(MimeMessage.class);

		when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
		doNothing().when(javaMailSender).send(any(MimeMessage.class));

		mailService.sendEmailVerifyMail(email);

		verify(javaMailSender, times(1)).createMimeMessage();
		verify(javaMailSender, times(1)).send(mimeMessage);
	}

	@Test
	void 메일_검증_테스트() {
		String email = "a@a.com";
		String authNumber = "123456";
		String redisKey = "email:verify:" + email;
		String emailToken = "mockedEmailToken";

		when(valueOperations.get(redisKey)).thenReturn(authNumber);
		when(jwtUtil.generateEmailToken(email)).thenReturn(emailToken);
		when(redisTemplate.delete(redisKey)).thenReturn(true);
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);

		String resultToken = mailService.verifyEmail(email, authNumber);

		assertEquals(emailToken, resultToken);
		verify(valueOperations, times(1)).get(redisKey);
		verify(jwtUtil, times(1)).generateEmailToken(email);
		verify(redisTemplate, times(1)).delete(redisKey);
	}
}