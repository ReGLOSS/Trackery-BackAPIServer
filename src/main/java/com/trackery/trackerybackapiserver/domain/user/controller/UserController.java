package com.trackery.trackerybackapiserver.domain.user.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trackery.trackerybackapiserver.domain.common.response.ApiResponse;
import com.trackery.trackerybackapiserver.domain.common.response.enums.SuccessCode;
import com.trackery.trackerybackapiserver.domain.common.util.CookieUtil;
import com.trackery.trackerybackapiserver.domain.user.dto.UserLoginDto;
import com.trackery.trackerybackapiserver.domain.user.dto.UserRegisterDto;
import com.trackery.trackerybackapiserver.domain.user.dto.VerifyEmailDto;
import com.trackery.trackerybackapiserver.domain.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.controller
 * fileName       : UserController
 * author         : dururuk
 * date           : 25. 2. 12.
 * description    : 사용자 관련 HTTP 요청을 처리하는 컨트롤러 클래스입니다.
 * 					회원가입과 사용자명 중복 확인 기능을 제공합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 12.        durururuk       최초 생성
 * 25. 2. 24.        inari         주석 추가
 * 25. 2. 25.        durururuk      로그인 메서드 추가
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	/**
	 * 사용자 서비스 객체입니다.
	 */
	private final UserService userService;

	/**
	 * 회원가입 정보를 받아서 db에 인서트하고 jwt 토큰을 발급해서 쿠키로 반환하는 API
	 *
	 * @param userRegisterDto : 회원가입 정보를 담은 DTO
	 * @return : 성공, 실패 여부 응답
	 */
	@PostMapping("/register")
	public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody UserRegisterDto userRegisterDto) {
		String jwt = userService.registerUser(userRegisterDto);

		ResponseCookie cookie = CookieUtil.createAccessTokenCookie(jwt);

		return ResponseEntity.status(HttpStatus.CREATED)
			.header(HttpHeaders.SET_COOKIE, cookie.toString())
			.body(ApiResponse.success(SuccessCode.CREATED));
	}

	/**
	 * 로그인 정보 DTO 인증 후 jwt 토큰을 발급해서 쿠키로 반환하는 API
	 *
	 * @param userLoginDto : username, password를 받는 DTO
	 * @return : jwt 토큰을 http-only 쿠키에 담아서 반환
	 */
	@PostMapping("/login")
	public ResponseEntity<ApiResponse<String>> login(@Valid @RequestBody UserLoginDto userLoginDto) {
		String jwt = userService.login(userLoginDto);

		ResponseCookie cookie = CookieUtil.createAccessTokenCookie(jwt);

		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, cookie.toString())
			.body(ApiResponse.success(SuccessCode.OK));
	}

	/**
	 * 해당 유저명이 사용 가능한지 체크하는 API
	 *
	 * @param value String으로 유저명을 받습니다.
	 * @return db 조회 후 해당 유저명이 없다면 true, 이미 존재한다면 false를 반환합니다.
	 */
	@GetMapping("/exists/username")
	public ResponseEntity<ApiResponse<Boolean>> checkUsernameAvailability(@RequestParam String value) {
		boolean isAvailable = userService.checkUsernameAvailability(value);
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, isAvailable));
	}
}
