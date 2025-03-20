package com.trackery.trackerybackapiserver.domain.mail.service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.common.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.mail.service
 * fileName       : MailService
 * author         : durururuk
 * date           : 25. 3. 3.
 * description    : 이메일 전송 서비스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 3.        durururuk      최초 생성
 * 25. 3. 3.        durururuk      인증번호 이메일 전송 기능 추가
 * 25. 3. 5.        durururuk	   이메일 관련 기능 user 도메인에서 분리
 * 25. 3. 20.		durururuk	   이메일 템플릿 정적 리소스에 넣어두고 꺼내쓸 수 있게 수정
 * 25. 3. 20.	    durururuk	   이메일 전송 관련 로직 분리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {
	private final StringRedisTemplate redisTemplate;
	private final SecureRandom secureRandom = new SecureRandom();
	private final JwtUtil jwtUtil;
	private final EmailSenderService emailSenderService;

	private static final String EMAIL_VERIFICATION_REDIS_KEY = "email:verify:";

	/**
	 * HTML 양식으로 인증번호를 알려주는 이메일을 보냅니다.
	 *
	 * @param emailAddress : 발신 대상 이메일
	 */
	@SuppressWarnings({"checkstyle:RegexpSingleline", "checkstyle:LineLength"})
	public void sendEmailVerifyMail(String emailAddress) {
		String authNumber = String.format("%06d", secureRandom.nextInt(1000000));

		String htmlTitle = "Trackery 인증번호입니다.";
		String htmlContents = String.format("""
			아래 인증 번호를 확인하여 인증을 완료해주세요.
			<hr style="border: 0; border-top: 1px solid #bbb;">
			<p><strong>이메일 :</strong> %s</p>
			<p><strong>인증 번호 :</strong> %s</p>
			""", emailAddress, authNumber);

		String content = emailSenderService.generateEmailContents(htmlTitle, htmlContents);

		String emailTitle = "Trackery 인증번호입니다.";

		String logTitle = "인증번호";

		emailSenderService.sendEmail(emailAddress, emailTitle, content, logTitle);

		redisTemplate.opsForValue().set(EMAIL_VERIFICATION_REDIS_KEY + emailAddress, authNumber, 5, TimeUnit.MINUTES);
	}

	/**
	 * 이메일 인증을 검증합니다.
	 *
	 * @param emailAddress : 검증할 이메일
	 * @param authNumber : 입력된 인증번호
	 * @return : 이메일 JWT 토큰
	 */
	public String verifyEmail(String emailAddress, String authNumber) {
		String redisNumber = redisTemplate.opsForValue().get(EMAIL_VERIFICATION_REDIS_KEY + emailAddress);

		if (redisNumber == null || !redisNumber.equals(authNumber)) {
			throw new ApiException(ErrorCode.BAD_REQUEST);
		}

		redisTemplate.delete(EMAIL_VERIFICATION_REDIS_KEY + emailAddress);

		return jwtUtil.generateTokenWithSubject(emailAddress);
	}

}
