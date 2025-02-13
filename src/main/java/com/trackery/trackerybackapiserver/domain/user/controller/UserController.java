package com.trackery.trackerybackapiserver.domain.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trackery.trackerybackapiserver.domain.common.response.ApiResponse;
import com.trackery.trackerybackapiserver.domain.common.response.enums.SuccessCode;
import com.trackery.trackerybackapiserver.domain.user.dto.UserDto;
import com.trackery.trackerybackapiserver.domain.user.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {
	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping("/register")
	public ResponseEntity<ApiResponse<String>> registerUser(@RequestBody UserDto userDto) {
		userService.registerUser(userDto);
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(ApiResponse.success(SuccessCode.CREATED));
	}
}
