package com.trackery.trackerybackapiserver.domain.user.controller;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trackery.trackerybackapiserver.domain.common.enums.CookieName;
import com.trackery.trackerybackapiserver.domain.common.response.ApiResponse;
import com.trackery.trackerybackapiserver.domain.common.response.enums.SuccessCode;
import com.trackery.trackerybackapiserver.domain.common.util.CookieUtil;
import com.trackery.trackerybackapiserver.domain.user.dto.OAuthLinkRequestDto;
import com.trackery.trackerybackapiserver.domain.user.dto.OAuthLinkTokenDto;
import com.trackery.trackerybackapiserver.domain.user.dto.OAuthLoginDto;
import com.trackery.trackerybackapiserver.domain.user.dto.OAuthResponseDto;
import com.trackery.trackerybackapiserver.domain.user.dto.OAuthUrlResponseDto;
import com.trackery.trackerybackapiserver.domain.user.entity.CustomUserDetails;
import com.trackery.trackerybackapiserver.domain.user.enums.OAuthProvider;
import com.trackery.trackerybackapiserver.domain.user.service.OAuthLinkTokenService;
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
 * 25. 2. 27.        inari       	최초 생성
 * 25. 3. 25.        inari			계정 연동 토큰 생성 API 추가
 * 25. 3. 26.        inari			로그인 통합 매서드로 변경
 * 25. 3. 27.        inari			provider를 enum으로 변경
 * 25. 3. 27.        inari			코드 스멜 수정
 * 25. 4. 08.        inari			GlobalExceptionHandle로 ApiException 위임
 * 25. 6. 23.        inari		 	기존 유저에 간편 로그인 연동 추가
 * 25. 6. 24.        inari		 	linkToken을 이용하는 방식으로 변경
 * 25. 6. 25.        inari		 	리프레시 토큰 발급 추가
 * 25. 6. 27.        inari      	쿠키 이름과 정책 enum으로 변경
 * 25. 6. 28.        inari      	outhLogin의 코드 복잡도 해결
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
	 * 간편 로그인 연동 서비스 객체입니다.
	 */
	private final OAuthLinkTokenService oAuthLinkTokenService;

	/**
	 * OAuth 계정 연동을 위한 토큰을 생성하는 API 메서드입니다.
	 *
	 * @param request 계정 연동 요청 정보(제공자, 이메일)를 담고 있는 DTO
	 * @return 생성된 연동 토큰 정보를 포함한 응답 엔티티
	 */
	@PostMapping("/link-account")
	public ResponseEntity<ApiResponse<OAuthLinkTokenDto>> createLinkToken(@RequestBody OAuthLinkRequestDto request) {
		// 연동 토큰 생성
		String token = oAuthLinkTokenService.createLinkToken(request.getProvider(), request.getEmail());

		OAuthLinkTokenDto response = OAuthLinkTokenDto.builder()
			.token(token)
			.provider(request.getProvider())
			.build();

		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, response));
	}

	/**
	 * 통합된 OAuth 로그인 처리 메서드입니다.
	 *
	 * @param provider OAuth 제공자(KAKAO, GOOGLE, GITHUB, NAVER)
	 * @param code 인증 코드
	 * @param state 상태 값(선택 사항)
	 * @param linkToken 계정 연동 토큰(선택 사항)
	 * @return 로그인 처리 결과
	 */
	@GetMapping("/login/{provider}")
	public ResponseEntity<ApiResponse<OAuthResponseDto>> oauthLogin(
		@PathVariable("provider") OAuthProvider provider,
		@RequestParam("code") String code,
		@RequestParam(value = "state", required = false) String state,
		@RequestParam(value = "linkToken", required = false) String linkToken) {

		log.info("OAuth 로그인 요청: provider={}, code={}", provider, code);

		String extractedLinkToken = extractLinkToken(provider, linkToken, state);
		OAuthLoginDto oAuthLoginDto = processLinkToken(extractedLinkToken, provider, code);

		return processOAuthLogin(oAuthLoginDto);
	}

	/**
	 * OAuth 제공자별로 링크 토큰을 추출하는 메서드입니다.
	 *
	 * @param provider OAuth 제공자
	 * @param linkToken 직접 전달받은 링크 토큰
	 * @param state OAuth state 파라미터 (네이버의 경우 토큰 포함 가능)
	 * @return 추출된 링크 토큰 또는 null
	 */
	private String extractLinkToken(OAuthProvider provider, String linkToken, String state) {
		log.info("링크 토큰 추출 시도: provider={}, linkToken={}, state={}", provider, linkToken, state);

		if (provider == OAuthProvider.NAVER) {
			return extractNaverLinkToken(linkToken, state);
		}

		return extractGeneralLinkToken(provider, linkToken);
	}

	/**
	 * 네이버 OAuth에서 링크 토큰을 추출하는 메서드입니다.
	 * 네이버는 state 파라미터에 토큰을 포함시키는 방식을 사용합니다.
	 *
	 * @param linkToken 직접 전달받은 링크 토큰
	 * @param state OAuth state 파라미터
	 * @return 추출된 링크 토큰 또는 null
	 */
	private String extractNaverLinkToken(String linkToken, String state) {
		if (isValidToken(linkToken)) {
			return linkToken;
		}

		if (state != null && state.startsWith("random_state_")) {
			String extracted = state.substring("random_state_".length());
			log.info("네이버 state에서 링크 토큰 추출 성공: 원본state={}, 추출된linkToken={}", state, extracted);
			return extracted;
		}

		if (state != null) {
			log.info("네이버 state 형식이 맞지 않음: state={}", state);
		}

		return null;
	}

	/**
	 * 일반 OAuth 제공자(카카오, 구글, 깃허브)에서 링크 토큰을 추출하는 메서드입니다.
	 *
	 * @param provider OAuth 제공자
	 * @param linkToken 링크 토큰 파라미터
	 * @return 유효한 링크 토큰 또는 null
	 */
	private String extractGeneralLinkToken(OAuthProvider provider, String linkToken) {
		if (isValidToken(linkToken)) {
			log.info("{}에서 link_token 파라미터로 토큰 추출 성공: linkToken={}", provider, linkToken);
			return linkToken;
		}

		log.info("{}에서 link_token 파라미터 없음", provider);
		return null;
	}

	/**
	 * 토큰이 유효한지 검증하는 유틸리티 메서드입니다.
	 *
	 * @param token 검증할 토큰
	 * @return 토큰이 null이 아니고 비어있지 않으면 true
	 */
	private boolean isValidToken(String token) {
		return token != null && !token.isEmpty();
	}

	/**
	 * 링크 토큰을 검증하고 계정 연동 정보를 설정하는 메서드입니다.
	 *
	 * @param extractedLinkToken 추출된 링크 토큰
	 * @param provider OAuth 제공자
	 * @param code OAuth 인증 코드
	 * @return 계정 연동 정보가 설정된 OAuthLoginDto
	 */
	private OAuthLoginDto processLinkToken(String extractedLinkToken, OAuthProvider provider, String code) {
		if (!isValidToken(extractedLinkToken)) {
			log.info("링크 토큰 없음 - 일반 로그인 처리: provider={}", provider);
			return OAuthLoginDto.builder()
					.provider(provider.name())
					.code(code)
					.linkAccount(false)
					.build();
		}

		log.info("링크 토큰 감지: provider={}, token={}", provider, extractedLinkToken);
		try {
			Long validatedUserId = oAuthLinkTokenService.validateToken(extractedLinkToken);
			log.info("계정 연동 요청 검증 성공: provider={}, userId={}, token={}", provider, validatedUserId, extractedLinkToken);
			oAuthLinkTokenService.deleteToken(extractedLinkToken);
			return OAuthLoginDto.builder()
					.provider(provider.name())
					.code(code)
					.linkAccount(true)
					.linkUserId(validatedUserId)
					.build();
		} catch (Exception e) {
			log.warn("계정 연동 토큰 검증 실패: {}", e.getMessage());
			return OAuthLoginDto.builder()
					.provider(provider.name())
					.code(code)
					.linkAccount(false)
					.build();
		}
	}

	/**
	 * OAuth 로그인 공통 처리 메서드입니다.
	 *
	 * @param oAuthLoginDto OAuth 로그인 DTO
	 * @return 로그인 처리 결과
	 */
	private ResponseEntity<ApiResponse<OAuthResponseDto>> processOAuthLogin(OAuthLoginDto oAuthLoginDto) {
		// 통합된 메서드 호출로 OAuth 인증 및 JWT 토큰 생성을 한 번에 처리
		OAuthService.OAuthLoginResult result = oAuthService.processOAuthLogin(oAuthLoginDto);
		OAuthResponseDto responseDto = result.getResponseDto();

		// 이메일이 이미 존재하고 연동을 원하지 않는 경우
		if (responseDto.isExistingEmail() && !oAuthLoginDto.isLinkAccount()) {
			return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, responseDto));
		}

		// 로그인 성공 또는 계정 연동 성공한 경우
		// JWT 토큰을 쿠키에 설정
		if (result.getAuthTokenDto() != null) {
			HttpHeaders authHeaders = CookieUtil.setAuthCookie(result.getAuthTokenDto());
			return ResponseEntity.ok()
				.headers(authHeaders)
				.body(ApiResponse.success(SuccessCode.OK, responseDto));
		} else {
			ResponseCookie cookie = CookieUtil.createHttpOnlyCookie(
				CookieName.ACCESS_TOKEN.getValue(), result.getJwtToken(), Duration.ofMinutes(60));
			return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, cookie.toString())
				.body(ApiResponse.success(SuccessCode.OK, responseDto));
		}
	}

	/**
	 * OAuth 인증 URL을 생성하는 API 메서드입니다.
	 *
	 * @param provider OAuth 제공자(KAKAO, GOOGLE, GITHUB, NAVER)
	 * @param userDetails 현재 인증된 사용자 정보
	 * @return OAuth 인증 URL 응답
	 */
	@PostMapping("/link/{provider}/url")
	public ResponseEntity<ApiResponse<OAuthUrlResponseDto>> generateOAuthUrl(
		@PathVariable("provider") OAuthProvider provider,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		log.info("OAuth URL 생성 요청: provider={}, userId={}", provider, userDetails.getUserId());

		try {
			// 현재 사용자 ID 조회
			Long userId = userDetails.getUserId();

			// 계정 연동용 토큰 생성
			String linkToken = oAuthLinkTokenService.createLinkToken(provider.name(), userId);

			// OAuth 인증 URL 생성 (link_token 포함)
			OAuthUrlResponseDto urlResponse = oAuthService.generateAuthUrlWithToken(provider, linkToken);
			log.info("OAuth URL 생성 성공: provider={}, authUrl={}", provider, urlResponse.getAuthUrl());

			return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, urlResponse));
		} catch (Exception e) {
			log.error("OAuth URL 생성 실패: provider={}, error={}", provider, e.getMessage(), e);
			throw e;
		}
	}
}
