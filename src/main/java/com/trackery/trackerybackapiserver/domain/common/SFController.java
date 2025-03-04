package com.trackery.trackerybackapiserver.domain.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trackery.trackerybackapiserver.domain.common.service.MailService;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.common
 * fileName       : SFController
 * author         : durururuk
 * date           : 25. 3. 3.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 3.        durururuk      최초 생성
 */
@RestController
@RequiredArgsConstructor
public class SFController {
	private final MailService mailService;

	@GetMapping("/api/mail/test")
	public void mailSend() throws MessagingException {
		mailService.sendAuthMessage("zkdltms0913@gmail.com", "123456");
	}
}
