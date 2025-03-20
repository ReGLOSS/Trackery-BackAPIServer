package com.trackery.trackerybackapiserver.domain.common.service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.common.util.JwtUtil;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.common.service
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
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {
	private final JavaMailSender javaMailSender;
	private final StringRedisTemplate redisTemplate;
	private final SecureRandom secureRandom = new SecureRandom();
	private final JwtUtil jwtUtil;
	private final TemplateEngine templateEngine;

	private static final String EMAIL_VERIFICATION_REDIS_KEY = "email:verify:";

	/**
	 * 이메일 템플릿 내부에서의 제목과 본문을 동적으로 작성하는 메서드
	 * @param htmlTitle : 이메일 내부 제목
	 * @param htmlContents 이메일 내부 본문
	 * @return : 동적으로 생성된 html 템플릿
	 */
	private String generateEmailContents(String htmlTitle, String htmlContents) {
		Context context = new Context();
		context.setVariable("htmlTitle", htmlTitle);
		context.setVariable("htmlContents", htmlContents);
		return templateEngine.process("email-template", context);
	}

	/**
	 * 이메일을 전송합니다.
	 * @param emailAddress : 발신 대상 이메일
	 * @param emailTitle : 이메일 제목
	 * @param content : 이메일 본문
	 * @param logTitle : 로그 제목
	 */
	private void sendEmail(String emailAddress, String emailTitle, String content, String logTitle) {
		try {
			MimeMessage mimeMessage = javaMailSender.createMimeMessage();

			MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

			mimeMessageHelper.setTo(emailAddress);
			mimeMessageHelper.setSubject(emailTitle);

			mimeMessageHelper.setText(content, true);

			javaMailSender.send(mimeMessage);
			log.info("{} 이메일 전송 완료 : {}", logTitle, emailAddress);

		} catch (MessagingException e) {
			log.error("{} 이메일 전송 실패 : {}", logTitle, emailAddress);
			throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

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

		String content = generateEmailContents(htmlTitle, htmlContents);

		String emailTitle = "Trackery 인증번호입니다.";

		String logTitle = "인증번호";

		sendEmail(emailAddress, emailTitle, content, logTitle);

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
