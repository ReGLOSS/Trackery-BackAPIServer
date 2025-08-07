package com.trackery.trackerybackapiserver.domain.user.controller;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trackery.trackerybackapiserver.domain.common.enums.CookieName;
import com.trackery.trackerybackapiserver.domain.common.enums.SameSitePolicy;
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
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.controller
 * fileName       : UserController
 * author         : durururuk
 * date           : 25. 2. 12.
 * description    : 사용자 관련 HTTP 요청을 처리하는 컨트롤러 클래스입니다.
 * 					회원가입과 사용자명 중복 확인 기능을 제공합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 12.		durururuk		최초 생성
 * 25. 2. 12.		durururuk		기본 회원가입 기능 구현
 * 25. 2. 12.		durururuk		네이버 체크 스타일에 맞게 서식 수정
 * 25. 2. 13.		durururuk		컨트롤러에서 회원가입 시 공통 응답 포맷에 따르도록 수정
 * 25. 2. 14.		durururuk		registerUser 메서드 @Valid 추가
 * 25. 2. 14.		durururuk		닉네임 중복 체크하는 api 구현
 * 25. 2. 14.		durururuk		회원가입, 닉네임 중복체크 api 퍼블릭으로 허용, 컨트롤러, 서비스에 주석 추가
 * 25. 2. 18.		inari		dev 병합후 랜딩페이지 엔드포인트 수정
 * 25. 2. 20.		durururuk		회원가입 시 JWT를 담은 헤더를 같이 반환하도록 추가
 * 25. 2. 20.		durururuk		header -> authHeader 변수명 수정
 * 25. 2. 24.		inari		자바독 주석 추가
 * 25. 2. 24.		durururuk		회원가입 시 인증 헤더 -> http-only 쿠키 방식으로 변경
 * 25. 2. 25.		durururuk		로그인 서비스, 컨트롤러 추가
 * 25. 2. 26.		durururuk		액세스 토큰 쿠키 생성 메서드 클래스 분리
 * 25. 3. 4.		durururuk		이메일 요청 검증 기능 추가
 * 25. 3. 5.		durururuk		이메일 관련 기능 user 도메인에서 분리
 * 25. 3. 5.		durururuk		메일 컨트롤러 mockMvc 테스트 작성
 * 25. 3. 11.		durururuk		비밀번호 변경 컨트롤러 작성
 * 25. 3. 14.		durururuk		비밀번호 찾기 기능 리팩터링
 * 25. 3. 14.		durururuk		이메일토큰 테스트코드 추가
 * 25. 3. 14.		durururuk		유저명 사용가능할 시 userNameToken 쿠키에 추가
 * 25. 3. 14.		durururuk		주석 수정
 * 25. 3. 14.		durururuk		수정된 로직에 맞게 테스트 코드 수정
 * 25. 3. 20.		durururuk		회원가입 성공 시 이메일,유저명 토큰 삭제하게 수정
 * 25. 3. 28.		durururuk		리프레시 토큰 레디스 저장 기능 구현
 * 25. 3. 28.		durururuk		Cookie 유효시간 수정
 * 25. 3. 28.		durururuk		회원가입 시에도 refreshToken 발급되게 수정
 * 25. 3. 29.		durururuk		코드 가독성을 위해 List.of(accessToken, refreshToken) 구조에서 DTO 방식으로 변경
 * 25. 3. 29.		durururuk		mockMvc 단위 테스트용 필터 없는 테스트 컨픽 작성
 * 25. 4. 9.		durururuk		상세 정보 조회 API 추가
 * 25. 4. 10.		durururuk		이메일 토큰 기반 비밀번호 변경 url 변경, 인증 기반 비밀번호 변경 기능 구현
 * 25. 4. 10.		durururuk		인증 기반 비밀번호 변경 컨트롤러 mockMvc 테스트 작성
 * 25. 4. 12.		durururuk		닉네임 변경 기능 구현
 * 25. 4. 12.		durururuk		유저명 변경 기능 구현
 * 25. 6. 25.		inari		로그아웃 기능 추가
 * 25. 6. 26.		inari		회원 탈퇴 기능 작성
 * 25. 6. 26.		inari		탈퇴시 서비스에서 컨트롤러로 쿠키삭제 처리 피드백 반영
 * 25. 6. 27.		inari		쿠키 이름 및 정책 enum으로 수정
 * 25. 7. 11.		inari		user 도메인의 자바독 누락 및 체크스타일 해결
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
	 * 회원가입 정보를 받아서 데이터베이스에 저장하고 JWT 토큰을 발급해서 쿠키로 반환하는 API
	 *
	 * @param emailToken 이메일 인증 토큰 (쿠키에서 추출)
	 * @param userNameToken 사용자명 검증 토큰 (쿠키에서 추출)
	 * @param userRegisterDto 회원가입 정보를 담은 DTO
	 * @return 회원가입 성공 응답
	 */
	@PostMapping("/register")
	public ResponseEntity<ApiResponse<String>> register(@CookieValue(name = "emailToken") String emailToken,
		@CookieValue(name = "userNameToken") String userNameToken,
		@Valid @RequestBody UserRegisterDto userRegisterDto) {
		AuthTokenDto authTokenDto = userService.registerUser(emailToken, userNameToken, userRegisterDto);

		ResponseCookie accessTokenCookie = CookieUtil.createHttpOnlyCookie(
			CookieName.ACCESS_TOKEN.getValue(), authTokenDto.accessToken(), Duration.ofMinutes(60));
		ResponseCookie refreshTokenCookie = CookieUtil.createHttpOnlyCookie(
			CookieName.REFRESH_TOKEN.getValue(), authTokenDto.refreshToken(), Duration.ofDays(7));
		ResponseCookie emailTokenCookie = CookieUtil.deleteCookie(
			CookieName.EMAIL_TOKEN.getValue(), SameSitePolicy.STRICT.getValue());
		ResponseCookie userNameTokenCookie = CookieUtil.deleteCookie(
			CookieName.USERNAME_TOKEN.getValue(), SameSitePolicy.STRICT.getValue());

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

		ResponseCookie accessTokenCookie = CookieUtil.createHttpOnlyCookie(
			CookieName.ACCESS_TOKEN.getValue(), authTokenDto.accessToken(), Duration.ofMinutes(60));
		ResponseCookie refreshTokenCookie = CookieUtil.createHttpOnlyCookie(
			CookieName.REFRESH_TOKEN.getValue(), authTokenDto.refreshToken(), Duration.ofDays(7));

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
	public ResponseEntity<ApiResponse<Boolean>> checkUsernameAvailability(
		@RequestParam
		@Pattern(regexp = "^\\w{4,15}$",
			message = "유저명은 4~15자 길이에 영문 대소문자, 숫자, 밑줄(_)로 작성해주세요.") String value) {
		UserNameAvailabilityResponseDto result = userService.checkUsernameAvailability(value);

		if (result.available()) {
			ResponseCookie cookie = CookieUtil.createHttpOnlyCookie(
				CookieName.USERNAME_TOKEN.getValue(), result.token(), Duration.ofMinutes(10));

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
	 * @return 사용자 상세 정보 DTO
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
		userService.logout(accessToken, refreshToken);
		HttpHeaders headers = createCookieDeletionHeaders();

		return ResponseEntity.ok()
			.headers(headers)
			.body(ApiResponse.success(SuccessCode.OK));
	}

	/**
	 * 회원탈퇴 API
	 * 사용자의 개인정보를 익명화하고 상태를 탈퇴로 변경합니다.
	 * 모든 JWT 토큰을 무효화하고 쿠키를 삭제하여 완전한 탈퇴 처리를 합니다.
	 * @param userDetails 인증된 사용자 정보
	 * @param accessToken 액세스 토큰 (쿠키에서 추출)
	 * @param refreshToken 리프레시 토큰 (쿠키에서 추출)
	 * @return 회원탈퇴 성공 응답
	 */
	@DeleteMapping("/delete")
	public ResponseEntity<ApiResponse<String>> deleteUser(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@CookieValue(name = "accessToken") String accessToken,
		@CookieValue(name = "refreshToken") String refreshToken) {
		userService.deleteUser(userDetails.getUserId(), accessToken, refreshToken);
		HttpHeaders headers = createCookieDeletionHeaders();

		return ResponseEntity.ok()
			.headers(headers)
			.body(ApiResponse.success(SuccessCode.OK));
	}

	/**
	 * 인증 관련 쿠키들을 삭제하는 HttpHeaders를 생성하는 헬퍼 메서드입니다.
	 * @return 쿠키 삭제 헤더
	 */
	private HttpHeaders createCookieDeletionHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.SET_COOKIE, CookieUtil.deleteCookie(
			CookieName.ACCESS_TOKEN.getValue(), SameSitePolicy.STRICT.getValue()).toString());
		headers.add(HttpHeaders.SET_COOKIE, CookieUtil.deleteCookie(
			CookieName.REFRESH_TOKEN.getValue(), SameSitePolicy.STRICT.getValue()).toString());
		headers.add(HttpHeaders.SET_COOKIE, CookieUtil.deleteCookie(
			CookieName.SESSION.getValue(), SameSitePolicy.LAX.getValue()).toString());
		return headers;
	}
}
