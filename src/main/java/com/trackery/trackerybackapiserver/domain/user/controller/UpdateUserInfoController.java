package com.trackery.trackerybackapiserver.domain.user.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trackery.trackerybackapiserver.domain.common.response.ApiResponse;
import com.trackery.trackerybackapiserver.domain.common.response.enums.SuccessCode;
import com.trackery.trackerybackapiserver.domain.common.util.CookieUtil;
import com.trackery.trackerybackapiserver.domain.jwt.dto.AuthTokenDto;
import com.trackery.trackerybackapiserver.domain.user.dto.update.UpdateNickNameDto;
import com.trackery.trackerybackapiserver.domain.user.dto.update.UpdatePasswordDto;
import com.trackery.trackerybackapiserver.domain.user.dto.update.UpdateUserNameDto;
import com.trackery.trackerybackapiserver.domain.user.entity.CustomUserDetails;
import com.trackery.trackerybackapiserver.domain.user.service.UpdateUserInfoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.controller
 * fileName       : UpdateUserInfoController
 * author         : durururuk
 * date           : 25. 4. 12.
 * description    : 유저 정보 업데이트 관련 API
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 12.		durururuk		최초 생성
 */
@Slf4j
@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UpdateUserInfoController {
	private final UpdateUserInfoService updateUserInfoService;

	@PatchMapping("/nickname")
	public ResponseEntity<ApiResponse<Void>> updateNickname(
		@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody UpdateNickNameDto dto
	) {
		updateUserInfoService.updateUserNickname(userDetails.getUserId(), dto.getNickname());

		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK));
	}

	@PatchMapping("/username")
	public ResponseEntity<ApiResponse<Void>> updateUserName(
		@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody UpdateUserNameDto dto
	) {
		AuthTokenDto authTokenDto = updateUserInfoService.updateUserName(userDetails.getUserId(), dto.getUserName());

		HttpHeaders authHeader = CookieUtil.setAuthCookie(authTokenDto);

		return ResponseEntity
			.ok()
			.headers(authHeader)
			.body(ApiResponse.success(SuccessCode.OK));
	}

	/**
	 * 이메일 토큰으로 유저의 비밀번호를 변경하는 API
	 * 로그인을 할 수 없는 유저가 이메일로 비밀번호를 변경할 때 사용되는 API입니다.
	 *
	 * @param emailToken : 인증된 이메일
	 * @param dto : 변경될 비밀번호 dto
	 * @return : 이메일 토큰 재사용 못하게 제거하는 쿠키 + Ok 응답
	 */
	@PatchMapping("/password/email-token")
	public ResponseEntity<ApiResponse<String>> updatePasswordByEmailToken(@CookieValue(name = "emailToken") String emailToken,
		@Valid @RequestBody UpdatePasswordDto dto) {
		updateUserInfoService.updatePasswordByEmailToken(emailToken, dto.getNewPassword());

		ResponseCookie cookie = CookieUtil.deleteCookie("emailToken");

		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, cookie.toString())
			.body(ApiResponse.success(SuccessCode.OK));
	}

	/**
	 * 인증 정보와 DTO로 비밀번호를 변경하는 DTO
	 * 로그인 한 유저가 마이페이지에서 비밀번호를 변경할 때 사용되는 API입니다.
	 *
	 * @param userDetails : 인증된 유저 정보
	 * @param updatePasswordDto : 기존 비밀번호, 새 비밀번호를 담고있는 DTO, Validation으로 한 번 검증
	 * @return : 성공 시 Ok
	 */
	@PatchMapping("/password")
	public ResponseEntity<ApiResponse<Void>> updatePasswordByAuthentication(
		@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody UpdatePasswordDto updatePasswordDto) {
		updateUserInfoService.updatePasswordByAuthentication(userDetails.getUserId(), updatePasswordDto);
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK));
	}

}
