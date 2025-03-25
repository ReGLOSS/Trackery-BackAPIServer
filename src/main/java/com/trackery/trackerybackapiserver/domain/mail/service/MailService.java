package com.trackery.trackerybackapiserver.domain.mail.service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.common.util.JwtUtil;
import com.trackery.trackerybackapiserver.domain.user.entity.User;
import com.trackery.trackerybackapiserver.domain.user.mapper.UserMapper;

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
 * 25. 3. 25.		durururuk	   유저명 찾기 기능 구현
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {
	private final StringRedisTemplate redisTemplate;
	private final SecureRandom secureRandom = new SecureRandom();
	private final JwtUtil jwtUtil;
	private final MailSenderService mailSenderService;

	private static final String EMAIL_VERIFICATION_REDIS_KEY = "email:verify:";
	private final UserMapper userMapper;

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

		String content = mailSenderService.generateEmailContents(htmlTitle, htmlContents);

		String emailTitle = "Trackery 인증번호입니다.";

		String logTitle = "인증번호 메일 발송";

		mailSenderService.sendEmail(emailAddress, emailTitle, content, logTitle);

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

	/**
	 * 이메일을 받아서 회원의 유저명을 안내하는 이메일을 발송하는 메서드
	 *
	 * @param emailAddress : 발신 대상 이메일
	 */
	public void sendUserNameMail(String emailAddress) {
		User user = userMapper.findByEmail(emailAddress).orElseThrow(
			() -> new ApiException(ErrorCode.NOT_FOUND)
		);

		String userName = user.getUserName();

		String emailTitle = "Trackery 유저명 안내입니다.";

		String htmlTitle = "Trackery 유저명은 다음과 같습니다.";
		String htmlContents = String.format("""
			유저명을 확인하여 로그인을 진행해주십시오.
			<hr style="border: 0; border-top: 1px solid #bbb;">
			<p><strong> %s </strong></p>
			""", userName);
		String emailContents = mailSenderService.generateEmailContents(htmlTitle, htmlContents);

		String logTitle = "유저명 찾기 메일 발송";

		mailSenderService.sendEmail(emailAddress, emailTitle, emailContents, logTitle);
	}

}
