package com.trackery.trackerybackapiserver.domain.common.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.trackery.trackerybackapiserver.config.RedisConfig;

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
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {
	private final JavaMailSender javaMailSender;
	private final RedisConfig redisConfig;

	@SuppressWarnings({"checkstyle:RegexpSingleline", "checkstyle:LineLength"})
	public void sendAuthMessage(String email, int authNumber) throws MessagingException {
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
								<p><strong>인증 번호 :</strong> %d</p>
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
	}
}
