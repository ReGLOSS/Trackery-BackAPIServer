package com.trackery.trackerybackapiserver.domain.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trackery.trackerybackapiserver.domain.auth.dto.AuthUserDto;
import com.trackery.trackerybackapiserver.domain.auth.service.AuthService;
import com.trackery.trackerybackapiserver.domain.common.response.ApiResponse;
import com.trackery.trackerybackapiserver.domain.common.response.enums.SuccessCode;
import com.trackery.trackerybackapiserver.domain.user.entity.CustomUserDetails;

import lombok.RequiredArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.auth.controller
 * fileName       : AuthController
 * author         : durururuk
 * date           : 25. 3. 26.
 * description    : 프론트엔드에서 백엔드 서버에 인증 관련 요청하는 API를 담당하는 컨트롤러
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 3. 26.        durururuk      최초 생성
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;

	/**
	 * 인증 정보를 반환하는 API
	 *
	 * @param userDetails : JWT 필터를 통해 Security Context Holder에 저장된 인증 정보
	 * @return : 인증 정보(id, 유저명, 역할id)가 담긴 DTO
	 */
	@GetMapping("/me")
	public ResponseEntity<ApiResponse<AuthUserDto>> authCheck(@AuthenticationPrincipal CustomUserDetails userDetails) {
		AuthUserDto authUserDto = authService.toAuthUserDto(userDetails);

		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, authUserDto));
	}
}
