package com.trackery.trackerybackapiserver.domain.user.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
	public String registerUser(@RequestBody UserDto userDto) {
		userService.registerUser(userDto);
		return "Success";
	}
}
