package com.trackery.trackerybackapiserver.domain.mail.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.common.util.JwtUtil;
import com.trackery.trackerybackapiserver.domain.user.entity.User;
import com.trackery.trackerybackapiserver.domain.user.mapper.UserMapper;

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
	private StringRedisTemplate redisTemplate;

	@Mock
	private JwtUtil jwtUtil;

	@Mock
	private ValueOperations<String, String> valueOperations;

	@Mock
	private MailSenderService mailSenderService;

	@Mock
	private UserMapper userMapper;

	@Spy
	@InjectMocks
	private MailService mailService;

	@Test
	void 인증_메일_발송_테스트() {
		String email = "a@a.com";
		String content = "테스트 html";

		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
		when(mailSenderService.generateEmailContents(anyString(), anyString())).thenReturn(content);
		doNothing().when(mailSenderService).sendEmail(anyString(), anyString(), anyString(), anyString());

		mailService.sendEmailVerifyMail(email);

		verify(mailSenderService, times(1)).sendEmail(anyString(), anyString(), anyString(), anyString());
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
	void 유저명_메일_발송_테스트() {
		String email = "a@a.com";
		String userName = "abcdefg";

		User user = User.builder()
			.email(email)
			.userName(userName)
			.build();

		when(userMapper.findByEmail(email)).thenReturn(Optional.of(user));

		doNothing().when(mailSenderService).sendEmail(anyString(), anyString(), anyString(), anyString());

		when(mailSenderService.generateEmailContents(anyString(), anyString())).thenReturn("이메일 html");

		mailService.sendUserNameMail(email);

		verify(userMapper, times(1)).findByEmail(email);
		verify(mailSenderService, times(1)).sendEmail(anyString(), anyString(), anyString(), anyString());
	}

	@Test
	void 유저명_메일_발송_테스트_유저_미존재() {
		String email = "a@a.com";

		when(userMapper.findByEmail(email)).thenReturn(Optional.empty());

		assertThrows(ApiException.class, () -> mailService.sendUserNameMail(email));
		verify(userMapper, times(1)).findByEmail(email);
	}
}
