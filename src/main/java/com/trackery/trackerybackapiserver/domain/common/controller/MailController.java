package com.trackery.trackerybackapiserver.domain.common.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.trackery.trackerybackapiserver.domain.common.dto.VerifyEmailDto;
import com.trackery.trackerybackapiserver.domain.common.response.ApiResponse;
import com.trackery.trackerybackapiserver.domain.common.response.enums.SuccessCode;
import com.trackery.trackerybackapiserver.domain.common.service.MailService;

import lombok.RequiredArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.common.controller
 * fileName       : MailController
 * author         : durururuk
 * date           : 25. 3. 5.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 5.        durururuk      최초 생성
 * 25. 3. 5.        durururuk      유저 컨트롤러에서 분리
 */
@RestController
@RequiredArgsConstructor
public class MailController {
	private final MailService mailService;

	/**
	 * 인증 메일 요청
	 * @param dto : 이메일 DTO
	 * @return : 메일 발송
	 */
	@PostMapping("/mail/request-verify")
	public ResponseEntity<ApiResponse<String>> requestMailVerify(@RequestBody VerifyEmailDto dto) {
		mailService.requestEmailVerify(dto.getEmail());
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK));
	}

	/**
	 * 메일, 인증번호 검증
	 * @param dto : email, authNumber
	 * @return : 인증된 이메일이 subject인 jwt 토큰 발급
	 */
	@PostMapping("/mail/verify")
	public ResponseEntity<ApiResponse<String>> verifyMail(@RequestBody VerifyEmailDto dto) {
		String emailToken = mailService.verifyEmail(dto.getEmail(), dto.getAuthNumber());
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, emailToken));
	}
}
