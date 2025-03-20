package com.trackery.trackerybackapiserver.domain.mail.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.mail.service
 * fileName       : EmailSenderService
 * author         : durururuk
 * date           : 25. 3. 20.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 20.        durururuk      최초 생성
 * 25. 3. 20.	     durururuk	    이메일 전송 관련 로직 분리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailSenderService {
	private final JavaMailSender javaMailSender;
	private final TemplateEngine templateEngine;

	/**
	 * 이메일 템플릿 내부에서의 제목과 본문을 동적으로 작성하는 메서드
	 * @param htmlTitle : 이메일 내부 제목
	 * @param htmlContents 이메일 내부 본문
	 * @return : 동적으로 생성된 html 템플릿
	 */
	public String generateEmailContents(String htmlTitle, String htmlContents) {
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
	public void sendEmail(String emailAddress, String emailTitle, String content, String logTitle) {
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
}
