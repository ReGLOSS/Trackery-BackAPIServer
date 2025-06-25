package com.trackery.trackerybackapiserver.domain.user.controller;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trackery.trackerybackapiserver.domain.common.response.ApiResponse;
import com.trackery.trackerybackapiserver.domain.common.response.enums.SuccessCode;
import com.trackery.trackerybackapiserver.domain.common.util.CookieUtil;
import com.trackery.trackerybackapiserver.domain.jwt.dto.AuthTokenDto;
import com.trackery.trackerybackapiserver.domain.user.dto.DetailedUserInfoDto;
import com.trackery.trackerybackapiserver.domain.user.dto.UserLoginDto;
import com.trackery.trackerybackapiserver.domain.user.dto.UserNameAvailabilityResponseDto;
import com.trackery.trackerybackapiserver.domain.user.dto.UserRegisterDto;
import com.trackery.trackerybackapiserver.domain.user.entity.CustomUserDetails;
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
 * 25. 4. 09.		 durururuk		상세 정보 조회 API 추가
 * 25. 4. 10.		 durururuk		인증 기반 비밀번호 변경 API 추가
 * 25. 6. 25.		 inari			 로그아웃 기능 추가
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
	public ResponseEntity<ApiResponse<String>> register(@CookieValue(name = "emailToken") String emailToken,
		@CookieValue(name = "userNameToken") String userNameToken,
		@Valid @RequestBody UserRegisterDto userRegisterDto) {
		AuthTokenDto authTokenDto = userService.registerUser(emailToken, userNameToken, userRegisterDto);

		ResponseCookie accessTokenCookie = CookieUtil.createHttpOnlyCookie("accessToken", authTokenDto.accessToken(),
			Duration.ofMinutes(60));
		ResponseCookie refreshTokenCookie = CookieUtil.createHttpOnlyCookie("refreshToken", authTokenDto.refreshToken(),
			Duration.ofDays(7));
		ResponseCookie emailTokenCookie = CookieUtil.deleteCookie("emailToken", "Strict");
		ResponseCookie userNameTokenCookie = CookieUtil.deleteCookie("userNameToken", "Strict");

		return ResponseEntity.status(HttpStatus.CREATED)
			.headers(httpHeaders -> {
				httpHeaders.add(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
				httpHeaders.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
				httpHeaders.add(HttpHeaders.SET_COOKIE, emailTokenCookie.toString());
				httpHeaders.add(HttpHeaders.SET_COOKIE, userNameTokenCookie.toString());
			})
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
		AuthTokenDto authTokenDto = userService.login(userLoginDto);

		ResponseCookie accessTokenCookie = CookieUtil.createHttpOnlyCookie("accessToken", authTokenDto.accessToken(),
			Duration.ofMinutes(60));
		ResponseCookie refreshTokenCookie = CookieUtil.createHttpOnlyCookie("refreshToken", authTokenDto.refreshToken(),
			Duration.ofDays(7));

		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
			.header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
			.body(ApiResponse.success(SuccessCode.OK));
	}

	/**
	 * 유저명이 사용가능한지 체크하는 API
	 *
	 * @param value : 체크할 유저명
	 * @return : 유저명 토큰과 boolean 값을 담은 응답
	 */
	@GetMapping("/exists/username")
	public ResponseEntity<ApiResponse<Boolean>> checkUsernameAvailability(@RequestParam String value) {
		UserNameAvailabilityResponseDto result = userService.checkUsernameAvailability(value);

		if (result.available()) {
			ResponseCookie cookie = CookieUtil.createHttpOnlyCookie("userNameToken", result.token(),
				Duration.ofMinutes(10));

			return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, cookie.toString())
				.body(ApiResponse.success(SuccessCode.OK, true));
		} else {
			return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, false));
		}
	}

	/**
	 * 유저의 상세 정보를 조회하는 API
	 * 유저의 기본 정보, 간편로그인 연동 정보를 담은 DTO 반환
	 * @param userDetails : 인증된 유저의 정보
	 * @return DTO
	 */
	@GetMapping("/details")
	public ResponseEntity<ApiResponse<DetailedUserInfoDto>> getDetailedUserInfo(
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		DetailedUserInfoDto result = userService.getDetailedUserInfoByUserId(userDetails.getUserId());
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, result));
	}

	/**
	 * 로그아웃 API
	 * 액세스 토큰을 블랙리스트에 추가하고, 리프레시 토큰을 Redis에서 삭제합니다.
	 * 클라이언트 쿠키도 삭제하여 완전한 로그아웃을 처리합니다.
	 * @param accessToken 액세스 토큰 (쿠키에서 추출)
	 * @param refreshToken 리프레시 토큰 (쿠키에서 추출)
	 * @return 로그아웃 성공 응답
	 */
	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<String>> logout(
		@CookieValue(name = "accessToken") String accessToken,
		@CookieValue(name = "refreshToken") String refreshToken) {
		HttpHeaders headers = userService.logout(accessToken, refreshToken);

		return ResponseEntity.ok()
			.headers(headers)
			.body(ApiResponse.success(SuccessCode.OK));
	}
}
