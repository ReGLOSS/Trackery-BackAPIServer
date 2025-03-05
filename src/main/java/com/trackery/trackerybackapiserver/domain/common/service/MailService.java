package com.trackery.trackerybackapiserver.domain.common.service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

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
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {
	private final JavaMailSender javaMailSender;
	private final StringRedisTemplate redisTemplate;
	private final SecureRandom secureRandom = new SecureRandom();
	private final JwtUtil jwtUtil;

	/**
	 * HTML 양식으로 작성된 이메일을 보냅니다.
	 *
	 * @param email : 발신 대상 이메일
	 * @param authNumber : 인증번호
	 */
	@SuppressWarnings({"checkstyle:RegexpSingleline", "checkstyle:LineLength"})
	public void sendAuthMessage(String email, String authNumber) {
		try {
			MimeMessage mimeMessage = javaMailSender.createMimeMessage();

			MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

			mimeMessageHelper.setTo(email);
			mimeMessageHelper.setSubject("Trackery 회원가입 인증번호입니다.");

			String content = String.format("""
					<!DOCTYPE html>
					<html lang="ko">
					<head>
						<meta charset="UTF-8">
						<meta name="viewport" content="width=device-width, initial-scale=1.0">
						<title>Trackery 인증 이메일</title>
					</head>
					<body style="margin: 0; padding: 0; font-family: Arial, sans-serif;">
						<table align="center" width="500px" cellpadding="0" cellspacing="0" border="0"\s
							style="border: 1px solid gray; background-color: #ffffff; text-align: center;">
							<tr>
								<td style="background-color: #71717c; padding: 15px; color: white; font-weight: bold; font-size: 18px;">
									Trackery 회원 가입을 위한 인증번호입니다.
								</td>
							</tr>
							<tr>
								<td style="padding: 20px; background-color: #d8e0e6; text-align: center; font-size: 14px;">
									아래 인증 번호를 확인하여 이메일 주소 인증을 완료해주세요.
									<hr style="border: 0; border-top: 1px solid #bbb;">
									<p><strong>이메일 :</strong> %s</p>
									<p><strong>인증 번호 :</strong> %s</p>
								</td>
							</tr>
							<tr>
								<td style="padding: 15px; font-size: 12px; color: #888;">
									본 메일은 발신전용으로, 회신되지 않습니다.
								</td>
							</tr>
						</table>
					</body>
					</html>
				\t""", email, authNumber);

			mimeMessageHelper.setText(content, true);

			javaMailSender.send(mimeMessage);
			log.info("인증 이메일 전송 완료 : {}", email);

		} catch (MessagingException e) {
			log.error("인증 이메일 전송 실패 : {}", email);
			throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 인증번호를 생성해서 이메일을 보내고 레디스에 정보를 저장합니다.
	 *
	 * @param email : 대상 이메일
	 */
	public void requestEmailVerify(String email) {
		String authNumber = String.format("%06d", secureRandom.nextInt(1000000));

		sendAuthMessage(email, authNumber);

		redisTemplate.opsForValue().set("email:verify:" + email, authNumber, 5, TimeUnit.MINUTES);
	}

	/**
	 * 이메일 인증을 검증합니다.
	 *
	 * @param email : 검증할 이메일
	 * @param authNumber : 입력된 인증번호
	 * @return : 이메일 JWT 토큰
	 */
	public String verifyEmail(String email, String authNumber) {
		String redisNumber = redisTemplate.opsForValue().get("email:verify:" + email);

		if (redisNumber == null || !redisNumber.equals(authNumber)) {
			throw new ApiException(ErrorCode.BAD_REQUEST);
		}

		redisTemplate.delete("email:verify:" + email);

		return jwtUtil.generateEmailToken(email);
	}
}
