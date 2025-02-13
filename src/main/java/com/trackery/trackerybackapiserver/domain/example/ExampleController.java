package com.trackery.trackerybackapiserver.domain.example;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trackery.trackerybackapiserver.domain.common.response.ApiResponse;
import com.trackery.trackerybackapiserver.domain.common.response.enums.SuccessCode;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ExampleController {

	private final ExampleService exampleService;

	@GetMapping
	public ResponseEntity<ApiResponse<String>> hello() {
		String result = exampleService.hello();
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, result));
	}
}
