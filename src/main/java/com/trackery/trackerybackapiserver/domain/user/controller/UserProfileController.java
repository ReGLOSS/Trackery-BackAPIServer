package com.trackery.trackerybackapiserver.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trackery.trackerybackapiserver.domain.common.response.ApiResponse;
import com.trackery.trackerybackapiserver.domain.common.response.enums.SuccessCode;
import com.trackery.trackerybackapiserver.domain.user.dto.UserProfileDto;
import com.trackery.trackerybackapiserver.domain.user.entity.CustomUserDetails;
import com.trackery.trackerybackapiserver.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;

/**
 * packageName    : com.trackery.trackerybackapiserver.domain.user.controller
 * fileName       : UserProfileController
 * author         : Nari-Lee
 * date           : 25. 4. 15.
 * description    : 사이드바에서 사용할 유저 프로필을 조회할때 사용하는 컨트롤러입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 15.		Nari-Lee		최초 생성
 * 25. 4. 28.		Nari-Lee		완성
 * 25. 6. 28.		Nari-Lee		체크스타일 적용
 */
@RestController
@RequestMapping("/api/users/profile")
@RequiredArgsConstructor
public class UserProfileController {

	private final UserService userService;

	/**
	 * 현재 인증된 사용자의 프로필 정보를 조회하는 엔드포인트입니다.
	 *
	 * @param userDetails 현재 인증된 사용자의 세부 정보
	 * @return 사용자 프로필 정보를 포함한 응답
	 */
	@GetMapping("/me")
	public ResponseEntity<ApiResponse<UserProfileDto>> getMyProfile(
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		UserProfileDto profileDto = userService.getUserProfile(userDetails.getUserId());
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, profileDto));
	}
}
