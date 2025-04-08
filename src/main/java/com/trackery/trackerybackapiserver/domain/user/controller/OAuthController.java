package com.trackery.trackerybackapiserver.domain.user.controller;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trackery.trackerybackapiserver.domain.common.response.ApiResponse;
import com.trackery.trackerybackapiserver.domain.common.response.enums.SuccessCode;
import com.trackery.trackerybackapiserver.domain.common.util.CookieUtil;
import com.trackery.trackerybackapiserver.domain.user.dto.OAuthLinkRequestDto;
import com.trackery.trackerybackapiserver.domain.user.dto.OAuthLinkTokenDto;
import com.trackery.trackerybackapiserver.domain.user.dto.OAuthLoginDto;
import com.trackery.trackerybackapiserver.domain.user.dto.OAuthResponseDto;
import com.trackery.trackerybackapiserver.domain.user.enums.OAuthProvider;
import com.trackery.trackerybackapiserver.domain.user.service.OAuthLinkService;
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
	private final OAuthLinkService oAuthLinkService;

	/**
	 * OAuth 계정 연동을 위한 토큰을 생성하는 API 메서드입니다.
	 *
	 * @param request 계정 연동 요청 정보(제공자, 이메일)를 담고 있는 DTO
	 * @return 생성된 연동 토큰 정보를 포함한 응답 엔티티
	 */
	@PostMapping("/link-account")
	public ResponseEntity<ApiResponse<OAuthLinkTokenDto>> createLinkToken(@RequestBody OAuthLinkRequestDto request) {
		// 연동 토큰 생성
		String token = oAuthLinkService.createLinkToken(request.getProvider(), request.getEmail());

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
		@RequestParam(value = "link_token", required = false) String linkToken) {

		log.info("OAuth 로그인 요청: provider={}, code={}", provider, code);

		OAuthLoginDto.OAuthLoginDtoBuilder builder = OAuthLoginDto.builder()
			.provider(provider.name())
			.code(code);

		// 링크 토큰이 있으면 Redis에서 검증하고 연동 플래그 설정
		if (linkToken != null && !linkToken.isEmpty()) {
			try {
				OAuthLinkRequestDto linkRequest = oAuthLinkService.validateToken(linkToken);
				builder.linkAccount(true);
				log.info("계정 연동 요청 검증 성공: provider={}, email={}, token={}",
					provider, linkRequest.getEmail(), linkToken);

				// 토큰 사용 후 삭제
				oAuthLinkService.deleteToken(linkToken);

			} catch (Exception e) {
				log.warn("계정 연동 토큰 검증 실패: {}", e.getMessage());
			}
		}

		OAuthLoginDto oAuthLoginDto = builder.build();
		return processOAuthLogin(oAuthLoginDto);
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
		ResponseCookie cookie = CookieUtil.createHttpOnlyCookie("accessToken", result.getJwtToken(),
			Duration.ofMinutes(60));

		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, cookie.toString())
			.body(ApiResponse.success(SuccessCode.OK, responseDto));
	}
}
