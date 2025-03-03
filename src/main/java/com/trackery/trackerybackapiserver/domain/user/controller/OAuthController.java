package com.trackery.trackerybackapiserver.domain.user.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trackery.trackerybackapiserver.domain.common.response.ApiResponse;
import com.trackery.trackerybackapiserver.domain.common.response.enums.ErrorCode;
import com.trackery.trackerybackapiserver.domain.common.response.enums.SuccessCode;
import com.trackery.trackerybackapiserver.domain.common.response.exception.ApiException;
import com.trackery.trackerybackapiserver.domain.common.util.CookieUtil;
import com.trackery.trackerybackapiserver.domain.user.dto.OAuthLoginDto;
import com.trackery.trackerybackapiserver.domain.user.dto.OAuthResponseDto;
import com.trackery.trackerybackapiserver.domain.user.service.OAuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.controller
 * fileName       : OAuthController
 * author         : inari
 * date           : 25. 2. 27.
 * description    : 간편 로그인 관련 HTTP 요청을 처리하는 컨트롤러 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 2. 27.        inari       최초 생성
 */
@Slf4j
@RestController
@RequestMapping("/api/users/oauth")
@RequiredArgsConstructor
public class OAuthController {

	/**
	 * 간편 로그인 서비스 객체입니다.
	 */
	private final OAuthService oAuthService;

	/**
	 * 카카오 로그인 처리
	 *
	 * @param code 인증 코드
	 * @param linkAccount 계정 연동 여부
	 * @return 로그인 처리 결과
	 */
	@GetMapping("/login/kakao")
	public ResponseEntity<ApiResponse<OAuthResponseDto>> kakaoLogin(
		@RequestParam("code") String code,
		@RequestParam(value = "link_account", defaultValue = "false") boolean linkAccount) {

		OAuthLoginDto oAuthLoginDto = OAuthLoginDto.builder()
			.provider("KAKAO")
			.code(code)
			.linkAccount(linkAccount)
			.build();

		return processOAuthLogin(oAuthLoginDto);
	}

	/**
	 * 네이버 로그인 처리
	 *
	 * @param code 인증 코드
	 * @param state CSRF 방지용 상태값
	 * @param linkAccount 계정 연동 여부
	 * @return 로그인 처리 결과
	 */
	@GetMapping("/login/naver")
	public ResponseEntity<ApiResponse<OAuthResponseDto>> naverLogin(
		@RequestParam("code") String code,
		@RequestParam("state") String state,
		@RequestParam(value = "link_account", defaultValue = "false") boolean linkAccount) {

		OAuthLoginDto oAuthLoginDto = OAuthLoginDto.builder()
			.provider("NAVER")
			.code(code)
			.linkAccount(linkAccount)
			.build();

		return processOAuthLogin(oAuthLoginDto);
	}

	/**
	 * 구글 로그인 처리
	 *
	 * @param code 인증 코드
	 * @param linkAccount 계정 연동 여부
	 * @return 로그인 처리 결과
	 */
	@GetMapping("/login/google")
	public ResponseEntity<ApiResponse<OAuthResponseDto>> googleLogin(
		@RequestParam("code") String code,
		@RequestParam(value = "link_account", defaultValue = "false") boolean linkAccount) {

		OAuthLoginDto oAuthLoginDto = OAuthLoginDto.builder()
			.provider("GOOGLE")
			.code(code)
			.linkAccount(linkAccount)
			.build();

		return processOAuthLogin(oAuthLoginDto);
	}

	/**
	 * 깃허브 로그인 처리
	 *
	 * @param code 인증 코드
	 * @param linkAccount 계정 연동 여부
	 * @return 로그인 처리 결과
	 */
	@GetMapping("/login/github")
	public ResponseEntity<ApiResponse<OAuthResponseDto>> githubLogin(
		@RequestParam("code") String code,
		@RequestParam(value = "link_account", defaultValue = "false") boolean linkAccount) {

		OAuthLoginDto oAuthLoginDto = OAuthLoginDto.builder()
			.provider("GITHUB")
			.code(code)
			.linkAccount(linkAccount)
			.build();

		return processOAuthLogin(oAuthLoginDto);
	}

	/**
	 * OAuth 로그인 공통 처리 메서드
	 * @param oAuthLoginDto OAuth 로그인 DTO
	 * @return 로그인 처리 결과
	 */
	private ResponseEntity<ApiResponse<OAuthResponseDto>> processOAuthLogin(OAuthLoginDto oAuthLoginDto) {
		try {
			// 통합된 메서드 호출로 OAuth 인증 및 JWT 토큰 생성을 한 번에 처리
			OAuthService.OAuthLoginResult result = oAuthService.processOAuthLogin(oAuthLoginDto);
			OAuthResponseDto responseDto = result.getResponseDto();

			// 이메일이 이미 존재하고 연동을 원하지 않는 경우
			if (responseDto.isExistingEmail() && !oAuthLoginDto.isLinkAccount()) {
				return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, responseDto));
			}

			// 로그인 성공 또는 계정 연동 성공한 경우
			// JWT 토큰을 쿠키에 설정
			ResponseCookie cookie = CookieUtil.createAccessTokenCookie(result.getJwtToken());

			return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, cookie.toString())
				.body(ApiResponse.success(SuccessCode.OK, responseDto));

		} catch (ApiException e) {
			log.error("OAuth 로그인 처리 중 오류 발생: {}", e.getMessage());
			return ResponseEntity.status(e.getErrorCode().getStatus())
				.body(ApiResponse.error(e.getErrorCode()));
		} catch (Exception e) {
			log.error("OAuth 로그인 처리 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
			return ResponseEntity.internalServerError()
				.body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
		}
	}
}
