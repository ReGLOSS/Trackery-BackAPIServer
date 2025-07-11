package com.trackery.trackerybackapiserver.domain.mail.controller;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trackery.trackerybackapiserver.domain.common.dto.VerifyEmailDto;
import com.trackery.trackerybackapiserver.domain.common.enums.CookieName;
import com.trackery.trackerybackapiserver.domain.common.response.ApiResponse;
import com.trackery.trackerybackapiserver.domain.common.response.enums.SuccessCode;
import com.trackery.trackerybackapiserver.domain.common.util.CookieUtil;
import com.trackery.trackerybackapiserver.domain.mail.service.MailService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.common.controller
 * fileName       : MailController
 * author         : durururuk
 * date           : 25. 3. 5.
 * description    : 이메일 관련 기능을 담당하는 REST 컨트롤러입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 5.        durururuk      최초 생성
 * 25. 3. 5.        durururuk      유저 컨트롤러에서 분리
 * 25. 6. 27.        inari      	쿠키 이름과 정책 enum으로 변경
 * 25. 7. 11.        inari      	클래스 설명 추가
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mail")
public class MailController {
	private final MailService mailService;

	//TODO URI 더 생각해보기

	/**
	 * 이메일 인증 메일 요청
	 * @param dto : 이메일 DTO
	 * @return : 메일 발송
	 */
	@PostMapping("/request-verify/email")
	public ResponseEntity<ApiResponse<String>> requestMailVerify(@Valid @RequestBody VerifyEmailDto dto) {
		mailService.sendEmailVerifyMail(dto.getEmail());
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK));
	}

	/**
	 * 메일, 인증번호 검증
	 * @param dto : email, authNumber
	 * @return : 인증된 이메일이 subject인 jwt 토큰 발급
	 */
	@PostMapping("/verify/email")
	public ResponseEntity<ApiResponse<String>> verifyMail(@Valid @RequestBody VerifyEmailDto dto) {
		String emailToken = mailService.verifyEmail(dto.getEmail(), dto.getAuthNumber());

		ResponseCookie cookie = CookieUtil.createHttpOnlyCookie(
			CookieName.EMAIL_TOKEN.getValue(), emailToken, Duration.ofMinutes(10));

		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, cookie.toString())
			.body(ApiResponse.success(SuccessCode.OK));
	}

	/**
	 * 유저명을 알려주는 이메일을 전송합니다.
	 *
	 * @param dto : email 정보를 담고있는 dto
	 * @return : 성공 시 Ok 응답 반환
	 */
	@PostMapping("/find-username")
	public ResponseEntity<ApiResponse<String>> sendUserNameByEmail(@Valid @RequestBody VerifyEmailDto dto) {
		mailService.sendUserNameMail(dto.getEmail());

		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK));
	}
}
