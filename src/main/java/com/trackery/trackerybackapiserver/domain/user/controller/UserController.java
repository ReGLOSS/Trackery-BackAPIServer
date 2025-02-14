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

@RestController
@RequestMapping("/api/users")
public class UserController {
	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping("/register")
	public ResponseEntity<ApiResponse<String>> registerUser(@Valid @RequestBody UserRegisterDto userRegisterDto) {
		userService.registerUser(userRegisterDto);
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(ApiResponse.success(SuccessCode.CREATED));
	}

	@GetMapping("/username-availability")
	public ResponseEntity<ApiResponse<Boolean>> checkUsernameAvailability(@RequestParam String username) {
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, userService.checkUsernameAvailability(username)));
	}
}
