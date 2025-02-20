package com.trackery.trackerybackapiserver.domain.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trackery.trackerybackapiserver.domain.common.response.ApiResponse;
import com.trackery.trackerybackapiserver.domain.common.response.enums.SuccessCode;
import com.trackery.trackerybackapiserver.domain.user.dto.UserRegisterDto;
import com.trackery.trackerybackapiserver.domain.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;

	/**
	 * 회원가입 정보를 받아서 db에 인서트하고 성공 여부를 반환하는 API
	 *
	 * @param userRegisterDto : 회원가입 정보를 담은 DTO
	 * @return : 성공, 실패 여부 응답
	 */
	@PostMapping("/register")
	public ResponseEntity<ApiResponse<String>> registerUser(@Valid @RequestBody UserRegisterDto userRegisterDto) {
		String authHeader = userService.registerUser(userRegisterDto);
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.header("Authorization", authHeader)
			.body(ApiResponse.success(SuccessCode.CREATED));
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
